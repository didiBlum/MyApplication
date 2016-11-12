package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SalaryCalculator {

    public static final double EMPTY_VALUE = 0.0;
    public static final String HOUR_SALARY_ATTRIBUTE = "hourSalary";
    public static final String DAILY_SALARY_ATTRIBUTE = "dailySalary";
    private Double hourSalary;
    private Double dailySalary;

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public double geySalaryForTime(double timeInSeconds, Context ctx, int workingDays) {
        getSalaryValues(ctx);
        if (checkForEmpty(ctx)) return EMPTY_VALUE;
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

    private boolean checkForEmpty(Context ctx) {
        String hourString = getSharedPreferences(ctx).getString(HOUR_SALARY_ATTRIBUTE, String.valueOf(EMPTY_VALUE));
        double hours = Double.parseDouble(hourString);

        String dailyString = getSharedPreferences(ctx).getString(DAILY_SALARY_ATTRIBUTE, String.valueOf(EMPTY_VALUE));
        double daily = Double.parseDouble(dailyString);

        return hours <= EMPTY_VALUE && daily <= EMPTY_VALUE;
    }

    private void getSalaryValues(Context ctx) {
        String hourSalary = getSharedPreferences(ctx).getString(HOUR_SALARY_ATTRIBUTE, String.valueOf(EMPTY_VALUE));
        this.hourSalary = Double.parseDouble(hourSalary);
        String dailySalary = getSharedPreferences(ctx).getString(DAILY_SALARY_ATTRIBUTE, String.valueOf(EMPTY_VALUE));
        this.dailySalary = Double.parseDouble(dailySalary);
    }
}
