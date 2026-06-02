package com.guciowons.distance;

import java.math.BigDecimal;
import java.util.List;

public record OsrmResponse(List<Route> routes) {
    public record Route(BigDecimal distance, BigDecimal duration) {}
}
