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
        Date todayWithZeroTime = getDateZero(date);
        if (dateToLoginData.containsKey(todayWithZeroTime)) {
            DateLogData dateLogData = dateToLoginData.get(todayWithZeroTime);
            dateLogData.setTotalTime(totalTime);
        } else {
            dateToLoginData.put(todayWithZeroTime, new DateLogData(new ArrayList<LogEvent>(), date, totalTime));
        }
    }

    public void updateLoginEvent(Date date, LogEvent logEvent) {
        System.out.println("updating log event " + logEvent);
        DateLogData dataForDate = getDataForDate(date);
        dataForDate.addLogEvent(logEvent);
    }

    public DateLogData getDataForDate(Date date) {
        Date todayWithZeroTime = getDateZero(date);
        return dateToLoginData.get(todayWithZeroTime);
    }

    private Date getDateZero(Date date) {
        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            return formatter.parse(formatter.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "AllLoginData{" +
                "dateToLoginData=" + dateToLoginData +
                '}';
    }
}
