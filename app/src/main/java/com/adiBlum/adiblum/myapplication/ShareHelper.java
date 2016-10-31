package com.adiBlum.adiblum.myapplication;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ShareHelper {

    private static final String COMMA_DELIMITER = ": ";
    private static final String NEW_LINE_SEPARATOR = "\n";

    public static String createMonthly(Map<String, Double> datesToHours, Date startDate, Date endDate) {
        String result = "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        double totalTime = 0;
        int workingDays = 0;
        while (startDate.before(endDate) || SaveDataHelper.isSameDay(startDate, endDate)) {
            String stringDate = SaveDataHelper.getStringDate(startDate);
            if (datesToHours.containsKey(stringDate)) {
                Double timeAtWork = datesToHours.get(stringDate);
                String formattedTimeAtWork = SaveDataHelper.getPrettyTimeString(timeAtWork);
                if (timeAtWork == -1) {
                    formattedTimeAtWork = "No data";
                }
                if (timeAtWork == 0) {
                    formattedTimeAtWork = "Out of office";
                } else {
                    if (timeAtWork > 0) {
                        totalTime += timeAtWork;
                        workingDays++;
                    }
                }
                result += NEW_LINE_SEPARATOR + stringDate + COMMA_DELIMITER + formattedTimeAtWork;
            }
            startDate = incDate(cal);
        }

        String totalString = "Total Monthly: " + workingDays + " working days. Total time: " + SaveDataHelper.getPrettyTimeString(totalTime) + NEW_LINE_SEPARATOR;
        result = totalString + result;

        return result;
    }

    private static Date incDate(Calendar cal) {
        Date startDate;
        cal.add(Calendar.DAY_OF_YEAR, 1);
        startDate = cal.getTime();
        return startDate;
    }
}
