package com.adiBlum.adiblum.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalaryCalculator {

    private Double hourSalary;
    private Double dailySalary;

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public double geySalaryForTime(double timeInSeconds, Context ctx, int workingDays) {
        getSalaryValues(ctx);
        double result = 0;
        int hours = (int) timeInSeconds / 60 / 60;
        result += hours * hourSalary;
        double minutes = (timeInSeconds / 60) % 60;
        double partOfHour = minutes / 60;
        result += partOfHour * hourSalary;
        result += workingDays * dailySalary;
        return new BigDecimal(result).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private void getSalaryValues(Context ctx) {
        String hourSalary = getSharedPreferences(ctx).getString("hourSalary", "0");
        this.hourSalary = Double.parseDouble(hourSalary);
        String dailySalary = getSharedPreferences(ctx).getString("dailySalary", "0");
        this.dailySalary = Double.parseDouble(dailySalary);
    }

    public boolean isInitSalaryValues(Context ctx) {
        return (getSharedPreferences(ctx).contains("hourSalary") &&
                getSharedPreferences(ctx).contains("dailySalary"));
    }
}
