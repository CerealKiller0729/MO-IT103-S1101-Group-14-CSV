/*
 * Main class for MotorPH application.
 * Handles user login, menu navigation, and core functionalities.
 * Updated for weekly payroll processing with CSV file support.
 */
package com.mycompany.motorph;

import java.util.List;
import java.util.Scanner;
import java.text.DecimalFormat;

public class MotorPHMain {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static void main(String[] args) {
        // Attempt to log in before proceeding to the main menu
        if (login()) {
            System.out.println("Current Working Directory: " + System.getProperty("user.dir"));

            // Load attendance records from the CSV file
            try {
                AttendanceRecord.loadAttendanceFromCSV("src/main/resources/AttendanceRecord.csv");
                System.out.println("Attendance records loaded successfully.");
            } catch (Exception e) {
                System.err.println("Error loading attendance records: " + e.getMessage());
                return; // Exit if attendance records cannot be loaded
            }

            // Display the main menu
            menu();
        } else {
            System.out.println("Login failed. Exiting application.");
        }
    }

    private static boolean login() {
        String correctUsername = "admin";
        String correctPassword = "admin";

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        // Normalize input to lowercase for case-insensitive comparison
        return username.equalsIgnoreCase(correctUsername) && password.equalsIgnoreCase(correctPassword);
    }

    private static void menu() {
        int resume = 1;
        do {
            System.out.print("""
                    ----- DASHBOARD-----
                    1: Show Employee Details
                    2: Calculate Gross Wage
                    3: Calculate Net Wage
                    0: EXIT
                    -------------------------
                    CHOOSE: """);

            String choice = scanner.next();
            System.out.println("-------------------------");

            switch (choice) {
                case "1" -> handleEmployeeDetails();
                case "2" -> calculateGrossWage();
                case "3" -> calculateNetWage();
                case "0" -> {
                    System.out.println("Exiting application. Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid input! Please try again.");
            }

            System.out.println("Back to menu? 1 = yes, 0 = no");
            resume = scanner.nextInt();
            scanner.nextLine(); // Consume newline
        } while (resume != 0);
    }

    private static void handleEmployeeDetails() {
        System.out.print("""
                ----- DASHBOARD-----
                1: Individual Employee Details
                2: All Employee Details
                -------------------------
                Choose: """);

        String detailSub = scanner.next();
        System.out.println("-------------------------");

        switch (detailSub) {
            case "1" -> printEmployeeDetails();
            case "2" -> printAllEmployeeDetails();
            default -> System.out.println("Invalid input! Please try again.");
        }
    }

    private static void printEmployeeDetails() {
        System.out.print("Enter Employee #: ");
        String empNum = scanner.next();
        System.out.println("-------------------------");

        Employee employee = findEmployeeById(empNum);
        if (employee != null) {
            System.out.println("Employee Details for Employee ID " + empNum + ":" + '\n' +
                    "-------------------------");
            System.out.println(employee.toString());
            System.out.println("-------------------------");
        } else {
            System.out.println("Employee ID " + empNum + " not found.");
        }
    }

    private static void printAllEmployeeDetails() {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        String format = "%-15s %-20s %-20s"; // Format for displaying employee details

        System.out.println("-------------------------");
        System.out.println("|     Employee List     |");
        System.out.println("-------------------------");

        for (Employee employee : employees) {
            System.out.printf(format, employee.getEmployeeNumber(), employee.getLastName(), employee.getFirstName());
            System.out.println(); // Print a new line
        }

        System.out.println("-------------------------");
    }

    private static Employee findEmployeeById(String empId) {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        for (Employee employee : employees) {
            if (employee.getEmployeeNumber().equals(empId)) {
                return employee;
            }
        }
        return null;
    }

    private static void calculateGrossWage() {
        System.out.print("\nEnter Employee ID: ");
        String empId = scanner.next();

        Employee employee = findEmployeeById(empId);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }

        System.out.println("Employee Name: " + employee.getLastName() + ", " + employee.getFirstName());
        int year = getYear();
        int month = getMonth();
        int week = getWeek();

        Grosswage grosswage = new Grosswage(
            empId, 
            employee.getFirstName(), 
            employee.getLastName(), 
            year, 
            month, 
            week, 
            employee.getShiftStartTime(), 
            employee.isNightShift()
        );

        displayGrossWageDetails(week, month, year, grosswage);
    }

    private static void calculateNetWage() {
        System.out.print("\nEnter Employee ID: ");
        String empId = scanner.next();

        Employee employee = findEmployeeById(empId);
        if (employee == null) {
            System.out.println("Employee not found.");
            return;
        }
        System.out.println("Employee Name: " + employee.getLastName() + ", " + employee.getFirstName());

        int year = getYear();
        int month = getMonth();
        int week = getWeek();
        String employeeName = employee.getLastName() + ", " + employee.getFirstName();

        Grosswage grosswage = new Grosswage(
            empId, 
            employee.getFirstName(), 
            employee.getLastName(), 
            year, 
            month, 
            week, 
            employee.getShiftStartTime(), 
            employee.isNightShift()
        );

        Netwage netwage = new Netwage(
            empId, 
            employeeName, 
            grosswage.calculate(), 
            grosswage.getHoursWorked(), 
            week, 
            grosswage, 
            month, 
            year
        );

        displayPayrollResults(week, month, year, empId, employeeName, grosswage, netwage);
    }

