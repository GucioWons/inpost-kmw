package com.guciowons.algorithm;

import com.guciowons.algorithm.bruteforce.BruteforceAlgorithm;
import com.guciowons.algorithm.genetic.GeneticAlgorithm;
import com.guciowons.algorithm.nearest.NearestNeighborAlgorithm;
import com.guciowons.distance.DistanceCalculator;
import com.guciowons.distance.DistanceDao;
import com.guciowons.model.ParcelLocker;
import com.guciowons.model.Route;
import com.guciowons.model.StartingPoint;

import java.util.List;

public class RouteService {
    private final DistanceDao distanceDao;

    public RouteService(DistanceDao distanceDao) {
        this.distanceDao = distanceDao;
    }

    public Route calculateRoute(
            StartingPoint startingPoint,
            List<ParcelLocker> lockers,
            AlgorithmType algorithmType
    ) {
        RouteAlgorithm algorithm = switch (algorithmType) {
            case NEAREST_NEIGHBOR -> new NearestNeighborAlgorithm(distanceDao);
            case GENETIC -> new GeneticAlgorithm(distanceDao);
            case BRUTEFORCE -> new BruteforceAlgorithm(distanceDao);
        };

        return algorithm.calculate(startingPoint, lockers);
    }
}
