package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.adiBlum.adiblum.myapplication.NeuraConnection;
import com.adiBlum.adiblum.myapplication.model.AllLoginData;
import com.google.gson.Gson;
import com.neura.resources.insights.DailySummaryCallbacks;
import com.neura.resources.insights.DailySummaryData;
import com.neura.resources.object.ActivityPlace;
import com.neura.resources.situation.SituationCallbacks;
import com.neura.resources.situation.SituationData;
import com.neura.standalonesdk.service.NeuraApiClient;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DataFetcherService {

    public static final String DATA_FETCHER_SERVICE_RESULT = "DataFetcherServiceResult";
    public static final String CUSTOM_DATA_FETCHER_SERVICE_RESULT = "CustomDataFetcherServiceResult";
    public static final String USER_SITUATION_RESULT = "UserSituation";
    private static DataFetcherService instance = null;
    int pendingAnswers = 0;

    private AllLoginData allLoginData;

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

        NeuraApiClient client = NeuraConnection.getClient();
        client.getDailySummary(date.getTime(), new DailySummaryCallbacks() {
            @Override
            public void onSuccess(DailySummaryData dailySummaryData) {
                double timeSpentAtWork = getTimeSpentAtWork(dailySummaryData);
                handleHttpResultForDay(timeSpentAtWork, date, context);
            }

            @Override
            public void onFailure(Bundle bundle, int i) {
                decPendingAnswers();
            }
        });

    }

    private void handleHttpResultForDay(double timeSpentAtWork, Date dateToday, Context context) {
        allLoginData.updateTimeAtPlace(dateToday, timeSpentAtWork);
        decPendingAnswers();
        if (getPendingAnswers() == 0) {
            sendSummaryResults(context);
        }
        SaveDataHelper.addToFile(allLoginData, context);
    }

    private void sendSummaryResults(Context context) {
        System.out.println("sending summary results: " + allLoginData.getDateToLoginData().size());
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(DATA_FETCHER_SERVICE_RESULT);
        intent.putExtra(DATA_FETCHER_SERVICE_RESULT, allLoginData);
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
        if (SaveDataHelper.isSameDay(date, new Date()) ||
                SaveDataHelper.isSameDay(date, DatesHelper.getYesterday())) { // always ask for today
            requestDailySummaryFromClient(date, context);
            return false;
        }
        if (!allLoginData.getDateToLoginData().containsKey(date)) {
            requestDailySummaryFromClient(date, context);
            return false;
        }
        return true;
    }

    public boolean askForDataForDatesOfMonth(Context context) {
        try {
            allLoginData = SaveDataHelper.getDataFromFile(context);
        } catch (IOException | ClassNotFoundException e) {
            if (allLoginData == null) {
                allLoginData = new AllLoginData();
            }
            e.printStackTrace();
            System.out.println("failed to read file");
        }

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
        final long timestamp = System.currentTimeMillis();
        System.out.println("siiit request: " + timestamp);
        neuraApiClient.getUserSituation(new SituationCallbacks() {
            @Override
            public void onSuccess(SituationData situationData) {
                if (situationData != null) {
                    System.out.println("received response for situation: " + situationData);
                    sendSituationDataResults(context, situationData);
                }
            }

            @Override
            public void onFailure(Bundle bundle, int i) {
                System.out.println("failed to get response of timestamp: " + timestamp);
            }
        }, timestamp);
    }
}
