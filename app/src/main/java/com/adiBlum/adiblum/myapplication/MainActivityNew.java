package com.adiBlum.adiblum.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.adiBlum.adiblum.myapplication.helpers.DatesHelper;
import com.adiBlum.adiblum.myapplication.helpers.SaveDataHelper;
import com.adiBlum.adiblum.myapplication.helpers.ShareHelper;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.neura.resources.insights.DailySummaryData;
import com.neura.resources.object.ActivityPlace;
import com.neura.standalonesdk.util.SDKUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivityNew extends AppCompatActivity {

    private static final String IS_FIRST_RUN = "isFirstRun";
    public static final String URL = "https://wapi.theneura.com/v1/users/profile/daily_summary?date=";
    private boolean isCustom = false;
    private Date customStart = null;
    private Date customEnd = null;
    private Map<String, Double> datesToHours = new HashMap<>();
    int pendingAnswers = 0;

    private MainActivityNew view;
    private TabLayout tabLayout;
    private PagerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_viewer);
        view = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        tabLayout = setTabs();
        setPageViewer(tabLayout);
        isFirstTime();
    }

//    @Override
//    protected void onResume(){
//        super.onResume();
//        isFirstTime();
//    }

    private void mainFlow() {
        datesToHours = SaveDataHelper.getDataFromFile(this.getApplicationContext());
        connectToNeura();
    }

    private void connectToNeura() {
        NeuraConnection.initNeuraConnection(this);
        if (!SDKUtils.isConnected(this, NeuraConnection.getmNeuraApiClient())) {
            NeuraConnection.authenticateNeura(this, this);
        } else {
            showData();
        }
    }

    public synchronized void showData() {
        try {
            if (askForDataForDatesOfMonth()) {
                showSpinner(false);
                updateViews();
            } else {
                showSpinner(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateViews() {
        SummaryActivity summaryActivity = (SummaryActivity) this.adapter.getItem(0);
        summaryActivity.start(datesToHours);

        HistoryActivity historyActivity = (HistoryActivity) this.adapter.getItem(1);
        historyActivity.start(datesToHours);
    }

    private void setPageViewer(TabLayout tabLayout) {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        assert viewPager != null;
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        this.adapter = adapter;
    }

    @NonNull
    private TabLayout setTabs() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        assert tabLayout != null;
        tabLayout.addTab(tabLayout.newTab().setText("Summary"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        return tabLayout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SaveSalaryDataActivity.class), 1);
                return true;
            case R.id.action_share:
                showShare();
                return true;
        }
        return true;
    }

    private void showShare() {
        Intent i = ShareHelper.getIntentForShare(datesToHours);
        startActivity(Intent.createChooser(i, "Share via"));
    }

    private void isFirstTime() {
        Boolean isFirstRun = getSharedPreferences(this.getApplicationContext())
                .getBoolean(IS_FIRST_RUN, true);
        if (isFirstRun) {
            getSharedPreferences(this.getApplicationContext()).edit().putBoolean(IS_FIRST_RUN, false).commit();
            showWelcome();
        } else {
            mainFlow();
        }
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    private void showWelcome() {
        System.out.println("first time");
        startActivityForResult(new Intent(this, WelcomeActivity.class), 0);
    }

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

    private void handleResultForDay(double timeSpentAtWork, String dateToday) {
//        System.out.println("timeSpentAtWork: " + timeSpentAtWork / 60 / 60 + " hours");
        SaveDataHelper.addToFile(dateToday + ":" + timeSpentAtWork + ";", getApplicationContext());
        datesToHours.put(dateToday, timeSpentAtWork);
        decPendingAnswers();
//        System.out.println("pendingAnswers-- now is: " + getPendingAnswers());
        if (getPendingAnswers() == 0) {
//            if (isCustom) {
//                getCustomDataForDates(customStart, customEnd);
//            }
//            updateTextViews();
        }
    }

    public void requestDailySummary(final Date date) {
        incPendingAnswers();
//        System.out.println("getting data from server for " + date);
        RequestQueue queue = Volley.newRequestQueue(this);
        final String dateToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
        String url = URL + dateToday;
        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
//                        System.out.println("Response" + response);
                        try {
                            double timeSpentAtWork = getTimeSpentAtWork(response);
                            handleHttpResultForDay(timeSpentAtWork, dateToday);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            handleHttpResultForDay(-1.0, dateToday);
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
                String bearer = "Bearer " + NeuraConnection.getAccessToken(getApplicationContext());
//                System.out.println("bearer: " + bearer);

                params.put("Authorization", bearer);
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void handleHttpResultForDay(double timeSpentAtWork, String dateToday) {
//        System.out.println("timeSpentAtWork: " + timeSpentAtWork / 60 / 60 + " hours");
        SaveDataHelper.addToFile(dateToday + ":" + timeSpentAtWork + ";",
                getApplicationContext());
        datesToHours.put(dateToday, timeSpentAtWork);
        decPendingAnswers();
//        System.out.println("pendingAnswers-- now is: " + getPendingAnswers());
        if (getPendingAnswers() == 0) {
//            if (isCustom) {
//                getCustomDataForDates(customStart, customEnd);
//            }
            showSpinner(false);
            updateViews();
        }
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mainFlow();
    }

    private boolean getDataForDay(Date date) throws IOException {
//        System.out.println("getting data for day: " + date);
        if (SaveDataHelper.isSameDay(date, new Date())) { // always ask for today
            requestDailySummary(date);
            return false;
        }
        String stringDate = SaveDataHelper.getStringDate(date);
        if (!datesToHours.containsKey(stringDate)) {
            requestDailySummary(date);
            return false;
        }
        return true;
    }

    private boolean askForDataForDatesOfMonth() throws IOException {
        Date current = DatesHelper.getFirstDateOfTheMonth();
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.add(Calendar.DATE, -8);
        return askForDataForDates(cal.getTime(), new Date());
    }

    private synchronized boolean askForDataForDates(Date start, Date end) throws IOException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        boolean hasAllData = true;
        while (start.before(end) || SaveDataHelper.isSameDay(start, end)) {
            if (!getDataForDay(start)) {
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

    private void showSpinner(boolean showSpinner) {
        ProgressBar spinner = (ProgressBar) view.findViewById(R.id.progressBar1);
        assert spinner != null;
        if (showSpinner) {
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
        }
    }

}