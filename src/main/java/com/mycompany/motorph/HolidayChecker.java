package com.mycompany.motorph;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;
import java.util.HashSet;

/**
 * Philippine holidays for 2024 only
 * Regular holidays = 200% pay
 * Special non-working days = 130% pay
 */
public class HolidayChecker {
    private static final Set<LocalDate> REGULAR_HOLIDAYS = new HashSet<>();
    private static final Set<LocalDate> SPECIAL_NON_WORKING_DAYS = new HashSet<>();

    static {
        // REGULAR HOLIDAYS (200% pay)
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.JANUARY, 1));   // New Year's Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.APRIL, 9));     // Araw ng Kagitingan
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.APRIL, 10));    // Eid'l Fitr
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.MAY, 1));       // Labor Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.JUNE, 12));     // Independence Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.JUNE, 17));     // Eid'l Adha
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.AUGUST, 26));   // National Heroes Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.NOVEMBER, 30)); // Bonifacio Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.DECEMBER, 25)); // Christmas Day
        REGULAR_HOLIDAYS.add(LocalDate.of(2024, Month.DECEMBER, 30)); // Rizal Day

        // SPECIAL NON-WORKING DAYS (130% pay)
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.FEBRUARY, 10)); // Chinese New Year
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.MARCH, 28));    // Maundy Thursday
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.MARCH, 29));    // Good Friday
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.MARCH, 30));    // Black Saturday
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.AUGUST, 21));   // Ninoy Aquino Day
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.NOVEMBER, 1));  // All Saints' Day
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.DECEMBER, 8));  // Immaculate Conception
        SPECIAL_NON_WORKING_DAYS.add(LocalDate.of(2024, Month.DECEMBER, 31)); // New Year's Eve
    }

    public static boolean isRegularHoliday(LocalDate date) {
        return REGULAR_HOLIDAYS.contains(date);
    }

    public static boolean isSpecialNonWorkingDay(LocalDate date) {
        return SPECIAL_NON_WORKING_DAYS.contains(date);
    }

    public static boolean isHoliday(LocalDate date) {
        return isRegularHoliday(date) || isSpecialNonWorkingDay(date);
    }

    /**
     * @return 2.0 for regular holidays, 1.3 for special days, 1.0 otherwise
     */
    public static double getHolidayPayMultiplier(LocalDate date) {
        if (isRegularHoliday(date)) return 2.0;
        if (isSpecialNonWorkingDay(date)) return 1.3;
        return 1.0;
    }
}