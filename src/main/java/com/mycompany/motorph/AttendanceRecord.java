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

public class AttendanceRecord {
    private String name;
    private String id;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private static final String CSV_FILE_PATH = "src/main/resources/AttendanceRecord.csv";
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    
    // Date formats
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    };
    
    // Time formats - now includes all possible variations
    private static final DateTimeFormatter[] TIME_FORMATTERS = {
        DateTimeFormatter.ofPattern("H:mm"),    // 8:59
        DateTimeFormatter.ofPattern("HH:mm"),   // 08:59
        DateTimeFormatter.ofPattern("H:mm:ss"), // 8:59:00
        DateTimeFormatter.ofPattern("HH:mm:ss") // 08:59:00
    };

    public static ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();

    // Constructors remain the same
    public AttendanceRecord(String name, String id, LocalDate date, LocalTime timeIn, LocalTime timeOut) {
        this.name = name;
        this.id = id;
        this.date = date;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
    }

    public AttendanceRecord(String[] data) {
        if (data.length < 6) {
            throw new IllegalArgumentException("Insufficient data to create AttendanceRecord");
        }
        this.id = data[0];
        this.name = data[1] + " " + data[2].trim();
        this.date = parseDate(data[3]);
        this.timeIn = parseTime(data[4]);
        this.timeOut = parseTime(data[5]);
        
        if (this.date == null || this.timeIn == null || this.timeOut == null) {
            throw new IllegalArgumentException("Invalid date/time values in record");
        }
    }

    public static void loadAttendanceFromCSV(String filePath) {
        try {
            attendanceRecords = loadAttendance(filePath);
            System.out.println("Loaded " + attendanceRecords.size() + " attendance records.");
        } catch (IOException e) {
            System.err.println("Error loading attendance records: " + e.getMessage());
        }
    }

    public static ArrayList<AttendanceRecord> loadAttendance(String filePath) throws IOException {
        ArrayList<AttendanceRecord> attendanceRecords = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // Skip header row
            br.readLine();
            
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // Handles quoted values
                    if (values.length < 6) {
                        System.out.println("Skipping incomplete record: " + line);
                        continue;
                    }

                    String id = cleanId(values[0]);
                    String name = cleanName(values[1]);
                    String surname = cleanName(values[2]);
                    LocalDate date = parseDate(cleanValue(values[3]));
                    LocalTime timeIn = parseTime(cleanValue(values[4]));
                    LocalTime timeOut = parseTime(cleanValue(values[5]));

                    if (date != null && timeIn != null && timeOut != null) {
                        attendanceRecords.add(new AttendanceRecord(
                            name + " " + surname, id, date, timeIn, timeOut));
                    } else {
                        System.out.println("Skipping record with invalid date/time: " + line);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing record: " + line + " - " + e.getMessage());
                }
            }
        }

        return attendanceRecords;
    }

    // Helper methods for cleaning values
    private static String cleanId(String id) {
        return id.replace(".0", "").replace("\"", "").trim();
    }

    private static String cleanName(String name) {
        return name.replace("\"", "").trim();
    }

    private static String cleanValue(String value) {
        return value.replace("\"", "").trim();
    }

    // Improved date parsing
    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        
        dateString = cleanValue(dateString);
        
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

    // Robust time parsing that handles all formats
    private static LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return null;
        }
        
        timeString = cleanValue(timeString);
        
        // First try standard formats
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalTime.parse(timeString, formatter);
            } catch (DateTimeParseException e) {
                // Try next format
            }
        }
        
        // Fallback for times without leading zero (e.g., "8:59")
        try {
            String[] parts = timeString.split(":");
            if (parts.length == 2) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                    return LocalTime.of(hours, minutes);
                }
            }
        } catch (NumberFormatException e) {
            // Continue to error handling
        }
        
        System.err.println("Could not parse time: " + timeString);
        return null;
    }

    // Rest of the class remains the same
    public double calculateHoursWorked() {
        if (timeIn == null || timeOut == null) return 0.0;
        
        Duration duration = timeOut.isBefore(timeIn) ? 
            Duration.between(timeIn, timeOut.plusHours(24)) : 
            Duration.between(timeIn, timeOut);
            
        return duration.toHours() + (duration.toMinutes() % 60) / 60.0;
    }

    public static double calculateTotalHours(int year, int month, String employeeID, int week) {
        return attendanceRecords.stream()
            .filter(r -> r.getId().equals(employeeID))
            .filter(r -> r.getDate().getYear() == year && r.getDate().getMonthValue() == month)
            .filter(r -> getWeekOfMonth(r.getDate()) == week)
            .mapToDouble(AttendanceRecord::calculateHoursWorked)
            .sum();
    }

    public static int getWeekOfMonth(LocalDate date) {
        return ((date.getDayOfMonth() - 1) / 7) + 1;
    }

    // Getters
    public String getName() { return name; }
    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public static ArrayList<AttendanceRecord> getAttendanceRecords() { return attendanceRecords; }
}