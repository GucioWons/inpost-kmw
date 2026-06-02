package com.guciowons.distance;

import com.guciowons.model.Address;
import com.guciowons.model.ParcelLocker;
import com.guciowons.model.StartingPoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistanceDao {
    private static final Map<DistanceKey, OsrmResponse.Route> MAP = new HashMap<>();

    public DistanceDao(DistanceCalculator distanceCalculator, StartingPoint startingPoint, List<ParcelLocker> lockers) {
        for (ParcelLocker locker : lockers) {
            MAP.computeIfAbsent(
                    new DistanceKey(startingPoint.address(), locker.address()),
                    key -> distanceCalculator.calculateDistance(key.address1(), key.address2())
            );
            MAP.computeIfAbsent(
                    new DistanceKey(locker.address(), startingPoint.address()),
                    key -> distanceCalculator.calculateDistance(key.address1(), key.address2())
            );
        }

        for (int i = 0; i < lockers.size(); i++) {
            for (int j = 0; j < lockers.size(); j++) {
                if (j == i) {
                    continue;
                }

                ParcelLocker a = lockers.get(i);
                ParcelLocker b = lockers.get(j);

                MAP.computeIfAbsent(
                        new DistanceKey(a.address(), b.address()),
                        key -> distanceCalculator.calculateDistance(
                                key.address1(),
                                key.address2()
                        )
                );
            }
        }
    }

    public OsrmResponse.Route get(Address address1, Address address2) {
        return MAP.get(new DistanceKey(address1, address2));
    }
}
