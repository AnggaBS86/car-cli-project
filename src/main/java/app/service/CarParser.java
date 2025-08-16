package app.service;

import app.model.Car;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * This class used for parsing the CSV and XML file
 * 
 * @author Angga Bayu S (anggabs86@gmail.com)
 */
public class CarParser {
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Parsing the xml and csv file
     * 
     * @param xmlFilePath
     * @param csvFilePath
     * @return List Car Objects
     * @throws Exception
     */
    public static List<Car> parse(String xmlFilePath, String csvFilePath) throws Exception {
        List<Car> cars = parseXml(xmlFilePath);
        Map<String, String> brandDates = parseCsv(csvFilePath);

        for (Car car : cars) {
            String brand = mapModelToBrand(car.getModel());
            car.setBrand(brand);

            String releaseDateStr = brandDates.get(brand);
            if (releaseDateStr != null) {
                car.setReleaseDate(LocalDate.parse(releaseDateStr, CSV_DATE_FORMAT));
            }
        }
        return cars;
    }

    private static List<Car> parseXml(String filePath) throws Exception {
        List<Car> cars = new ArrayList<>();
        File xmlFile = new File(filePath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList carNodes = doc.getElementsByTagName("car");
        for (int i = 0; i < carNodes.getLength(); i++) {
            Node carNode = carNodes.item(i);
            if (carNode.getNodeType() == Node.ELEMENT_NODE) {
                Element carElement = (Element) carNode;
                Car car = new Car();
                car.setType(carElement.getElementsByTagName("type").item(0).getTextContent());
                car.setModel(carElement.getElementsByTagName("model").item(0).getTextContent());

                Node priceNode = carElement.getElementsByTagName("price").item(0);
                if (priceNode instanceof Element) {
                    Element priceElement = (Element) priceNode;
                    String currency = priceElement.getAttribute("currency");
                    double value = Double.parseDouble(priceElement.getTextContent());
                    car.getPrices().put(currency, value);
                }

                NodeList pricesNodes = carElement.getElementsByTagName("prices");
                if (pricesNodes.getLength() > 0) {
                    Element pricesElement = (Element) pricesNodes.item(0);
                    NodeList priceList = pricesElement.getElementsByTagName("price");
                    for (int j = 0; j < priceList.getLength(); j++) {
                        Element priceEl = (Element) priceList.item(j);
                        String currency = priceEl.getAttribute("currency");
                        double value = Double.parseDouble(priceEl.getTextContent());
                        car.getPrices().put(currency, value);
                    }
                }

                cars.add(car);
            }
        }
        return cars;
    }

    private static Map<String, String> parseCsv(String filePath) throws IOException {
        Map<String, String> brandDates = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.replace("\"", "").split(",");
            if (parts.length == 2)
                brandDates.put(parts[0], parts[1]);
        }
        return brandDates;
    }

    private static String mapModelToBrand(String model) {
        switch (model) {
            case "RAV4":
                return "Toyota";
            case "Civic":
                return "Honda";
            case "F-150":
                return "Ford";
            case "Model X":
                return "Tesla";
            case "330i":
                return "BMW";
            case "Q5":
                return "Audi";
            case "Silverado":
                return "Chevrolet";
            case "C-Class":
                return "Mercedes-Benz";
            case "Rogue":
                return "Nissan";
            case "Elantra":
                return "Hyundai";
            default:
                return "Unknown";
        }
    }
}
