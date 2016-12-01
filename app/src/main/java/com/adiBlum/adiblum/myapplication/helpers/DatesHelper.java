package com.adiBlum.adiblum.myapplication.helpers;

import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class DatesHelper {

    public static Date getFirstDateOfTheMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        // get start of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static String getMonthName() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
        return sdf.format(new Date());
    }

    public static String getMonthName(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
        return sdf.format(date);
    }

    public static Date getDateFromString(String s) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return simpleDateFormat.parse(s);
    }

    public static Date getOldestDay(Map<String, Double> data) {
        if (data.isEmpty()) {
            return null;
        } else {
            return findOldestDateInList(data);
        }

    }

    @Nullable
    private static Date findOldestDateInList(Map<String, Double> data) {
        Date oldest;
        Iterator<String> iterator = data.keySet().iterator();
        String string = iterator.next();
        try {
            oldest = getDateFromString(string);
            for (String s : data.keySet()) {
                Date newDate = getDateFromString(s);
                if (newDate.before(oldest)) {
                    oldest = newDate;
                }
            }
            return oldest;
        } catch (ParseException e) {
            return null;
        }
    }

    public static int getDayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static Date getLastDayOfMonth(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        return c.getTime();
    }
}
