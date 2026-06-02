package com.guciowons.distance;

import com.guciowons.model.Address;

import java.math.BigDecimal;

public class DistanceCalculator {
    private final OsrmClient client;

    public DistanceCalculator() {
        this.client = FeignConfig.osrmClient();
    }

    public OsrmResponse.Route calculateDistance(Address address1, Address address2) {
        OsrmResponse response = client.getRoute(
                address1.latitude(),
                address1.longitude(),
                address2.latitude(),
                address2.longitude()
        );

        if (response.routes() == null || response.routes().isEmpty()) {
            throw new RuntimeException("No routes found");
        }

        return response.routes().getFirst();
    }
}
