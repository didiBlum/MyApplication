package com.adiBlum.adiblum.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalaryCalculator {

    public static final double EMPTY_VALUE = -1.0;
    private Double hourSalary;
    private Double dailySalary;

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public double geySalaryForTime(double timeInSeconds, Context ctx, int workingDays) {
        getSalaryValues(ctx);
        if (checkForEmpty()) return EMPTY_VALUE;
        return calculateNotEmptySalary(timeInSeconds, workingDays);
    }

    private double calculateNotEmptySalary(double timeInSeconds, int workingDays) {
        double result = 0;
        int hours = (int) timeInSeconds / 60 / 60;
        result += hours * hourSalary;
        double minutes = (timeInSeconds / 60) % 60;
        double partOfHour = minutes / 60;
        result += partOfHour * hourSalary;
        result += workingDays * dailySalary;
        return new BigDecimal(result).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private boolean checkForEmpty() {
        if ((hourSalary == null || hourSalary.equals(EMPTY_VALUE)) &&
                dailySalary == null || dailySalary.equals(EMPTY_VALUE)){
            return true;
        }
        return false;
    }

    private void getSalaryValues(Context ctx) {
        String hourSalary = getSharedPreferences(ctx).getString("hourSalary", "-1");
        this.hourSalary = Double.parseDouble(hourSalary);
        String dailySalary = getSharedPreferences(ctx).getString("dailySalary", "-1");
        this.dailySalary = Double.parseDouble(dailySalary);
    }

    public boolean isInitSalaryValues(Context ctx) {
        return (getSharedPreferences(ctx).contains("hourSalary") &&
                getSharedPreferences(ctx).contains("dailySalary"));
    }
}
