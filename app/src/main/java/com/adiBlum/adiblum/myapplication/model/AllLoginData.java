package com.adiBlum.adiblum.myapplication.model;


import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AllLoginData implements Serializable {
    private Map<Date, DateLogData> dateToLoginData = new HashMap<>();

    public Map<Date, DateLogData> getDateToLoginData() {
        return dateToLoginData;
    }

    public void updateTimeAtPlace(Date date, double totalTime) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date todayWithZeroTime = null;
        try {
            todayWithZeroTime = formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dateToLoginData.containsKey(todayWithZeroTime)) {
            DateLogData dateLogData = dateToLoginData.get(todayWithZeroTime);
            dateLogData.setTotalTime(totalTime);
        } else {
            dateToLoginData.put(todayWithZeroTime, new DateLogData(new ArrayList<LogEvent>(), date, totalTime));
        }
    }

    public DateLogData getDataForDate(Date date) {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date todayWithZeroTime = null;
        try {
            todayWithZeroTime = formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        date.setHours(0);
        date.setMinutes(0);
        date.setSeconds(0);
        return dateToLoginData.get(todayWithZeroTime);
    }

    @Override
    public String toString() {
        return "AllLoginData{" +
                "dateToLoginData=" + dateToLoginData +
                '}';
    }
}
