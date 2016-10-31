package com.adiBlum.adiblum.myapplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
}
