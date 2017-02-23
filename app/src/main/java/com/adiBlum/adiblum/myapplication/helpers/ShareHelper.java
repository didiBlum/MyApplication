package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.adiBlum.adiblum.myapplication.model.AllLoginData;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class ShareHelper {

    private static final String COMMA_DELIMITER = ": ";
    private static final String NEW_LINE_SEPARATOR = "\n";

    public static String createMonthly(AllLoginData allLoginData, Date startDate, Date endDate) {
        String result = "";
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        double totalTime = 0;
        int workingDays = 0;
        while (startDate.before(endDate) || SaveDataHelper.isSameDay(startDate, endDate)) {
            String stringDate = SaveDataHelper.getStringDate(startDate);
            if (allLoginData.getDateToLoginData().containsKey(startDate)) {
                Double timeAtWork = allLoginData.getDataForDate(startDate).getTotalTime();
                String formattedTimeAtWork = SaveDataHelper.getPrettyTimeString(timeAtWork);
                if (timeAtWork == -1) {
                    formattedTimeAtWork = "No data";
                }
                if (timeAtWork == 0) {
                    formattedTimeAtWork = "Out of work";
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

    @NonNull
    public static Intent getIntentForShare(AllLoginData allLoginData, Date start, Date end) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        String monthly = ShareHelper.createMonthly(allLoginData, start, end);
        i.putExtra(Intent.EXTRA_SUBJECT, "Monthly report for " + DatesHelper.getMonthName());
        i.putExtra(Intent.EXTRA_TEXT, monthly);
        return i;
    }

    @NonNull
    public static Intent getIntentForFeedback(Context context) throws PackageManager.NameNotFoundException {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.setType("text/email");
        email.putExtra(Intent.EXTRA_EMAIL, new String[] { "autoworklog.feedback@gmail.com" });
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        String version = pInfo.versionName;
        int verCode = pInfo.versionCode;
        email.putExtra(Intent.EXTRA_SUBJECT, "Feedback (version: " + version + ", code version " + verCode + ")");
        return email;
    }
}
