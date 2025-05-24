package com.mycompany.motorph;

import java.text.DecimalFormat;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/**
 * Class representing an Employee with personal and employment details.
 */
public class Employee {
    private final String employeeNumber;
    private final String lastName;
    private final String firstName;
    private final String birthday;
    private final String address;
    private final String phoneNumber;
    private final String sssNumber;
    private final String philhealthNumber;
    private final String tinNumber;
    private final String pagIbigNumber;
    private final String status;
    private final String position;
    private final String immediateSupervisor;
    private final String basicSalary;
    private final String riceSubsidy;
    private final String phoneAllowance;
    private final String clothingAllowance;
    private final String grossSemiMonthlyRate;
    private final double hourlyRate;
    private final LocalTime shiftStartTime;
    private final boolean nightShift;

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private static final LocalTime DEFAULT_SHIFT_START = LocalTime.of(8, 0);

    public Employee(String[] data) {
        if (data == null || data.length < 19) {
            throw new IllegalArgumentException("Insufficient data to create Employee object");
        }

        this.employeeNumber = parseEmployeeNumber(data[0]);
        this.lastName = getValue(data, 1);
        this.firstName = getValue(data, 2);
        this.birthday = getValue(data, 3);
        this.address = getValue(data, 4);
        this.phoneNumber = getValue(data, 5);
        this.sssNumber = getValue(data, 6);
        this.philhealthNumber = getValue(data, 7);
        this.tinNumber = getValue(data, 8);
        this.pagIbigNumber = getValue(data, 9);
        this.status = getValue(data, 10);
        this.position = getValue(data, 11);
        this.immediateSupervisor = getValue(data, 12);
        this.basicSalary = getValue(data, 13);
        this.riceSubsidy = getValue(data, 14);
        this.phoneAllowance = getValue(data, 15);
        this.clothingAllowance = getValue(data, 16);
        this.grossSemiMonthlyRate = getValue(data, 17);
        this.hourlyRate = parseDoubleValue(data[18]);
        this.shiftStartTime = parseShiftStartTime(getValue(data, 19));
        this.nightShift = parseNightShift(getValue(data, 20));
    }

    private String parseEmployeeNumber(String empNum) {
        try {
            return String.valueOf((int) Double.parseDouble(empNum));
        } catch (NumberFormatException e) {
            System.err.println("Invalid employee number format: " + empNum);
            return empNum;
        }
    }

    private String getValue(String[] data, int index) {
        return (index < data.length) ? data[index].trim() : "";
    }

    private double parseDoubleValue(String value) {
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format: " + value);
            return 0.0;
        }
    }

    private LocalTime parseShiftStartTime(String timeString) {
        try {
            return timeString.isEmpty() ? DEFAULT_SHIFT_START : LocalTime.parse(timeString);
        } catch (DateTimeParseException e) {
            System.err.println("Invalid time format: " + timeString);
            return DEFAULT_SHIFT_START;
        }
    }

    private boolean parseNightShift(String value) {
        return "true".equalsIgnoreCase(value);
    }

    // All getters
    public String getEmployeeNumber() { return employeeNumber; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public String getBirthday() { return birthday; }
    public String getAddress() { return address; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getSssNumber() { return sssNumber; }
    public String getPhilhealthNumber() { return philhealthNumber; }
    public String getTinNumber() { return tinNumber; }
    public String getPagIbigNumber() { return pagIbigNumber; }
    public String getStatus() { return status; }
    public String getPosition() { return position; }
    public String getImmediateSupervisor() { return immediateSupervisor; }
    public String getBasicSalary() { return basicSalary; }
    public String getRiceSubsidy() { return riceSubsidy; }
    public String getPhoneAllowance() { return phoneAllowance; }
    public String getClothingAllowance() { return clothingAllowance; }
    public String getGrossSemiMonthlyRate() { return grossSemiMonthlyRate; }
    public double getHourlyRate() { return hourlyRate; }
    public LocalTime getShiftStartTime() { return shiftStartTime; }
    public boolean isNightShift() { return nightShift; }

    @Override
    public String toString() {
        return toString(true); // Default to detailed view
    }

    /**
     * Returns a string representation of the employee
     * @param detailed If true, returns full details; if false, returns basic info
     * @return Formatted employee information
     */
    public String toString(boolean detailed) {
        if (detailed) {
            return String.format("""
                Employee ID: %s
                Name: %s, %s
                Birthday: %s
                Address: %s
                Phone: %s
                SSS: %s
                PhilHealth: %s
                TIN: %s
                Pag-IBIG: %s
                Position: %s
                Status: %s
                Supervisor: %s
                Basic Salary: %s
                Hourly Rate: %.2f
                Allowances:
                  Rice: %s
                  Phone: %s
                  Clothing: %s
                Gross Semi-Monthly: %s
                Shift: %s %s
                """,
                employeeNumber,
                lastName, firstName,
                birthday,
                address,
                phoneNumber,
                sssNumber,
                philhealthNumber,
                tinNumber,
                pagIbigNumber,
                position,
                status,
                immediateSupervisor,
                basicSalary,
                hourlyRate,
                riceSubsidy,
                phoneAllowance,
                clothingAllowance,
                grossSemiMonthlyRate,
                shiftStartTime,
                nightShift ? "(Night Shift)" : ""
            );
        } else {
            return String.format("%s %s (ID: %s, Position: %s)", 
                firstName, lastName, employeeNumber, position);
        }
    }
}
