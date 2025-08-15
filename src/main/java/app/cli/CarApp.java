package app.cli;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

import app.constant.AppConstant;
import app.model.Car;
import app.output.OutputFormatter;
import app.service.CarFilter;
import app.service.CarParser;
import app.service.CarSorter;

public class CarApp {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        printBanner();
        printUsage();

        System.out.println("");

        while (true) {
            String xmlFile = null;
            String csvFile = null;
            String filterType = null;
            String filterValue = null;
            String sortOption = null;
            String outputFormat = "table";

            if (args.length == 0) {
                // Interactive mode with file existence check
                xmlFile = promptFilePath(scanner, "Enter path to XML file: ");
                csvFile = promptFilePath(scanner, "Enter path to CSV file: ");

                boolean doFilter = promptYesNo(scanner, "Do you want to filter? (yes/no): ");
                if (doFilter) {
                    String chosenFilterType = promptAndValidate(scanner,
                            "Enter filter type (brand-price / brand-release): ",
                            s -> s.equalsIgnoreCase(AppConstant.FILTER_BRAND_PRICE) ||
                                    s.equalsIgnoreCase(AppConstant.FILTER_BRAND_RELEASE),
                            "Invalid filter type. Allowed: brand-price, brand-release.");
                    filterType = chosenFilterType;

                    if (chosenFilterType.equalsIgnoreCase(AppConstant.FILTER_BRAND_PRICE)) {
                        System.out.println("Format: brand,minPrice,maxPrice");
                        System.out.println("Example: Toyota,20000,30000");
                    } else {
                        System.out.println("Format: brand,yyyy-MM-dd OR brand,yyyy,dd,MM");
                        System.out.println("Example: Honda,2020-05-10  OR  Honda,2020,10,05");
                    }

                    final String finalFilterType = chosenFilterType; // required for lambda
                    filterValue = promptAndValidate(scanner,
                            "Enter filter value: ",
                            s -> {
                                String[] parts = s.split(",");
                                if (finalFilterType.equalsIgnoreCase(AppConstant.FILTER_BRAND_PRICE)) {
                                    return parts.length == 3 &&
                                            parts[1].matches("\\d+") &&
                                            parts[2].matches("\\d+");
                                } else {
                                    return parts.length == 2 &&
                                            (parts[1].matches("\\d{4}-\\d{2}-\\d{2}") ||
                                                    parts[1].matches("\\d{4},\\d{2},\\d{2}"));
                                }
                            },
                            "Invalid filter format. Please follow the example above.");
                }

                sortOption = promptAndValidate(scanner,
                        "Enter sort option (year / price / suv-eur / sedan-jpy / truck-usd), or leave blank: ",
                        s -> s.isEmpty() || s.matches("year|price|suv-eur|sedan-jpy|truck-usd"),
                        "Invalid sort option. Allowed: year, price, suv-eur, sedan-jpy, truck-usd.");
                if (sortOption.isEmpty()) sortOption = null;

                outputFormat = promptAndValidate(scanner,
                        "Enter output format (table / json / xml) [default: table]: ",
                        s -> s.isEmpty() || s.matches("table|json|xml"),
                        "Invalid output format. Allowed: table, json, xml.");
                if (outputFormat.isEmpty()) outputFormat = "table";

                System.out.println();
                System.out.println("=== Processing Data ===");

            } else {
                // Command-line args mode
                for (int i = 0; i < args.length; i++) {
                    switch (args[i]) {
                        case AppConstant.OPTION_XML_PARAM:
                            xmlFile = args[++i];
                            break;
                        case AppConstant.OPTION_CSV_PARAM:
                            csvFile = args[++i];
                            break;
                        case AppConstant.OPTION_FILTER_PARAM:
                            filterType = args[++i];
                            filterValue = args[++i];
                            break;
                        case AppConstant.OPTION_SORT_PARAM:
                            sortOption = args[++i];
                            break;
                        case AppConstant.OPTION_OUTPUT_PARAM:
                            outputFormat = args[++i];
                            break;
                        default:
                            System.err.println("Unknown argument " + args[i]);
                            printUsage();
                            return;
                    }
                }
            }

            if (xmlFile == null || csvFile == null) {
                System.err.println("Both --xml and --csv files must be specified.");
                printUsage();
                continue;
            }

            try {
                List<Car> cars = CarParser.parse(xmlFile, csvFile);

                List<Car> processedCars = new ArrayList<>(cars);
                if (filterType != null) {
                    processedCars = CarFilter.filter(processedCars, filterType, filterValue);
                }

                if (sortOption != null) {
                    CarSorter.sort(processedCars, sortOption);
                }

                OutputFormatter.formatAndPrint(processedCars, outputFormat);

            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }

            if (!promptYesNo(scanner, "\nDo you want to process another file? (yes/no): ")) {
                System.out.println("Goodbye!");
                break;
            }

            System.out.println("\n-----------------------------------\n");
        }
    }

    private static String promptFilePath(Scanner scanner, String prompt) {
        return promptAndValidate(scanner, prompt,
                s -> {
                    File file = new File(s);
                    return file.exists() && file.isFile();
                },
                "File does not exist or is not a valid file. Please try again.");
    }

    private static String promptAndValidate(Scanner scanner, String prompt,
                                            Predicate<String> validator, String errorMessage) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (validator.test(input)) {
                return input;
            }
            System.err.println(errorMessage);
        }
    }

    // Yes/No prompt
    private static boolean promptYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes")) return true;
            if (input.equals("no")) return false;
            System.err.println("Please enter 'yes' or 'no'.");
        }
    }

    private static void printUsage() {
        System.out.println("Usage for direct command: java -jar <filename>.jar --xml <xmlFile> --csv <csvFile> [options]");
        System.out.println("Options:");
        System.out.println("  " + AppConstant.OPTION_XML_PARAM + " <path>       Path to the carsType.xml file.");
        System.out.println("  " + AppConstant.OPTION_CSV_PARAM + " <path>       Path to the CarsBrand.csv file.");
        System.out.println("  " + AppConstant.OPTION_FILTER_PARAM + " <type> <value>");
        System.out.println("                     'brand-price' <brand>,<minPrice>,<maxPrice>");
        System.out.println("                     'brand-release' <brand>,<yyyy-MM-dd|yyyy,dd,MM>");
        System.out.println("  " + AppConstant.OPTION_SORT_PARAM + " <option>    'year', 'price', 'suv-eur', 'sedan-jpy', 'truck-usd'");
        System.out.println("  " + AppConstant.OPTION_OUTPUT_PARAM + " <format>  'table', 'json', 'xml' (default: table)");
    }

    public static void printBanner() {
        String reset = "\u001B[0m";
        String cyan = "\u001B[36m";
        String yellow = "\u001B[33m";
        String green = "\u001B[32m";
        String blue = "\u001B[34m";
        String bold = "\u001B[1m";

        String border = cyan + "╔══════════════════════════════════════════════════════════════╗" + reset;
        String bottom = cyan + "╚══════════════════════════════════════════════════════════════╝" + reset;

        System.out.println(border);
        System.out.println(cyan + "║" + reset + yellow + bold + "                    Welcome to CarApp CLI                     " + cyan + "║" + reset);
        System.out.println(cyan + "║" + reset + green + "     Created by Angga Bayu Sejati <anggabs86@gmail.com>        " + cyan + "║" + reset);
        System.out.println(cyan + "║" + reset + blue + "                © " + LocalDate.now().getYear() + " All Rights Reserved                 " + cyan + "║" + reset);
        System.out.println(bottom);
        System.out.println();
    }
}
