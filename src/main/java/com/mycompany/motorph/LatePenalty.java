package com.mycompany.motorph;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.time.DateTimeException;

public class LatePenalty extends Calculation {
    private final String targetEmployeeID;
    private final int targetMonth;
    private final int targetYear;
    private final int week;  // Added week field (1-4)
    private final double hourlyRate;
    private final LocalTime shiftStartTime;
    
    private static final int GRACE_PERIOD_MINUTES = 15;
    private static final LocalTime SHIFT_8AM = LocalTime.of(8, 0);
    private static final LocalTime SHIFT_9AM = LocalTime.of(9, 0);
    private static final LocalTime SHIFT_10AM = LocalTime.of(10, 0);

    // Updated enum to include WEEKLY
    public enum PayrollCycle {
        WEEKLY, FIRST_HALF, SECOND_HALF
    }

    public LatePenalty(String targetEmployeeID, int targetMonth, int targetYear, 
                      int week, double hourlyRate, LocalTime shiftStartTime) {
        if (targetEmployeeID == null || targetEmployeeID.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be null or empty");
        }
        if (targetMonth < 1 || targetMonth > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (targetYear < 2000 || targetYear > LocalDate.now().getYear() + 1) {
            throw new IllegalArgumentException("Invalid year");
        }
        if (week < 1 || week > 4) {
            throw new IllegalArgumentException("Week must be between 1-4");
        }
        if (hourlyRate <= 0) {
            throw new IllegalArgumentException("Hourly rate must be positive");
        }
        if (shiftStartTime == null) {
            throw new IllegalArgumentException("Shift start time cannot be null");
        }
        // Validate shift start time
        if (!shiftStartTime.equals(SHIFT_8AM) && 
            !shiftStartTime.equals(SHIFT_9AM) && 
            !shiftStartTime.equals(SHIFT_10AM)) {
            throw new IllegalArgumentException("Shift must be exactly 8:00, 9:00, or 10:00 AM");
        }

        this.targetEmployeeID = targetEmployeeID;
        this.targetMonth = targetMonth;
        this.targetYear = targetYear;
        this.week = week;
        this.hourlyRate = hourlyRate;
        this.shiftStartTime = shiftStartTime;
    }

    @Override
    public double calculate() {
        try {
            YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
            LocalDate startDate = calculateWeekStartDate(yearMonth);
            LocalDate endDate = calculateWeekEndDate(yearMonth);
            
            double totalLateDeduction = 0;
            List<AttendanceRecord> attendanceRecords = AttendanceRecord.getAttendanceRecords();

            for (AttendanceRecord record : attendanceRecords) {
                if (record != null && record.getId().equals(targetEmployeeID)) {
                    LocalDate recordDate = record.getDate();
                    if (recordDate != null && 
                        !recordDate.isBefore(startDate) && 
                        !recordDate.isAfter(endDate)) {
                        
                        LocalTime timeIn = record.getTimeIn();
                        if (timeIn != null) {
                            LocalTime lateThreshold = shiftStartTime.plusMinutes(GRACE_PERIOD_MINUTES);
                            if (timeIn.isAfter(lateThreshold)) {
                                long minutesLate = java.time.Duration.between(lateThreshold, timeIn).toMinutes();
                                double deduction = (hourlyRate / 60.0) * minutesLate;
                                totalLateDeduction += Math.max(0, deduction);
                            }
                        }
                    }
                }
            }
            return totalLateDeduction;
        } catch (DateTimeException e) {
            throw new IllegalStateException("Failed to calculate late penalty: " + e.getMessage(), e);
        }
    }

    private LocalDate calculateWeekStartDate(YearMonth yearMonth) {
        int startDay = 1 + (week - 1) * 7;
        return yearMonth.atDay(Math.min(startDay, yearMonth.lengthOfMonth()));
    }

    private LocalDate calculateWeekEndDate(YearMonth yearMonth) {
        int endDay = week * 7;
        return yearMonth.atDay(Math.min(endDay, yearMonth.lengthOfMonth()));
    }

    // Getters
    public String getTargetEmployeeID() { return targetEmployeeID; }
    public int getTargetMonth() { return targetMonth; }
    public int getTargetYear() { return targetYear; }
    public int getWeek() { return week; }
    public double getHourlyRate() { return hourlyRate; }
    public LocalTime getShiftStartTime() { return shiftStartTime; }
}