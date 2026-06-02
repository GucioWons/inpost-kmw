package com.guciowons.csv;

import com.guciowons.model.Address;
import com.guciowons.model.ParcelLocker;
import com.guciowons.model.StartingPoint;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class AddressCsvReader {
    public List<ParcelLocker> readLockers() {
        return readRows("parcel-lockers.csv").stream()
                .map(row -> new ParcelLocker(
                        row.getId(),
                        new Address(row.getCity(), row.getLatitude(), row.getLongitude())))
                .toList();
    }

    public List<StartingPoint> readStartingPoints() {
        return readRows("starting-points.csv").stream()
                .map(row -> new StartingPoint(
                        row.getId(),
                        new Address(row.getCity(), row.getLatitude(), row.getLongitude())))
                .toList();
    }

    private List<AddressCsvRow> readRows(String filename) {
        try (Reader reader = new FileReader(filename)) {
            return new CsvToBeanBuilder<AddressCsvRow>(reader)
                    .withType(AddressCsvRow.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build()
                    .parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
