package com.guciowons;

import com.guciowons.algorithm.AlgorithmType;
import com.guciowons.algorithm.RouteService;
import com.guciowons.csv.AddressCsvReader;
import com.guciowons.distance.DistanceCalculator;
import com.guciowons.distance.DistanceDao;
import com.guciowons.model.ParcelLocker;
import com.guciowons.model.Route;
import com.guciowons.model.StartingPoint;
import com.guciowons.presentation.ConsoleMenu;
import com.guciowons.presentation.MapGenerator;

import java.util.List;

public class Main {
    private static final ConsoleMenu menu = new ConsoleMenu();
    private static final MapGenerator mapGenerator = new MapGenerator();

    public static void main(String[] args) {
        AddressCsvReader addressCsvReader = new AddressCsvReader();

        List<StartingPoint> startingPoints = addressCsvReader.readStartingPoints();

        String city = menu.selectCity(startingPoints);

        StartingPoint startingPoint = menu.selectStartingPoint(startingPoints, city);

        List<ParcelLocker> lockers = addressCsvReader.readLockers()
                .stream()
                .filter(locker -> locker.address().city().equalsIgnoreCase(city))
                .toList();

        handleAlgorithms(startingPoint, lockers);
    }

    public static void handleAlgorithms(StartingPoint startingPoint, List<ParcelLocker> lockers) {
        DistanceDao distanceDao = new DistanceDao(new DistanceCalculator(), startingPoint, lockers);
        RouteService routeService = new RouteService(distanceDao);

        if (lockers.size() <= 12) {
            Route bruteforce = executeWithTimer(AlgorithmType.BRUTEFORCE.name(),
                    () -> routeService.calculateRoute(startingPoint, lockers, AlgorithmType.BRUTEFORCE));
            menu.printRoute(bruteforce);
            mapGenerator.generateMap(bruteforce, AlgorithmType.BRUTEFORCE.name());
        }

        Route nearest = executeWithTimer(AlgorithmType.NEAREST_NEIGHBOR.name(),
                () -> routeService.calculateRoute(startingPoint, lockers, AlgorithmType.NEAREST_NEIGHBOR));
        menu.printRoute(nearest);
        mapGenerator.generateMap(nearest, AlgorithmType.NEAREST_NEIGHBOR.name());

        Route genetic = executeWithTimer(AlgorithmType.GENETIC.name(),
                () -> routeService.calculateRoute(startingPoint, lockers, AlgorithmType.GENETIC));
        menu.printRoute(genetic);
        mapGenerator.generateMap(genetic, AlgorithmType.GENETIC.name());
    }

    public static Route executeWithTimer(String name, java.util.function.Supplier<Route> supplier) {
        System.out.println(name);
        long start = System.nanoTime();

        Route route = supplier.get();

        long end = System.nanoTime();
        double durationMs = (end - start) / 1_000_000.0;
        System.out.println("Execution time: " + durationMs + " ms");

        return route;
    }
}