package com.runova.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final ThreadLocal<SimpleDateFormat> ISO_DATE =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd", Locale.US));

    // Calculate age from ISO date string
    public static int ageFrom(String dateOfBirth) {
        try {
            Date dob = ISO_DATE.get().parse(dateOfBirth);
            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dob);
            
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            
            // Adjust if birthday hasn't occurred this year
            if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            
            return age;
        } catch (ParseException e) {
            return 0;
        }
    }

    // Derive ISO date of birth so that the user's age is exactly the given age today
    public static String dobFromAge(int age) {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR) - age;
        return String.format("%04d-%02d-%02d", year,
            cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }

    // Get weekday (1=Monday, 7=Sunday)
    public static int getWeekday(String date) {
        try {
            Date d = ISO_DATE.get().parse(date);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            int day = cal.get(Calendar.DAY_OF_WEEK);
            // Convert: Sunday=1 in Calendar -> 7 for us, Monday=2 -> 1
            return day == Calendar.SUNDAY ? 7 : day - 1;
        } catch (ParseException e) {
            return 1; // Default Monday
        }
    }

    // Get today's date as ISO string
    public static String today() {
        return ISO_DATE.get().format(new Date());
    }

    // Validate ISO date format
    public static boolean isValidDate(String date) {
        try {
            ISO_DATE.get().parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Check if date is in future
    public static boolean isFuture(String date) {
        try {
            Date d = ISO_DATE.get().parse(date);
            return d.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    // Days between two dates
    public static long daysBetween(String startDate, String endDate) {
        try {
            Date start = ISO_DATE.get().parse(startDate);
            Date end = ISO_DATE.get().parse(endDate);
            long diff = end.getTime() - start.getTime();
            return diff / (1000 * 60 * 60 * 24);
        } catch (ParseException e) {
            return 0;
        }
    }
}
