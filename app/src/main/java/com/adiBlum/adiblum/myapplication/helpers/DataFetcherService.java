package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.adiBlum.adiblum.myapplication.NeuraConnection;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.neura.resources.insights.DailySummaryData;
import com.neura.resources.object.ActivityPlace;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DataFetcherService {

    public static final String DATA_FETCHER_SERVICE_RESULT = "DataFetcherServiceResult";
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

    private synchronized void resetPendingAnswers() {
        pendingAnswers = 0;
    }



    public static DataFetcherService getInstance() {
        if (instance == null) {
            instance = new DataFetcherService();
        }
        return instance;
    }

    public Map<String, Double> getDatesToHours() {
        return datesToHours;
    }

    private void requestDailySummary(final Date date, final Context context) {
        incPendingAnswers();
//        System.out.println("getting data from server for " + date);
        RequestQueue queue = Volley.newRequestQueue(context);
        final String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        String url = URL + dateToday;
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        System.out.println("Response" + response);
                        try {
                            double timeSpentAtWork = getTimeSpentAtWork(response);
                            handleHttpResultForDay(timeSpentAtWork, dateToday, context);
                        } catch (JSONException e) {
                            handleHttpResultForDay(-1.0, dateToday, context);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        System.out.println("error => " + error.toString());
                        decPendingAnswers();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                String bearer = "Bearer " + NeuraConnection.getAccessToken(context);
//                System.out.println("bearer: " + bearer);

                params.put("Authorization", bearer);
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void handleHttpResultForDay(double timeSpentAtWork, String dateToday, Context context) {
//        System.out.println("timeSpentAtWork: " + timeSpentAtWork / 60 / 60 + " hours");
        SaveDataHelper.addToFile(dateToday + ":" + timeSpentAtWork + ";",
                context);
        datesToHours.put(dateToday, timeSpentAtWork);
        decPendingAnswers();
//        System.out.println("pendingAnswers-- now is: " + getPendingAnswers());
        if (getPendingAnswers() == 0) {
            sendResults(context);
        }
    }

    private void sendResults(Context context) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(DATA_FETCHER_SERVICE_RESULT);
        intent.putExtra(DATA_FETCHER_SERVICE_RESULT, (Serializable) datesToHours);
        broadcastManager.sendBroadcast(intent);
    }

    private double getTimeSpentAtWork(String dailySummaryJson) throws JSONException {
        JSONObject json = new JSONObject(dailySummaryJson);
        JSONObject data = json.getJSONObject("data");
        if (data != null) {
            JSONArray jsonArray = data.getJSONArray("visitedPlaces");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject visitedPlace = jsonArray.getJSONObject(i);
                if ("work".equals(visitedPlace.getString("label"))) {
                    return visitedPlace.getDouble("timeSpentAtPlace");
                }
            }
            return 0;
        }
        return -1;
    }

    private boolean getDataForDay(Date date, Context context) {
//        System.out.println("getting data for day: " + date);
        if (SaveDataHelper.isSameDay(date, new Date())) { // always ask for today
            requestDailySummary(date, context);
            return false;
        }
        String stringDate = SaveDataHelper.getStringDate(date);
        if (!datesToHours.containsKey(stringDate)) {
            requestDailySummary(date, context);
            return false;
        }
        return true;
    }

    public boolean askForDataForDatesOfMonth(Context context) {
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
        ArrayList<ActivityPlace> activityPlaces = dailySummaryData.getActivityPlaces();
        for (ActivityPlace activityPlace : activityPlaces) {
            if (activityPlace.getLabel().equals("work")) {
                return activityPlace.getTimeSpentAtPlace();
            }
        }
        return 0;
    }
}
