package com.adiBlum.adiblum.myapplication.model;


import java.io.Serializable;

public class LogEvent implements Serializable {
    private LoginState state;
    private long timestamp;

    public LogEvent(LoginState state, long timestamp) {
        this.state = state;
        this.timestamp = timestamp;
    }

    public LogEvent() {
    }

    public LoginState getState() {
        return state;
    }

    public void setState(LoginState state) {
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "state=" + state +
                ", timestamp=" + timestamp +
                '}';
    }
}
