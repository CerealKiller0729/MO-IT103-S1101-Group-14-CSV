package com.mycompany.motorph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SSS extends Calculation {

    private double sssDeduction; // SSS deduction amount
    private final Grosswage grosswage; // Gross wage object for calculation

    private static final String CSV_FILE_PATH = "src/main/resources/SSSCont.csv"; // Path to the SSS contributions CSV file
    private static final List<SSSRecord> sssDeductionRecords; // List of SSS deduction records

    // Static block to load SSS deduction records when the class is loaded
    static {
        sssDeductionRecords = loadSssDeductions();
        if (sssDeductionRecords == null) {
            throw new RuntimeException("Failed to load SSS deductions.");
        }
    }

    /**
     * Constructor for SSS.
     * @param grosswage The Grosswage object containing the employee's gross wage.
     */
    public SSS(Grosswage grosswage) {
        this.grosswage = grosswage;
    }

    /**
     * Calculates the SSS deduction based on the employee's gross wage.
     * @return The SSS deduction amount.
     */
    @Override
    public double calculate() {
        double gross = grosswage.calculate();

        // Initialize SSS deduction to 0
        sssDeduction = 0.0;

        // Iterate through the SSS deduction records
        for (SSSRecord record : sssDeductionRecords) {
            double[] range = parseSssCompensationRange(record.getCompensationRange());

            // Check if the gross wage falls within the range
            if (gross >= range[0] && gross <= range[1]) {
                sssDeduction = record.getContribution();
                break;
            }
        }

        // If no range matches, apply the maximum contribution
        if (sssDeduction == 0.0) {
            double maxContribution = sssDeductionRecords.stream()
                    .mapToDouble(SSSRecord::getContribution)
                    .max()
                    .orElse(0.0);
            sssDeduction = maxContribution;
        }

        return sssDeduction;
    }

    /**
     * Loads SSS deduction records from a CSV file.
     * @return A list of SSSRecord objects.
     */
    private static List<SSSRecord> loadSssDeductions() {
        List<SSSRecord> deductionRecords = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            // Skip header row
            br.readLine();
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = parseCSVLine(line);
                
                if (values.length >= 4) { // Ensure we have enough columns
                    String compensationRange = values[0].trim();
                    double contribution = parseDoubleValue(values[3].trim()); // Contribution is in column D (index 3)
                    
                    deductionRecords.add(new SSSRecord(compensationRange, contribution));
                }
            }
        } catch (IOException e) {
            handleException(e);
        }

        return deductionRecords;
    }

    /**
     * Parses a CSV line, handling quoted values and commas within quotes
     */
    private static String[] parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder value = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(value.toString().trim());
                value = new StringBuilder();
            } else {
                value.append(c);
            }
        }
        // Add the last value
        values.add(value.toString().trim());

        return values.toArray(new String[0]);
    }

    /**
     * Parses a string value into a double
     */
    private static double parseDoubleValue(String value) {
        if (value.isEmpty() || value.equals("-")) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            System.err.println("Invalid numeric format: " + value);
            return 0.0;
        }
    }

    /**
     * Parses the SSS compensation range string into a numeric range.
     * @param compensationRange The compensation range string (e.g., "Below 3,250").
     * @return An array containing the start and end values of the range.
     */
    private static double[] parseSssCompensationRange(String compensationRange) {
        compensationRange = compensationRange.trim();

        // Handle the "Below X" format
        if (compensationRange.startsWith("Below")) {
            String endValue = compensationRange.replace("Below", "").trim();
            double end = parseNumber(endValue);
            return new double[]{0, end};
        }

        // Handle the "Over" format
        if (compensationRange.contains("Over")) {
            String startValue = compensationRange.replace("Over", "").trim();
            double start = parseNumber(startValue);
            return new double[]{start, Double.MAX_VALUE};
        }

        // Handle the "X - Y" format
        if (compensationRange.contains("-")) {
            String[] rangeParts = compensationRange.split("-");
            if (rangeParts.length == 2) {
                try {
                    double start = parseNumber(rangeParts[0].trim());
                    double end = parseNumber(rangeParts[1].trim());
                    return new double[]{start, end};
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid numeric format in compensation range: " + compensationRange, e);
                }
            }
        }

        // Handle single numeric values
        try {
            double value = parseNumber(compensationRange);
            return new double[]{value, value};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid compensation range format: " + compensationRange, e);
        }
    }

    /**
     * Parses a number from a string, removing commas and other non-numeric characters.
     */
    private static double parseNumber(String numberString) {
        numberString = numberString.replace(",", "").trim();
        return Double.parseDouble(numberString);
    }

    /**
     * Handles exceptions by printing the stack trace.
     */
    private static void handleException(Exception e) {
        e.printStackTrace();
    }

    /**
     * Returns the SSS deduction amount.
     */
    public double getSssDeduction() {
        return sssDeduction;
    }
}