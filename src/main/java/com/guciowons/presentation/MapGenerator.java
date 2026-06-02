package com.guciowons.presentation;

import com.guciowons.distance.FeignConfig;
import com.guciowons.distance.OsrmClient;
import com.guciowons.distance.OsrmGeometryResponse;
import com.guciowons.model.Address;
import com.guciowons.model.ParcelLocker;
import com.guciowons.model.Route;
import com.guciowons.model.StartingPoint;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MapGenerator {
    private final OsrmClient osrmClient = FeignConfig.osrmClient();

    public void generateMap(Route route, String algorithmName) {
        StartingPoint start = route.getStartingPoint();
        List<Route.RoutePoint> points = route.getPoints();

        List<Address> orderedAddresses = new ArrayList<>();
        orderedAddresses.add(start.address());
        points.forEach(p -> orderedAddresses.add(p.locker().address()));
        orderedAddresses.add(start.address());

        String coordinates = buildCoordinates(orderedAddresses);
        OsrmGeometryResponse response = osrmClient.getRouteWithGeometry(coordinates);

        if (response.routes() == null || response.routes().isEmpty()) {
            System.err.println("No route geometry returned from OSRM for " + algorithmName);
            return;
        }

        List<List<Double>> geometry = response.routes().getFirst().geometry().coordinates();
        String html = buildHtml(route, algorithmName, geometry);

        String fileName = "map_" + algorithmName.toLowerCase() + "_" + start.address().city().replaceAll("\\s+", "_") + ".html";
        Path path = Path.of(fileName);
        try {
            Files.writeString(path, html);
            System.out.println("Map saved: " + path.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save map: " + e.getMessage());
        }
    }

    private String buildCoordinates(List<Address> addresses) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < addresses.size(); i++) {
            Address a = addresses.get(i);
            if (i > 0) sb.append(";");
            sb.append(a.longitude()).append(",").append(a.latitude());
        }
        return sb.toString();
    }

    private String buildHtml(Route route, String algorithmName, List<List<Double>> geometry) {
        StartingPoint start = route.getStartingPoint();
        BigDecimal centerLat = start.address().latitude();
        BigDecimal centerLon = start.address().longitude();

        StringBuilder geoJsonCoords = new StringBuilder();
        for (int i = 0; i < geometry.size(); i++) {
            if (i > 0) geoJsonCoords.append(",");
            List<Double> coord = geometry.get(i);
            geoJsonCoords.append("[").append(coord.get(0)).append(",").append(coord.get(1)).append("]");
        }

        StringBuilder markerJs = new StringBuilder();
        // Starting point marker (green)
        markerJs.append(String.format(
                "L.marker([%s, %s], {icon: greenIcon}).addTo(map).bindPopup('<b>START / META</b><br>%s');%n",
                centerLat, centerLon, escapeHtml(start.name())
        ));

        // Locker markers
        List<Route.RoutePoint> points = route.getPoints();
        for (int i = 0; i < points.size(); i++) {
            ParcelLocker locker = points.get(i).locker();
            Address addr = locker.address();
            markerJs.append(String.format(
                    "L.marker([%s, %s], {icon: blueIcon}).addTo(map).bindPopup('<b>%d. %s</b><br>+%s m');%n",
                    addr.latitude(), addr.longitude(),
                    i + 1, escapeHtml(locker.name()),
                    points.get(i).distanceTo()
            ));
        }

        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="utf-8"/>
                  <title>Route: %s — %s</title>
                  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
                  <style>
                    body { margin: 0; }
                    #map { height: 100vh; }
                    #info { position: absolute; top: 10px; right: 10px; z-index: 1000;
                            background: white; padding: 10px 14px; border-radius: 8px;
                            box-shadow: 0 2px 8px rgba(0,0,0,.3); font-family: sans-serif; }
                    #info h3 { margin: 0 0 6px; }
                  </style>
                </head>
                <body>
                <div id="map"></div>
                <div id="info">
                  <h3>%s</h3>
                  <p>Start: <b>%s</b></p>
                  <p>Stops: <b>%d</b></p>
                  <p>Total distance: <b>%s m</b></p>
                </div>
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <script>
                  var map = L.map('map').setView([%s, %s], 13);
                  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                    attribution: '© OpenStreetMap contributors'
                  }).addTo(map);

                  var greenIcon = L.icon({
                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                    iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]
                  });
                  var blueIcon = L.icon({
                    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
                    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
                    iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]
                  });

                  var routeLine = L.geoJSON({
                    "type": "Feature",
                    "geometry": {
                      "type": "LineString",
                      "coordinates": [%s]
                    }
                  }, { style: { color: '#e63946', weight: 4, opacity: 0.8 } }).addTo(map);
                  map.fitBounds(routeLine.getBounds());

                  %s
                </script>
                </body>
                </html>
                """.formatted(
                algorithmName, escapeHtml(start.address().city()),
                algorithmName,
                escapeHtml(start.name()),
                points.size(),
                route.getDistance(),
                centerLat, centerLon,
                geoJsonCoords,
                markerJs
        );
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "&#39;");
    }
}
