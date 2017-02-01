package com.adiBlum.adiblum.myapplication.model;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class DateLogData implements Serializable {
    private List<LogEvent> logEvents;
    private Date date;
    private double totalTime;

    public DateLogData(List<LogEvent> logEvents, Date date, double totalTime) {
        this.logEvents = logEvents;
        this.date = date;
        this.totalTime = totalTime;
    }

    public DateLogData() {
    }

    public List<LogEvent> getLogEvents() {
        return logEvents;
    }

    public void setLogEvents(List<LogEvent> logEvents) {
        this.logEvents = logEvents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public void addLogEvent(LogEvent logEvent) {
        logEvents.add(logEvent);
    }
}
