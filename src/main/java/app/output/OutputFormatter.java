package app.output;

import app.constant.AppConstant;
import app.model.Car;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Used for format the output
 * 
 * @author Angga Bayu S (anggabs86@gmail.com)
 */
public class OutputFormatter {

    /**
     * Format and print the output
     * 
     * @param cars
     * @param format
     */
    public static void formatAndPrint(List<Car> cars, String format) {
        switch (format.toLowerCase()) {
            case AppConstant.OUTPUT_TABLE_PARAM:
                printTable(cars);
                break;
            case AppConstant.OUTPUT_JSON_PARAM:
                printJson(cars);
                break;
            case AppConstant.OUTPUT_XML_PARAM:
                printXml(cars);
                break;
            default:
                cars.forEach(System.out::println);
        }
    }

    private static void printTable(List<Car> cars) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Table header
        String leftAlignFormat = "| %-15s | %-10s | %-15s | %-12s | %-12s | %-12s | %-12s | %-12s |%n";
        System.out.format("+-----------------+------------+-----------------+--------------+--------------+--------------+--------------+--------------+%n");
        System.out.format("| Brand           | Type       | Model           | Release Date | Price (JPY)  | Price (EUR)  | Price (GBP)  | Price (USD)  |%n");
        System.out.format("+-----------------+------------+-----------------+--------------+--------------+--------------+--------------+--------------+%n");

        // Rows OK!
        for (Car car : cars) {
            System.out.format(
                    leftAlignFormat,
                    car.getBrand(),
                    car.getType(),
                    car.getModel(),
                    car.getReleaseDate() != null ? car.getReleaseDate().format(dateFormatter) : "-",
                    car.getPrices().getOrDefault("JPY", 0.0),
                    car.getPrices().getOrDefault("EUR", 0.0),
                    car.getPrices().getOrDefault("GBP", 0.0),
                    car.getPrices().getOrDefault("USD", 0.0)
            );
        }

        // Thee footer
        System.out.format("+-----------------+------------+-----------------+--------------+--------------+--------------+--------------+--------------+%n");
    }

    private static void printJson(List<Car> cars) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // ✅ Support LocalDate
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // pretty date format
        try {
            String jsonOutput = mapper.writeValueAsString(cars);
            System.out.println(jsonOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printXml(List<Car> cars) {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new JavaTimeModule()); // ✅ Support LocalDate
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // pretty date format
        try {
            String xmlOutput = xmlMapper.writeValueAsString(cars);
            System.out.println(xmlOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
