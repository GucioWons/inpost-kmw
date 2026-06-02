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

import java.util.List;

public class Main {
    private static final ConsoleMenu menu = new ConsoleMenu();

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
            executeWithTimer(() -> {
                System.out.println(AlgorithmType.BRUTEFORCE.name());
                Route route = routeService.calculateRoute(startingPoint, lockers, AlgorithmType.BRUTEFORCE);
                menu.printRoute(route);
            });
        }

        executeWithTimer(() -> {
            System.out.println(AlgorithmType.NEAREST_NEIGHBOR.name());
            Route route = routeService.calculateRoute(startingPoint, lockers, AlgorithmType.NEAREST_NEIGHBOR);
            menu.printRoute(route);
        });

        executeWithTimer(() -> {
            System.out.println(AlgorithmType.GENETIC.name());
            Route route = routeService.calculateRoute(startingPoint, lockers, AlgorithmType.GENETIC);
            menu.printRoute(route);
        });
    }

    public static void executeWithTimer(Runnable runnable) {
        long start = System.nanoTime();

        runnable.run();

        long end = System.nanoTime();
        long durationNs = end - start;
        double durationMs = durationNs / 1_000_000.0;

        System.out.println("Execution time: " + durationMs + " ms");
    }
}