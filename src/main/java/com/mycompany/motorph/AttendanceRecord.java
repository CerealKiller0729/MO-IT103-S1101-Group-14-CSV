package com.mycompany.motorph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttendanceRecord {
    private String name;
    private String id;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    
    // Work hour constants
    private static final double REGULAR_WORK_HOURS_PER_DAY = 8.0;
    private static final double OVERTIME_THRESHOLD = 8.0;
    
    // Date formats
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };
    
    // Time formats
    private static final DateTimeFormatter[] TIME_FORMATTERS = {
        DateTimeFormatter.ofPattern("H:mm"),    // 8:59
        DateTimeFormatter.ofPattern("HH:mm"),   // 08:59
        DateTimeFormatter.ofPattern("H:mm:ss"), // 8:59:00
        DateTimeFormatter.ofPattern("HH:mm:ss") // 08:59:00
    };

    private static final List<AttendanceRecord> attendanceRecords = new ArrayList<>();

    public AttendanceRecord(String name, String id, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.date = Objects.requireNonNull(date, "Date cannot be null");
        this.timeIn = Objects.requireNonNull(timeIn, "TimeIn cannot be null");
        this.timeOut = Objects.requireNonNull(timeOut, "TimeOut cannot be null");
        
        // Validate time sequence
        if (timeOut.isBefore(timeIn) && !timeOut.equals(LocalTime.MIDNIGHT)) {
            throw new IllegalArgumentException("TimeOut cannot be before TimeIn unless it's midnight");
        }
    }

    public static void loadAttendanceFromCSV(String filePath) throws IOException {
        attendanceRecords.clear(); // Clear existing records before loading new ones
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip header row
            br.readLine();
            
            String line;
            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                try {
                    String[] values = line.split(",");
                    if (values.length < 6) {
                        System.err.println("Skipping incomplete record at line " + lineNumber);
                        continue;
                    }

                    String id = cleanId(values[0]);
                    String lastName = cleanName(values[1]);
                    String firstName = cleanName(values[2]);
                    LocalDate date = parseDate(values[3]);
                    LocalTime timeIn = parseTime(values[4]);
                    LocalTime timeOut = parseTime(values[5]);

                    if (date != null && timeIn != null && timeOut != null) {
                        attendanceRecords.add(new AttendanceRecord(
                            firstName + " " + lastName, id, date, timeIn, timeOut));
                    }
                } catch (Exception e) {
                    System.err.println("Error processing record at line " + lineNumber + 
                                     ": " + e.getMessage());
                }
            }
        }
        
        if (attendanceRecords.isEmpty()) {
            throw new IOException("No valid attendance records found in file");
        }
    }

    public double[] calculateWorkHours() {
        Duration duration;
        
        // Handle overnight shifts (timeOut is next day)
        if (timeOut.isBefore(timeIn)) {
            duration = Duration.between(timeIn, timeOut.plusHours(24));
        } else {
            duration = Duration.between(timeIn, timeOut);
        }
        
        double totalHours = duration.toMinutes() / 60.0;
        double regularHours = Math.min(totalHours, REGULAR_WORK_HOURS_PER_DAY);
        double overtimeHours = Math.max(0, totalHours - REGULAR_WORK_HOURS_PER_DAY);
        
        return new double[]{regularHours, overtimeHours};
    }

    public static double[] calculateTotalWorkHours(String employeeID, int year, int month, int week) {
        double[] totals = new double[]{0.0, 0.0};
        
        for (AttendanceRecord record : attendanceRecords) {
            if (record.getId().equals(employeeID) && 
                isDateInTargetWeek(record.getDate(), year, month, week)) {
                
                double[] dailyHours = record.calculateWorkHours();
                totals[0] += dailyHours[0];
                totals[1] += dailyHours[1];
            }
        }
        
        return totals;
    }

    public static boolean isDateInTargetWeek(LocalDate date, int year, int month, int week) {
        if (date == null) return false;
        
        // First check year and month
        if (date.getYear() != year || date.getMonthValue() != month) {
            return false;
        }
        
        // Calculate adjusted week of month (considering proper week boundaries)
        int dayOfMonth = date.getDayOfMonth();
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Monday to 7=Sunday
        
        // Week starts on Monday
        int adjustedWeek = ((dayOfMonth - dayOfWeek + 10) / 7);
        return adjustedWeek == week;
    }

    // Helper methods for parsing and cleaning data
    private static String cleanId(String id) {
        return id.replace("\"", "").trim();
    }

    private static String cleanName(String name) {
        return name.replace("\"", "").trim();
    }

    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        
        dateString = dateString.replace("\"", "").trim();
        
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateString, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        System.err.println("Could not parse date: " + dateString);
        return null;
    }

    private static LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return null;
        }
        
        timeString = timeString.replace("\"", "").trim();
        
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(timeString, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        System.err.println("Could not parse time: " + timeString);
        return null;
    }

    // Getters
    public String getName() { return name; }
    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public static List<AttendanceRecord> getAttendanceRecords() { 
        return new ArrayList<>(attendanceRecords); 
    }

    @Override
    public String toString() {
        return String.format("%s (%s) %s %s-%s", name, id, date, timeIn, timeOut);
    }
}