package com.guciowons.algorithm.genetic;

import com.guciowons.algorithm.RouteAlgorithm;
import com.guciowons.distance.DistanceDao;
import com.guciowons.model.Address;
import com.guciowons.model.ParcelLocker;
import com.guciowons.model.Route;
import com.guciowons.model.StartingPoint;

import java.math.BigDecimal;
import java.util.*;

public class GeneticAlgorithm implements RouteAlgorithm {
    private static final int POPULATION_SIZE = 60;
    private static final int GENERATIONS = 200;
    private static final int ELITE_COUNT = 3;

    private final DistanceDao distanceDao;
    private final Random random = new Random();

    public GeneticAlgorithm(DistanceDao distanceDao) {
        this.distanceDao = distanceDao;
    }

    @Override
    public Route calculate(StartingPoint start, List<ParcelLocker> lockers) {
        List<Route> population = initPopulation(lockers, start);

        Route bestRoute = null;

        BigDecimal bestFitness = BigDecimal.valueOf(Double.MAX_VALUE);

        for (int gen = 0; gen < GENERATIONS; gen++) {
            population.sort(Comparator.comparing(Route::getDistance));
            List<Route> nextGen = new ArrayList<>();

            for (int i = 0; i < ELITE_COUNT; i++) {
                nextGen.add(population.get(i));
            }

            while (nextGen.size() < POPULATION_SIZE) {
                Route parent1 = tournament(population);
                Route parent2 = tournament(population);

                List<ParcelLocker> childLockers = crossover(parent1, parent2);

                mutate(childLockers, gen);

                childLockers = twoOptFast(childLockers, start);

                nextGen.add(toRoute(start, childLockers));
            }

            population = nextGen;

            Route currentBest = population.stream()
                    .min(Comparator.comparing(Route::getDistance))
                    .orElseThrow();
            BigDecimal fitness = currentBest.getDistance();

            if (fitness.compareTo(bestFitness) < 0) {
                bestFitness = fitness;
                bestRoute = currentBest;
            }
        }

        return bestRoute;
    }

    private List<Route> initPopulation(List<ParcelLocker> lockers, StartingPoint start) {
        List<Route> population = new ArrayList<>();

        for (int i = 0; i < POPULATION_SIZE; i++) {
            List<ParcelLocker> copy = new ArrayList<>(lockers);
            Collections.shuffle(copy, random);
            population.add(toRoute(start, copy));
        }

        return population;
    }

    private Route tournament(List<Route> population) {
        int cutoff = (int) (population.size() * 0.8);
        int k = 5;

        Route best = population.get(random.nextInt(cutoff));

        for (int i = 1; i < k; i++) {
            Route candidate = population.get(random.nextInt(cutoff));

            if (candidate.getDistance().compareTo(best.getDistance()) < 0) {
                best = candidate;
            }
        }

        return best;
    }

    private List<ParcelLocker> crossover(Route p1, Route p2) {
        int size = p1.getPoints().size();

        int start = random.nextInt(size);
        int end = start + random.nextInt(size - start);

        List<ParcelLocker> child = new ArrayList<>(Collections.nCopies(size, null));

        for (int i = start; i < end; i++) {
            child.set(i, p1.getPoints().get(i).locker());
        }

        int idx = 0;

        for (ParcelLocker p : p2.getPoints().stream().map(Route.RoutePoint::locker).toList()) {
            if (!child.contains(p)) {
                while (child.get(idx) != null) {
                    idx++;
                }
                child.set(idx, p);
            }
        }

        return child;
    }

    private void mutate(List<ParcelLocker> route, int gen) {
        double mutationRate = 0.2 + 0.2 * (1.0 - (gen / (double) GENERATIONS));

        if (random.nextDouble() < mutationRate) {
            int i = random.nextInt(route.size());
            int j = random.nextInt(route.size());

            Collections.swap(route, i, j);
        }
    }

    private List<ParcelLocker> twoOptFast(
            List<ParcelLocker> route,
            StartingPoint start
    ) {
        List<ParcelLocker> best = new ArrayList<>(route);
        int n = best.size();

        for (int i = 0; i < n - 1; i++) {

            for (int j = i + 2; j < Math.min(n, i + 8); j++) {

                if (i == 0 && j == n - 1) continue;

                BigDecimal delta = twoOptDelta(best, i, j, start);

                if (delta.compareTo(BigDecimal.ZERO) < 0) {
                    reverse(best, i, j);
                }
            }
        }

        return best;
    }

    private BigDecimal twoOptDelta(
            List<ParcelLocker> route,
            int i,
            int j,
            StartingPoint start
    ) {
        Address a = (i == 0)
                ? start.address()
                : route.get(i - 1).address();

        Address b = route.get(i).address();
        Address c = route.get(j).address();

        Address d = (j == route.size() - 1)
                ? start.address()
                : route.get(j + 1).address();

        return distanceDao.get(a, c).distance()
                .add(distanceDao.get(b, d).distance())
                .subtract(
                        distanceDao.get(a, b).distance()
                                .add(distanceDao.get(c, d).distance())
                );
    }

    private void reverse(List<ParcelLocker> route, int i, int j) {
        while (i < j) {
            ParcelLocker tmp = route.get(i);
            route.set(i, route.get(j));
            route.set(j, tmp);
            i++;
            j--;
        }
    }

    private Route toRoute(StartingPoint start, List<ParcelLocker> lockers) {
        Route route = new Route(start);

        Address current = start.address();

        for (ParcelLocker locker : lockers) {
            BigDecimal dist = distanceDao.get(current, locker.address()).distance();

            route.add(new Route.RoutePoint(locker, dist));

            current = locker.address();
        }

        route.setReturnDistance(distanceDao.get(current, start.address()).distance());
        return route;
    }
}
