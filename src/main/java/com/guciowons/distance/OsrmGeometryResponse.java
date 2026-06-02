package com.guciowons.distance;

import java.math.BigDecimal;
import java.util.List;

public record OsrmGeometryResponse(List<Route> routes) {
    public record Route(BigDecimal distance, BigDecimal duration, Geometry geometry) {}
    public record Geometry(String type, List<List<Double>> coordinates) {}
}
