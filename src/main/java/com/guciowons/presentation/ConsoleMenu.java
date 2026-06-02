package com.guciowons.presentation;

import com.guciowons.model.Route;
import com.guciowons.model.StartingPoint;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {
    private final Scanner scanner = new Scanner(System.in);

    public String selectCity(List<StartingPoint> startingPoints) {
        List<String> cities = startingPoints.stream()
                .map(point -> point.address().city().toUpperCase())
                .distinct()
                .sorted()
                .toList();

        System.out.println("=== SELECT CITY ===");

        return chooseFromList(cities);
    }

    public StartingPoint selectStartingPoint(List<StartingPoint> startingPoints, String city) {
        List<StartingPoint> points = startingPoints.stream()
                        .filter(p -> p.address().city().equalsIgnoreCase(city))
                        .toList();

        System.out.println("=== SELECT STARTING POINT ===");

        return chooseFromList(points);
    }

    public void printRoute(Route route) {
        StartingPoint startingPoint = route.getStartingPoint();
        List<Route.RoutePoint> points = route.getPoints();

        System.out.print(startingPoint.name() + "(" + startingPoint.address().coordinatesToString() + ")");
        points.forEach(point -> System.out.print("--" + point.distanceTo() + "m-->" + point.locker().name() + "(" + point.locker().address().coordinatesToString() + ")"));
        System.out.print("--" + route.getReturnDistance() + "m-->" + startingPoint.name() + "(" + startingPoint.address().coordinatesToString() + ")");
        System.out.println();
        System.out.println("Total distance: " + route.getDistance());
    }

    private <T> T chooseFromList(List<T> items) {
        for (int i = 0; i < items.size(); i++) {
            System.out.println((i + 1) + ". " + items.get(i));
        }

        int choice = scanner.nextInt();
        scanner.nextLine();

        return items.get(choice - 1);
    }
}
