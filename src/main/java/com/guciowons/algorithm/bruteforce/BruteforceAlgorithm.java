package com.guciowons.algorithm.bruteforce;

import com.guciowons.algorithm.RouteAlgorithm;
import com.guciowons.distance.DistanceDao;
import com.guciowons.model.Address;
import com.guciowons.model.ParcelLocker;
import com.guciowons.model.Route;
import com.guciowons.model.StartingPoint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BruteforceAlgorithm implements RouteAlgorithm {
    private final DistanceDao distanceDao;

    public BruteforceAlgorithm(DistanceDao distanceDao) {
        this.distanceDao = distanceDao;
    }

    @Override
    public Route calculate(StartingPoint start, List<ParcelLocker> lockers) {
        boolean[] used = new boolean[lockers.size()];
        List<ParcelLocker> current = new ArrayList<>();
        Result best = new Result(null, BigDecimal.valueOf(Double.MAX_VALUE));

        permute(start, lockers, used, current, best);

        return best.route;
    }

    private void permute(
            StartingPoint start,
            List<ParcelLocker> lockers,
            boolean[] used,
            List<ParcelLocker> current,
            Result best
    ) {
        if (current.size() == lockers.size()) {

            Route route = buildRoute(start, current);
            BigDecimal dist = route.getDistance();

            if (dist.compareTo(best.distance) < 0) {
                best.distance = dist;
                best.route = route;
            }
            return;
        }

        for (int i = 0; i < lockers.size(); i++) {
            if (used[i]) continue;

            used[i] = true;
            current.add(lockers.get(i));

            permute(start, lockers, used, current, best);

            current.removeLast();
            used[i] = false;
        }
    }

    private Route buildRoute(StartingPoint start, List<ParcelLocker> order) {
        Route route = new Route(start);

        Address current = start.address();

        for (ParcelLocker locker : order) {
            BigDecimal d = distanceDao.get(current, locker.address()).distance();
            route.add(new Route.RoutePoint(locker, d));
            current = locker.address();
        }

        route.setReturnDistance(distanceDao.get(current, start.address()).distance());

        return route;
    }

    private static class Result {
        Route route;
        BigDecimal distance;

        Result(Route route, BigDecimal distance) {
            this.route = route;
            this.distance = distance;
        }
    }
}
