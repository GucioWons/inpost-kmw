package com.guciowons.model;

import java.math.BigDecimal;

public record Address(
        String city,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public String coordinatesToString() {
        return String.format("%s,%s", latitude, longitude);
    }
}
