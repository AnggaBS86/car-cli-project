package app.service;

import app.constant.AppConstant;
import app.model.Car;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CarSorter {

    public static void sort(List<Car> cars, String sortOption) {
        if (sortOption == null || sortOption.trim().equalsIgnoreCase("")) return;

        switch (sortOption.toLowerCase()) {
            case AppConstant.SORTER_YEAR:
                Collections.sort(cars, new Comparator<Car>() {
                    @Override
                    public int compare(Car c1, Car c2) {
                        if (c1.getReleaseDate() == null && c2.getReleaseDate() == null) return 0;
                        if (c1.getReleaseDate() == null) return 1;
                        if (c2.getReleaseDate() == null) return -1;
                        return c2.getReleaseDate().compareTo(c1.getReleaseDate()); // descending
                    }
                });
                break;

            case AppConstant.SORTER_PRICE:
                Collections.sort(cars, new Comparator<Car>() {
                    @Override
                    public int compare(Car c1, Car c2) {
                        double p1 = c1.getPrices().getOrDefault("USD", 0.0);
                        double p2 = c2.getPrices().getOrDefault("USD", 0.0);
                        return Double.compare(p2, p1); // descending
                    }
                });
                break;

            case AppConstant.SORTER_SUV_EUR:
                replaceWithSortedAndFiltered(cars, "SUV", "EUR");
                break;

            case AppConstant.SORTER_SEDAN_JPY:
                replaceWithSortedAndFiltered(cars, "Sedan", "JPY");
                break;

            case AppConstant.SORTER_TRUCK_USD:
                replaceWithSortedAndFiltered(cars, "Truck", "USD");
                break;

            default:
                throw new IllegalArgumentException("Unsupported sort option: " + sortOption);
        }
    }

    private static void replaceWithSortedAndFiltered(List<Car> cars, String type, String currency) {
        List<Car> filtered = new ArrayList<>();
        for (Car c : cars) {
            if (type.equalsIgnoreCase(c.getType())) {
                filtered.add(c);
            }
        }

        Collections.sort(filtered, new Comparator<Car>() {
            @Override
            public int compare(Car c1, Car c2) {
                double p1 = c1.getPrices().getOrDefault(currency, 0.0);
                double p2 = c2.getPrices().getOrDefault(currency, 0.0);
                return Double.compare(p2, p1); // descending
            }
        });

        cars.clear();
        cars.addAll(filtered);
    }
}