    private static void displayGrossWageDetails(int week, int month, int year, Grosswage grosswage) {
        double gross = grosswage.calculate();
        double regularHours = grosswage.getRegularHours();
        double overtimeHours = grosswage.getOvertimeHours();
        double regularPay = grosswage.getRegularPay();
        double overtimePay = grosswage.getOvertimePay();
        double holidayPay = grosswage.getHolidayPay();

        System.out.println("\nWeek " + week + " of Month " + month + "/" + year + ":");
        System.out.println("------------------------------------------");
        System.out.printf("%-25s: %s hrs%n", "Regular Hours", decimalFormat.format(regularHours));
        System.out.printf("%-25s: %s hrs%n", "Overtime Hours", decimalFormat.format(overtimeHours));
        System.out.printf("%-25s: PHP %s%n", "Regular Pay", decimalFormat.format(regularPay));
        System.out.printf("%-25s: PHP %s%n", "Overtime Pay", decimalFormat.format(overtimePay));
        System.out.printf("%-25s: PHP %s%n", "Holiday Premium Pay", decimalFormat.format(holidayPay));
        System.out.printf("%-25s: PHP %s%n", "Total Gross Wage", decimalFormat.format(gross));
        System.out.println("------------------------------------------");
    }

    private static void displayPayrollResults(int week, int month, int year, String empId, 
                                           String employeeName, Grosswage grosswage, Netwage netwage) {
        double gross = grosswage.calculate();
        double regularHours = grosswage.getRegularHours();
        double overtimeHours = grosswage.getOvertimeHours();
        double regularPay = grosswage.getRegularPay();
        double overtimePay = grosswage.getOvertimePay();
        double holidayPay = grosswage.getHolidayPay();

        // Get weekly deductions
        double sssDeduction = netwage.getSSSDeduction();
        double philhealthDeduction = netwage.getPhilhealthDeduction();
        double pagibigDeduction = netwage.getPagIbigDeduction();
        double lateDeduction = netwage.getLateDeduction();
        
        double totalDeductions = netwage.getTotalDeductions();
        double taxableIncome = netwage.getTaxableIncome();
        double withholdingTax = netwage.getWithholdingTax();
        double netWage = gross - totalDeductions - withholdingTax;

        System.out.println("\nWeek " + week + " Payroll Details " + month + "/" + year + ":");
        System.out.println("------------------------------------------");
        System.out.printf("%-20s: %s%n", "Employee ID", empId);
        System.out.printf("%-20s: %s%n", "Employee Name", employeeName);
        System.out.println("------------------------------------------");
        System.out.printf("%-20s: %s hrs%n", "Regular Hours", decimalFormat.format(regularHours));
        System.out.printf("%-20s: %s hrs%n", "Overtime Hours", decimalFormat.format(overtimeHours));
        System.out.printf("%-20s: PHP %s%n", "Regular Pay", decimalFormat.format(regularPay));
        System.out.printf("%-20s: PHP %s%n", "Overtime Pay", decimalFormat.format(overtimePay));
        System.out.printf("%-20s: PHP %s%n", "Holiday Premium Pay", decimalFormat.format(holidayPay));
        System.out.printf("%-20s: PHP %s%n", "Gross Wage", decimalFormat.format(gross));
        
        System.out.println("\nDeductions:");
        System.out.printf("%-20s: PHP %s%n", "SSS", decimalFormat.format(sssDeduction));
        System.out.printf("%-20s: PHP %s%n", "PhilHealth", decimalFormat.format(philhealthDeduction));
        System.out.printf("%-20s: PHP %s%n", "Pag-IBIG", decimalFormat.format(pagibigDeduction));
        System.out.printf("%-20s: PHP %s%n", "Late Penalties", decimalFormat.format(lateDeduction));
        System.out.printf("%-20s: PHP %s%n", "Total Deductions", decimalFormat.format(totalDeductions));
        System.out.printf("%-20s: PHP %s%n", "Taxable Income", decimalFormat.format(taxableIncome));
        System.out.printf("%-20s: PHP %s%n", "Withholding Tax", decimalFormat.format(withholdingTax));
        System.out.println("------------------------------------------");
        System.out.printf("%-20s: PHP %s%n", "NET WAGE", decimalFormat.format(netWage));
        System.out.println("------------------------------------------");
    }

    private static int getYear() {
        System.out.print("Enter Year (YYYY): ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid year. Enter Year (YYYY): ");
            scanner.next();
        }
        return scanner.nextInt();
    }

    private static int getMonth() {
        int month;
        while (true) {
            System.out.print("Enter Month (1-12): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter Month (1-12): ");
                scanner.next();
            }
            month = scanner.nextInt();
            if (month >= 1 && month <= 12) {
                break;
            }
            System.out.print("Month must be between 1-12. Try again: ");
        }
        return month;
    }

    private static int getWeek() {
        int week;
        while (true) {
            System.out.print("Enter Week (1-4): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Enter Week (1-4): ");
                scanner.next();
            }
            week = scanner.nextInt();
            if (week >= 1 && week <= 4) {
                break;
            }
            System.out.print("Week must be between 1-4. Try again: ");
        }
        return week;
    }
}