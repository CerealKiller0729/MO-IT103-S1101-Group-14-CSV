package com.mycompany.motorph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class EmployeeModelFromFile {
    private static String filePath = "src/main/resources/EmployeeData.csv";
    private static final List<Employee> employees = loadEmployees();

    /**
     * Loads employee data from the CSV file
     * @return List of Employee objects
     */
    private static List<Employee> loadEmployees() {
        List<Employee> employeeList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip header row
            br.readLine();
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] rowData = parseCSVLine(line);
                
                if (rowData.length >= 19) {
                    employeeList.add(new Employee(rowData));
                } else {
                    System.err.println("Skipping incomplete record: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading employee data: " + e.getMessage());
        }

        return employeeList;
    }

    /**
     * Parses a CSV line, handling quoted values and commas within quotes
     * @param line The CSV line to parse
     * @return Array of string values
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
     * Gets an unmodifiable list of all employees
     * @return List of Employee objects
     */
    public static List<Employee> getEmployeeModelList() {
        return Collections.unmodifiableList(employees);
    }

    /**
     * Finds an employee by ID
     * @param employeeId The employee ID to search for
     * @return Employee object if found, null otherwise
     */
    public static Employee getEmployeeById(String employeeId) {
        return employees.stream()
                .filter(e -> e.getEmployeeNumber().equals(employeeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates the file path for the employee data
     * @param newFilePath New path to the employee data file
     */
    public static void setFilePath(String newFilePath) {
        filePath = newFilePath;
    }
}