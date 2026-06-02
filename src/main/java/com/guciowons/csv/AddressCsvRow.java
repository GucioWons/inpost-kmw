package com.guciowons.csv;

import com.opencsv.bean.CsvBindByName;

import java.math.BigDecimal;

public class AddressCsvRow {
    @CsvBindByName(column = "city")
    private String city;

    @CsvBindByName(column = "id")
    private String id;

    @CsvBindByName(column = "latitude")
    private String latitude;

    @CsvBindByName(column = "longitude")
    private String longitude;

    public String getCity() {
        return city;
    }

    public String getId() {
        return id;
    }

    public BigDecimal getLatitude() {
        return new BigDecimal(latitude);
    }

    public BigDecimal getLongitude() {
        return new BigDecimal(longitude);
    }
}
