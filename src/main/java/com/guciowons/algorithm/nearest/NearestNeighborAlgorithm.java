package com.guciowons.algorithm.nearest;

import com.guciowons.algorithm.RouteAlgorithm;
import com.guciowons.distance.DistanceCalculator;
import com.guciowons.distance.DistanceDao;
import com.guciowons.model.Address;
import com.guciowons.model.ParcelLocker;
import com.guciowons.model.Route;
import com.guciowons.model.StartingPoint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class NearestNeighborAlgorithm implements RouteAlgorithm {
    private final DistanceDao distanceDao;

    public NearestNeighborAlgorithm(DistanceDao distanceDao) {
        this.distanceDao = distanceDao;
    }

    @Override
    public Route calculate(StartingPoint start, List<ParcelLocker> lockers) {
        List<ParcelLocker> unvisitedLockers = new ArrayList<>(lockers);
        Route route = new Route(start);

        Address currentAddress = start.address();

        while (!unvisitedLockers.isEmpty()) {
            Route.RoutePoint nearest = findNearestNeighbor(unvisitedLockers, currentAddress);
            route.add(nearest);

            currentAddress = nearest.locker().address();

            unvisitedLockers.remove(nearest.locker());
        }

        route.setReturnDistance(distanceDao.get(currentAddress, start.address()).distance());
        return route;
    }

    private Route.RoutePoint findNearestNeighbor(List<ParcelLocker> unvisitedLockers, Address currentAddress) {
        ParcelLocker nearest = null;
        BigDecimal shortestDistance = BigDecimal.valueOf(Double.MAX_VALUE);

        for (ParcelLocker locker : unvisitedLockers) {
            BigDecimal dist = distanceDao.get(currentAddress, locker.address()).distance();

            if (dist.compareTo(shortestDistance) < 0) {
                shortestDistance = dist;
                nearest = locker;
            }
        }
        return new Route.RoutePoint(nearest, shortestDistance);
    }
}
