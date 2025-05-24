package com.mycompany.motorph;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Netwage extends Calculation {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private final Grosswage grosswage;
    private final String employeeID;
    private final String employeeName;
    private final double gross;
    private final double hours;
    private final int week;
    private final int targetMonth;
    private final int targetYear;
    
    // Cached calculations
    private Double sssDeduction;
    private Double philhealthDeduction;
    private Double pagibigDeduction;
    private Double lateDeduction;
    private Double withholdingTax;

    public Netwage(String employeeID, String employeeName, double gross, double hours, 
                  int week, Grosswage grosswage, int targetMonth, int targetYear) {
        if (employeeID == null || employeeID.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        if (employeeName == null || employeeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be null or empty");
        }
        if (gross < 0) {
            throw new IllegalArgumentException("Gross wage cannot be negative");
        }
        if (hours < 0) {
            throw new IllegalArgumentException("Hours cannot be negative");
        }
        if (grosswage == null) {
            throw new IllegalArgumentException("Grosswage cannot be null");
        }
        if (targetMonth < 1 || targetMonth > 12) {
            throw new IllegalArgumentException("Month must be between 1-12");
        }
        if (week < 1 || week > 4) {
            throw new IllegalArgumentException("Week must be between 1-4");
        }
        if (targetYear < 2000 || targetYear > LocalDate.now().getYear() + 1) {
            throw new IllegalArgumentException("Invalid year");
        }

        this.employeeID = employeeID;
        this.employeeName = employeeName;
        this.gross = gross;
        this.hours = hours;
        this.week = week;
        this.grosswage = grosswage;
        this.targetMonth = targetMonth;
        this.targetYear = targetYear;
    }

    @Override
    public double calculate() {
        double totalDeductions = getTotalDeductions();
        double withholdingTax = getWithholdingTax();
        double netWage = gross - totalDeductions - withholdingTax;
        return Double.parseDouble(decimalFormat.format(netWage));
    }

    public double getSSSDeduction() {
        if (sssDeduction == null) {
            Calculation sss = new SSS(grosswage);
            sssDeduction = sss.calculate() / 4; // Weekly calculation (monthly/4)
        }
        return sssDeduction;
    }

    public double getPhilhealthDeduction() {
        if (philhealthDeduction == null) {
            Calculation philhealth = new Philhealth(grosswage);
            philhealthDeduction = philhealth.calculate() / 4; // Weekly calculation
        }
        return philhealthDeduction;
    }

    public double getPagIbigDeduction() {
        if (pagibigDeduction == null) {
            Calculation pagibig = new Pagibig(grosswage);
            pagibigDeduction = pagibig.calculate() / 4; // Weekly calculation
        }
        return pagibigDeduction;
    }

    public double getLateDeduction() {
        if (lateDeduction == null) {
            lateDeduction = calculateWeeklyLatePenalty();
        }
        return lateDeduction;
    }

    private double calculateWeeklyLatePenalty() {
        List<AttendanceRecord> records = AttendanceRecord.getAttendanceRecords();
        double totalPenalty = 0.0;
        final double minuteRate = grosswage.getHourlyRate() / 60.0;
        final LocalTime shiftStart = grosswage.getShiftStartTime();
        final LocalTime lateThreshold = shiftStart.plusMinutes(15); // Using direct value instead of constant

        for (AttendanceRecord record : records) {
            if (record.getId().equals(employeeID) && isDateInTargetWeek(record.getDate())) {
                LocalTime timeIn = record.getTimeIn();
                if (timeIn != null && timeIn.isAfter(lateThreshold)) {
                    long minutesLate = java.time.Duration.between(lateThreshold, timeIn).toMinutes();
                    totalPenalty += minuteRate * minutesLate;
                }
            }
        }
        return totalPenalty;
    }

    private boolean isDateInTargetWeek(LocalDate date) {
        if (date.getYear() != targetYear || date.getMonthValue() != targetMonth) {
            return false;
        }
        int weekOfMonth = ((date.getDayOfMonth() - 1) / 7) + 1;
        return weekOfMonth == week;
    }

    public double getTotalDeductions() {
        return getSSSDeduction() + 
               getPhilhealthDeduction() + 
               getPagIbigDeduction() + 
               getLateDeduction();
    }

    public double getTaxableIncome() {
        return gross - getTotalDeductions();
    }

    public double getWithholdingTax() {
        if (withholdingTax == null) {
            double taxableIncome = getTaxableIncome();
            WithholdingTax withholdingTaxCalc = new WithholdingTax(grosswage, taxableIncome);
            withholdingTax = withholdingTaxCalc.calculate();
        }
        return withholdingTax;
    }

    // Getters
    public Grosswage getGrosswage() { return grosswage; }
    public String getEmployeeID() { return employeeID; }
    public String getEmployeeName() { return employeeName; }
    public double getGross() { return gross; }
    public double getHours() { return hours; }
    public int getWeek() { return week; }
    public int getTargetMonth() { return targetMonth; }
    public int getTargetYear() { return targetYear; }
}