package com.guciowons.algorithm;

import com.guciowons.model.ParcelLocker;
import com.guciowons.model.Route;
import com.guciowons.model.StartingPoint;

import java.util.List;

public interface RouteAlgorithm {
    Route calculate(StartingPoint start, List<ParcelLocker> lockers);
}
