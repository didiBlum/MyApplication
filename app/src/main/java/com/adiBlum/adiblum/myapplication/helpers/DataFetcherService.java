package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.adiBlum.adiblum.myapplication.NeuraConnection;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.neura.resources.insights.DailySummaryCallbacks;
import com.neura.resources.insights.DailySummaryData;
import com.neura.resources.object.ActivityPlace;
import com.neura.resources.situation.SituationCallbacks;
import com.neura.resources.situation.SituationData;
import com.neura.standalonesdk.service.NeuraApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DataFetcherService {

    public static final String DATA_FETCHER_SERVICE_RESULT = "DataFetcherServiceResult";
    public static final String USER_SITUATION_RESULT = "UserSituation";
    private static DataFetcherService instance = null;
    int pendingAnswers = 0;
    private Map<String, Double> datesToHours = new HashMap<>();
    public static final String URL = "https://wapi.theneura.com/v1/users/profile/daily_summary?date=";

    private synchronized void incPendingAnswers() {
        pendingAnswers++;
    }

    private synchronized void decPendingAnswers() {
        pendingAnswers--;
    }

    private synchronized int getPendingAnswers() {
        return pendingAnswers;
    }

    public static DataFetcherService getInstance() {
        if (instance == null) {
            instance = new DataFetcherService();
        }
        return instance;
    }

    public void requestDailySummaryFromClient(final Date date, final Context context) {
        incPendingAnswers();
//        System.out.println("getting data from server for " + date);
        final String dateToday = new SimpleDateFormat("yyyy-MM-dd").format(date);

        NeuraApiClient client = NeuraConnection.getClient();
        client.getDailySummary(date.getTime(), new DailySummaryCallbacks() {
            @Override
            public void onSuccess(DailySummaryData dailySummaryData) {
                double timeSpentAtWork = getTimeSpentAtWork(dailySummaryData);
                handleHttpResultForDay(timeSpentAtWork, dateToday, context);
            }

            @Override
            public void onFailure(Bundle bundle, int i) {
                decPendingAnswers();
            }
        });

    }

    private void handleHttpResultForDay(double timeSpentAtWork, String dateToday, Context context) {
//        System.out.println("timeSpentAtWork: " + timeSpentAtWork / 60 / 60 + " hours");
        SaveDataHelper.addToFile(dateToday + ":" + timeSpentAtWork + ";",
                context);
        datesToHours.put(dateToday, timeSpentAtWork);
        decPendingAnswers();
//        System.out.println("pendingAnswers-- now is: " + getPendingAnswers());
        if (getPendingAnswers() == 0) {
            sendSummaryResults(context);
        }
    }

    private void sendSummaryResults(Context context) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(DATA_FETCHER_SERVICE_RESULT);
        intent.putExtra(DATA_FETCHER_SERVICE_RESULT, (Serializable) datesToHours);
        broadcastManager.sendBroadcast(intent);
    }

    private void sendSituationDataResults(Context context, SituationData situationData) {
        if (situationData.getCurrentSituation() != null || situationData.getPreviousSituation() != null) {
            LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
            Intent intent = new Intent(USER_SITUATION_RESULT);
            intent.putExtra(USER_SITUATION_RESULT, (new Gson()).toJson(situationData));
            broadcastManager.sendBroadcast(intent);
        }
    }

    private boolean getDataForDay(Date date, Context context) {
//        System.out.println("getting data for day: " + date);

        if (SaveDataHelper.isSameDay(date, new Date()) ||
                SaveDataHelper.isSameDay(date, DatesHelper.getYesterday())) { // always ask for today
            requestDailySummaryFromClient(date, context);
            return false;
        }
        String stringDate = SaveDataHelper.getStringDate(date);
        if (!datesToHours.containsKey(stringDate)) {
            requestDailySummaryFromClient(date, context);
            return false;
        }
        return true;
    }

    public boolean askForDataForDatesOfMonth(Context context) {
        datesToHours = SaveDataHelper.getDataFromFile(context);
        Date current = DatesHelper.getFirstDateOfTheMonth();
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.add(Calendar.DATE, -8);
        return askForDataForDates(cal.getTime(), new Date(), context);
    }

    private synchronized boolean askForDataForDates(Date start, Date end, Context context) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        boolean hasAllData = true;
        while (start.before(end) || SaveDataHelper.isSameDay(start, end)) {
            if (!getDataForDay(start, context)) {
                hasAllData = false;
            }
            cal.add(Calendar.DATE, 1);
            start = cal.getTime();
        }
        return hasAllData;
    }

    private double getTimeSpentAtWork(DailySummaryData dailySummaryData) {
        ArrayList<ActivityPlace> visitedPlaces = dailySummaryData.getVisitedPlaces();
        if (visitedPlaces != null) {
            for (ActivityPlace activityPlace : visitedPlaces) {
                if (activityPlace.getLabel().equals("work")) {
                    return activityPlace.getTimeSpentAtPlace();
                }
            }
        }
        return 0;
    }

    public void getUserSituation(final Context context) {
        NeuraApiClient neuraApiClient = NeuraConnection.getmNeuraApiClient();
        long timestamp = System.currentTimeMillis();
        System.out.println("siiit request: " + timestamp);
        neuraApiClient.getUserSituation(new SituationCallbacks() {
            @Override
            public void onSuccess(SituationData situationData) {
                sendSituationDataResults(context, situationData);
            }

            @Override
            public void onFailure(Bundle bundle, int i) {

            }
        }, timestamp);
    }
}
