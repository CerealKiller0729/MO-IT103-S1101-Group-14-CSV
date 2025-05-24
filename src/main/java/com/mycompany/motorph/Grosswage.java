package com.mycompany.motorph;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.List;

public class Grosswage extends Calculation {
    private final String employeeID;
    private final String employeeName;
    private double gross;
    private double hourlyRate;
    private double hoursWorked;
    private final int year;
    private final int month;
    private final int week;
    private final LocalTime shiftStartTime;
    private final boolean nightShift;

    // Detailed breakdown fields
    private double regularHours;
    private double overtimeHours;
    private double regularPay;
    private double overtimePay;
    private double holidayPay;

    public Grosswage(String empId, String firstName, String lastName, int year, 
                   int month, int week, LocalTime shiftStartTime, boolean nightShift) {
        if (empId == null || empId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        if (firstName == null || lastName == null) {
            throw new IllegalArgumentException("Employee name cannot be null");
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1-12");
        }
        if (week < 1 || week > 5) {
            throw new IllegalArgumentException("Week must be between 1-5");
        }
        if (shiftStartTime == null) {
            throw new IllegalArgumentException("Shift start time cannot be null");
        }

        this.employeeID = empId;
        this.employeeName = firstName + " " + lastName;
        this.year = year;
        this.month = month;
        this.week = week;
        this.shiftStartTime = shiftStartTime;
        this.nightShift = nightShift;
    }

    @Override
    public double calculate() {
        initializeAttendanceRecords();
        Employee employee = getEmployeeData();
        this.hourlyRate = employee.getHourlyRate();
        
        calculateWeeklyHoursAndPay();
        this.gross = regularPay + overtimePay;
        
        validateHolidayPay();
        return gross;
    }

    private void initializeAttendanceRecords() {
        try {
            if (AttendanceRecord.getAttendanceRecords().isEmpty()) {
                System.out.println("Loading attendance records...");
                AttendanceRecord.loadAttendanceFromCSV("src/main/resources/AttendanceRecord.csv");
            }
            
            System.out.println("Total records available: " + 
                AttendanceRecord.getAttendanceRecords().size());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize attendance records: " + e.getMessage(), e);
        }
    }

    private Employee getEmployeeData() {
        List<Employee> employees = EmployeeModelFromFile.getEmployeeModelList();
        Employee employee = findEmployeeById(employeeID, employees);
        
        if (employee == null) {
            throw new IllegalStateException("Employee ID " + employeeID + " not found");
        }
        if (employee.getHourlyRate() <= 0) {
            throw new IllegalStateException("Invalid hourly rate for employee");
        }
        
        return employee;
    }

    private void calculateWeeklyHoursAndPay() {
        resetCounters();
        List<AttendanceRecord> records = AttendanceRecord.getAttendanceRecords();
        
        if (records.isEmpty()) {
            throw new IllegalStateException("No attendance records available");
        }
        
        System.out.println("Processing records for employee: " + employeeID);
        System.out.println("Target period: " + year + "-" + month + " week " + week);
        
        int matchingRecords = 0;
        
        for (AttendanceRecord record : records) {
            if (record.getId().equals(employeeID)) {
                if (AttendanceRecord.isDateInTargetWeek(record.getDate(), year, month, week)) {
                    matchingRecords++;
                    processDailyRecord(record);
                }
            }
        }
        
        System.out.println("Found " + matchingRecords + " matching records");
        
        this.hoursWorked = regularHours + overtimeHours;
        
        if (hoursWorked <= 0) {
            throw new IllegalStateException("No hours worked found for employee " + employeeID + 
                " in " + year + "-" + month + " week " + week);
        }
    }

    private void processDailyRecord(AttendanceRecord record) {
        LocalDate recordDate = record.getDate();
        double[] workHours = record.calculateWorkHours();
        double dayRegular = workHours[0];
        double dayOvertime = workHours[1];

        if (HolidayChecker.isHoliday(recordDate)) {
            applyHolidayRates(recordDate, dayRegular, dayOvertime);
        } else {
            applyRegularRates(dayRegular, dayOvertime);
        }
    }

    private void resetCounters() {
        regularHours = 0;
        overtimeHours = 0;
        regularPay = 0;
        overtimePay = 0;
        holidayPay = 0;
        hoursWorked = 0;
    }

    private void applyHolidayRates(LocalDate date, double regularHrs, double overtimeHrs) {
        double multiplier = HolidayChecker.getHolidayPayMultiplier(date);
        double holidayPremiumRate = multiplier - 1.0;
        
        regularPay += regularHrs * hourlyRate * multiplier;
        holidayPay += regularHrs * hourlyRate * holidayPremiumRate;
        
        if (overtimeHrs > 0) {
            double overtimeRate = nightShift ? 1.10 : 1.25;
            double baseOvertime = overtimeHrs * hourlyRate * overtimeRate;
            double overtimePremium = overtimeHrs * hourlyRate * holidayPremiumRate;
            
            overtimePay += baseOvertime;
            holidayPay += overtimePremium;
        }

        regularHours += regularHrs;
        overtimeHours += overtimeHrs;
    }

    private void applyRegularRates(double regularHrs, double overtimeHrs) {
        regularPay += regularHrs * hourlyRate;
        
        if (overtimeHrs > 0) {
            double overtimeMultiplier = nightShift ? 1.10 : 1.25;
            overtimePay += overtimeHrs * hourlyRate * overtimeMultiplier;
        }
        
        regularHours += regularHrs;
        overtimeHours += overtimeHrs;
    }

    private void validateHolidayPay() {
        double maxExpectedPremium = (regularHours + overtimeHours) * hourlyRate * 1.3;
        if (holidayPay > maxExpectedPremium) {
            throw new IllegalStateException(
                String.format("Holiday pay %.2f exceeds reasonable maximum (%.2f)", 
                holidayPay, maxExpectedPremium));
        }
    }

    // Getters
    public double getRegularHours() { return regularHours; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getRegularPay() { return regularPay; }
    public double getOvertimePay() { return overtimePay; }
    public double getHolidayPay() { return holidayPay; }
    public String getEmployeeID() { return employeeID; }
    public String getEmployeeName() { return employeeName; }
    public double getHourlyRate() { return hourlyRate; }
    public double getHoursWorked() { return hoursWorked; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public int getWeek() { return week; }
    public LocalTime getShiftStartTime() { return shiftStartTime; }
    public boolean isNightShift() { return nightShift; }

    private Employee findEmployeeById(String employeeId, List<Employee> employees) {
        return employees.stream()
            .filter(e -> e != null && e.getEmployeeNumber().equals(employeeId))
            .findFirst()
            .orElse(null);
    }
    
    public void printCalculationDetails() {
        System.out.println("\nPayroll Calculation Details");
        System.out.println("==========================");
        System.out.printf("Employee: %s (%s)%n", employeeName, employeeID);
        System.out.printf("Pay Period: %d-%02d (Week %d)%n", year, month, week);
        System.out.printf("Shift: %s %s%n", 
            shiftStartTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
            nightShift ? "(Night Shift)" : "(Day Shift)");
        System.out.println("--------------------------");
        System.out.printf("Hourly Rate: PHP %.2f%n", hourlyRate);
        System.out.printf("Regular Hours: %.2f (PHP %.2f)%n", regularHours, regularPay);
        System.out.printf("Overtime Hours: %.2f (PHP %.2f)%n", overtimeHours, overtimePay);
        if (holidayPay > 0) {
            System.out.printf("Holiday Premium Pay: PHP %.2f%n", holidayPay);
        }
        System.out.println("--------------------------");
        System.out.printf("Total Hours Worked: %.2f%n", hoursWorked);
        System.out.printf("Total Gross Wage: PHP %.2f%n", gross);
        System.out.println("==========================");
    }
}