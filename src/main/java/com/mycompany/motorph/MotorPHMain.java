/*
 * Main class for MotorPH application.
 * Fully implemented version with all menu functionalities.
 */
package com.mycompany.motorph;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.text.DecimalFormat;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.io.File;

public class MotorPHMain {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    // Helper record for pay coverage period
    private record PayCoverage(int year, int month, int week) {}

    public static void main(String[] args) {
        try {
            System.out.println("\n=== MotorPH Payroll System ===");
            
            // Load attendance records with explicit path verification
            loadAttendanceRecords();

            // Display the main menu
            menu();
        } catch (Exception e) {
            System.err.println("\nA critical error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            scanner.close();
            System.out.println("\nApplication shutdown complete.");
        }
    }

    private static void loadAttendanceRecords() {
        final int MAX_RETRIES = 3;
        int attempts = 0;

        while (attempts < MAX_RETRIES) {
            try {
                String filePath = new File("src/main/resources/AttendanceRecord.csv").getAbsolutePath();
                System.out.println("\nLoading attendance records from: " + filePath);
                
                AttendanceRecord.loadAttendanceFromCSV(filePath);
                System.out.println("Successfully loaded " + 
                    AttendanceRecord.getAttendanceRecords().size() + " attendance records.");
                return;
            } catch (Exception e) {
                attempts++;
                System.err.println("\nAttempt " + attempts + " failed: " + e.getMessage());
                if (attempts < MAX_RETRIES) {
                    System.out.println("Retrying...");
                }
            }
        }

        System.err.println("\nFailed to load attendance records after " + MAX_RETRIES + " attempts.");
        System.exit(1);
    }

    private static void menu() {
        int resume = 1;
        do {
            try {
                System.out.println("\n===== MAIN MENU =====");
                System.out.println("1. Show Employee Details");
                System.out.println("2. Calculate Gross Wage");
                System.out.println("3. Calculate Net Wage");
                System.out.println("0. Exit");
                System.out.print("Enter your choice: ");

                String choice = scanner.next();
                System.out.println("----------------------");

                switch (choice) {
                    case "1" -> handleEmployeeDetails();
                    case "2" -> calculateGrossWage();
                    case "3" -> calculateNetWage();
                    case "0" -> {
                        System.out.println("Exiting application. Goodbye!");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice! Please enter 1, 2, 3, or 0.");
                }

                System.out.print("\nReturn to main menu? (1 = Yes, 0 = No): ");
                resume = getValidIntegerInput(0, 1);
            } catch (Exception e) {
                System.err.println("\nMenu error: " + e.getMessage());
                scanner.nextLine(); // Clear buffer
                resume = 1; // Force return to menu
            }
        } while (resume != 0);
    }

    private static void handleEmployeeDetails() {
        try {
            System.out.println("\n=== EMPLOYEE DETAILS ===");
            System.out.println("1. Individual Employee Details");
            System.out.println("2. All Employee Details");
            System.out.print("Enter your choice: ");

            int choice = getValidIntegerInput(1, 2);

            switch (choice) {
                case 1 -> printEmployeeDetails();
                case 2 -> printAllEmployeeDetails();
            }
        } catch (Exception e) {
            System.err.println("\nError accessing employee details: " + e.getMessage());
        }
    }

    private static void printEmployeeDetails() {
        try {
            String empNum = getValidEmployeeNumber();
            Employee employee = findEmployeeById(empNum);
            
            System.out.println("\n=== EMPLOYEE DETAILS ===");
            System.out.println(employee);
            System.out.println("------------------------");
        } catch (Exception e) {
            System.err.println("\nError displaying employee details: " + e.getMessage());
        }
    }

    private static void printAllEmployeeDetails() {
        try {
            List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
            
            System.out.println("\n=== ALL EMPLOYEES ===");
            System.out.printf("%-10s %-20s %-20s%n", "ID", "Last Name", "First Name");
            System.out.println("------------------------------------------------");
            
            for (Employee employee : employees) {
                System.out.printf("%-10s %-20s %-20s%n",
                    employee.getEmployeeNumber(),
                    employee.getLastName(),
                    employee.getFirstName());
            }
        } catch (Exception e) {
            System.err.println("\nError listing employees: " + e.getMessage());
        }
    }

    private static void calculateGrossWage() {
        try {
            System.out.println("\n=== GROSS WAGE CALCULATION ===");
            
            String empId = getValidEmployeeNumber();
            Employee employee = findEmployeeById(empId);
            PayCoverage coverage = getValidPayCoverage();

            System.out.println("\nCalculating gross wage for:");
            System.out.println("Employee: " + employee.getLastName() + ", " + employee.getFirstName());
            System.out.printf("Period: Week %d of Month %d/%d%n", coverage.week(), coverage.month(), coverage.year());

            Grosswage grosswage = new Grosswage(
                empId, 
                employee.getFirstName(), 
                employee.getLastName(), 
                coverage.year(), 
                coverage.month(), 
                coverage.week(),
                employee.getShiftStartTime(), 
                employee.isNightShift()
            );

            double gross = grosswage.calculate();
            displayGrossWageDetails(coverage.week(), coverage.month(), coverage.year(), grosswage);
        } catch (Exception e) {
            System.err.println("\nError calculating gross wage: " + e.getMessage());
        }
    }

    private static void calculateNetWage() {
        try {
            System.out.println("\n=== NET WAGE CALCULATION ===");
            
            String empId = getValidEmployeeNumber();
            Employee employee = findEmployeeById(empId);
            PayCoverage coverage = getValidPayCoverage();

            System.out.println("\nCalculating net wage for:");
            System.out.println("Employee: " + employee.getLastName() + ", " + employee.getFirstName());
            System.out.printf("Period: Week %d of Month %d/%d%n", coverage.week(), coverage.month(), coverage.year());

            Grosswage grosswage = new Grosswage(
                empId, 
                employee.getFirstName(), 
                employee.getLastName(), 
                coverage.year(), 
                coverage.month(), 
                coverage.week(),
                employee.getShiftStartTime(), 
                employee.isNightShift()
            );

            String employeeName = employee.getLastName() + ", " + employee.getFirstName();
            Netwage netwage = new Netwage(
                empId, 
                employeeName, 
                grosswage.calculate(), 
                grosswage.getHoursWorked(), 
                coverage.week(), 
                grosswage, 
                coverage.month(), 
                coverage.year()
            );

            displayPayrollResults(coverage.week(), coverage.month(), coverage.year(), 
                              empId, employeeName, grosswage, netwage);
        } catch (Exception e) {
            System.err.println("\nError calculating net wage: " + e.getMessage());
        }
    }

    // ================== VALIDATION METHODS ================== //

    private static String getValidEmployeeNumber() {
        while (true) {
            try {
                System.out.print("\nEnter Employee ID: ");
                String empId = scanner.next().trim();
                
                if (!empId.matches("\\d+")) {
                    throw new IllegalArgumentException("Employee ID must contain only numbers");
                }
                
                if (findEmployeeById(empId) == null) {
                    throw new NoSuchElementException("Employee with ID " + empId + " not found");
                }
                
                return empId;
            } catch (IllegalArgumentException | NoSuchElementException e) {
                System.err.println("Error: " + e.getMessage());
                System.out.println("Please try again.");
                scanner.nextLine(); // Clear buffer
            }
        }
    }

    private static PayCoverage getValidPayCoverage() {
        System.out.println("\n=== PAY PERIOD DETAILS ===");
        int year = getValidYear();
        int month = getValidMonth();
        int week = getValidWeek();
        
        // Additional validation for logical date ranges
        LocalDate currentDate = LocalDate.now();
        if (year > currentDate.getYear() + 1) {
            throw new IllegalArgumentException("Year cannot be more than 1 year in the future");
        }
        
        return new PayCoverage(year, month, week);
    }

    private static int getValidYear() {
        while (true) {
            try {
                System.out.print("Enter Year (YYYY): ");
                int year = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                if (year < 2000 || year > LocalDate.now().getYear() + 1) {
                    throw new IllegalArgumentException("Year must be between 2000 and " + (LocalDate.now().getYear() + 1));
                }
                return year;
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a 4-digit year.");
                scanner.nextLine(); // Clear invalid input
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static int getValidMonth() {
        while (true) {
            try {
                System.out.print("Enter Month (1-12): ");
                int month = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                if (month < 1 || month > 12) {
                    throw new IllegalArgumentException("Month must be between 1-12");
                }
                return month;
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a number between 1-12.");
                scanner.nextLine(); // Clear invalid input
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static int getValidWeek() {
        while (true) {
            try {
                System.out.print("Enter Week (1-4): ");
                int week = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                if (week < 1 || week > 4) {
                    throw new IllegalArgumentException("Week must be between 1-4");
                }
                return week;
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a number between 1-4.");
                scanner.nextLine(); // Clear invalid input
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private static int getValidIntegerInput(int min, int max) {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                
                if (input < min || input > max) {
                    throw new IllegalArgumentException("Input must be between " + min + " and " + max);
                }
                return input;
            } catch (InputMismatchException e) {
                System.err.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // Clear invalid input
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    // ================== DISPLAY METHODS ================== //

    private static void displayGrossWageDetails(int week, int month, int year, Grosswage grosswage) {
        try {
            System.out.println("\n=== GROSS WAGE DETAILS ===");
            System.out.printf("Week %d, Month %d/%d%n", week, month, year);
            System.out.println("---------------------------");
            
            System.out.printf("%-25s: %s hrs%n", "Regular Hours", decimalFormat.format(grosswage.getRegularHours()));
            System.out.printf("%-25s: %s hrs%n", "Overtime Hours", decimalFormat.format(grosswage.getOvertimeHours()));
            System.out.printf("%-25s: PHP %s%n", "Regular Pay", decimalFormat.format(grosswage.getRegularPay()));
            System.out.printf("%-25s: PHP %s%n", "Overtime Pay", decimalFormat.format(grosswage.getOvertimePay()));
            System.out.printf("%-25s: PHP %s%n", "Holiday Premium Pay", decimalFormat.format(grosswage.getHolidayPay()));
            System.out.printf("%-25s: PHP %s%n", "Total Gross Wage", decimalFormat.format(grosswage.calculate()));
        } catch (Exception e) {
            System.err.println("\nError displaying wage details: " + e.getMessage());
        }
    }

    private static void displayPayrollResults(int week, int month, int year, String empId, 
                                           String employeeName, Grosswage grosswage, Netwage netwage) {
        try {
            System.out.println("\n=== PAYROLL RESULTS ===");
            System.out.printf("Week %d, Month %d/%d%n", week, month, year);
            System.out.println("-----------------------");
            
            System.out.printf("%-20s: %s%n", "Employee ID", empId);
            System.out.printf("%-20s: %s%n", "Employee Name", employeeName);
            System.out.println("-----------------------");
            
            // Earnings
            System.out.printf("%-20s: %s hrs%n", "Regular Hours", decimalFormat.format(grosswage.getRegularHours()));
            System.out.printf("%-20s: %s hrs%n", "Overtime Hours", decimalFormat.format(grosswage.getOvertimeHours()));
            System.out.printf("%-20s: PHP %s%n", "Regular Pay", decimalFormat.format(grosswage.getRegularPay()));
            System.out.printf("%-20s: PHP %s%n", "Overtime Pay", decimalFormat.format(grosswage.getOvertimePay()));
            System.out.printf("%-20s: PHP %s%n", "Gross Wage", decimalFormat.format(grosswage.calculate()));
            
            // Deductions
            System.out.println("\nDeductions:");
            System.out.printf("%-20s: PHP %s%n", "SSS", decimalFormat.format(netwage.getSSSDeduction()));
            System.out.printf("%-20s: PHP %s%n", "PhilHealth", decimalFormat.format(netwage.getPhilhealthDeduction()));
            System.out.printf("%-20s: PHP %s%n", "Pag-IBIG", decimalFormat.format(netwage.getPagIbigDeduction()));
            System.out.printf("%-20s: PHP %s%n", "Late Penalties", decimalFormat.format(netwage.getLateDeduction()));
            System.out.printf("%-20s: PHP %s%n", "Total Deductions", decimalFormat.format(netwage.getTotalDeductions()));
            System.out.printf("%-20s: PHP %s%n", "Withholding Tax", decimalFormat.format(netwage.getWithholdingTax()));
            
            // Net Wage
            System.out.println("-----------------------");
            System.out.printf("%-20s: PHP %s%n", "NET WAGE", 
                decimalFormat.format(grosswage.calculate() - netwage.getTotalDeductions() - netwage.getWithholdingTax()));
        } catch (Exception e) {
            System.err.println("\nError displaying payroll results: " + e.getMessage());
        }
    }

    // ================== UTILITY METHODS ================== //

    private static Employee findEmployeeById(String empId) {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equals(empId)) {
                return employee;
            }
        }
        return null;
    }
}