package com.guciowons.distance;

import feign.Param;
import feign.RequestLine;

import java.math.BigDecimal;

public interface OsrmClient {
    @RequestLine(
            "GET /route/v1/driving/{lon1},{lat1};{lon2},{lat2}?overview=false"
    )
    OsrmResponse getRoute(
            @Param("lat1") BigDecimal lat1,
            @Param("lon1") BigDecimal lon1,
            @Param("lat2") BigDecimal lat2,
            @Param("lon2") BigDecimal lon2
    );
}
