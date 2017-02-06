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

    public long getFirstLogin() {
        long early = Long.MAX_VALUE;
        if (logEvents != null && logEvents.size() > 0) {
            for (LogEvent logEvent : logEvents) {
                if (logEvent.getState().equals(LoginState.IN)) {
                    if (logEvent.getTimestamp() < early) {
                        early = logEvent.getTimestamp();
                    }
                }
            }
        }
        if (early == Long.MAX_VALUE) {
            early = -1;
        }
        System.out.println("early: " + early);
        return early;
    }

    public long getLastLogout() {
        long latest = -1;
        if (logEvents != null && logEvents.size() > 0) {
            for (LogEvent logEvent : logEvents) {
                if (logEvent.getState().equals(LoginState.OUT)) {
                    if (logEvent.getTimestamp() > latest) {
                        latest = logEvent.getTimestamp();
                    }
                }
            }
        }
        System.out.println("lastest: " + latest);
        return latest;
    }

    @Override
    public String toString() {
        return "DateLogData{" +
                "logEvents=" + logEvents +
                ", date=" + date +
                ", totalTime=" + totalTime +
                '}';
    }
}
