package app.service;

import app.constant.AppConstant;
import app.model.Car;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

public class CarFilter {

    private static final DateTimeFormatter DATE_FORMAT_DASHES = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMAT_COMMAS = DateTimeFormatter.ofPattern("yyyy,dd,MM");

    public static List<Car> filter(List<Car> cars, String type, String value) {
        String[] parts = value.split(",");

        switch (type) {
            case AppConstant.FILTER_BRAND_PRICE:
                String brand = parts[0];
                double min = Double.parseDouble(parts[1]);
                double max = Double.parseDouble(parts[2]);
                return cars.stream()
                        .filter(c -> c.getBrand().equalsIgnoreCase(brand))
                        .filter(c -> c.getPrices().getOrDefault("USD", 0.0) >= min)
                        .filter(c -> c.getPrices().getOrDefault("USD", 0.0) <= max)
                        .collect(Collectors.toList());

            case AppConstant.FILTER_BRAND_RELEASE:
                String brandFilter = parts[0];
                final LocalDate releaseDate = parseDate(parts[1]);
                return cars.stream()
                        .filter(c -> c.getBrand().equalsIgnoreCase(brandFilter))
                        .filter(c -> c.getReleaseDate() != null && c.getReleaseDate().isEqual(releaseDate))
                        .collect(Collectors.toList());

            default:
                throw new IllegalArgumentException("Unsupported filter type: " + type);
        }
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT_DASHES);
        } catch (DateTimeParseException e) {
            return LocalDate.parse(value, DATE_FORMAT_COMMAS);
        }
    }
}
