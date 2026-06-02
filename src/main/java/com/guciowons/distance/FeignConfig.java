package com.guciowons.distance;

import feign.Feign;
import feign.jackson.JacksonDecoder;

public class FeignConfig {
    public static OsrmClient osrmClient() {
        return Feign.builder()
                .decoder(new JacksonDecoder())
                .target(OsrmClient.class, "https://router.project-osrm.org");
    }
}
