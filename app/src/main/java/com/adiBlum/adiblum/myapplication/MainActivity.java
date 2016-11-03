package com.adiBlum.adiblum.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;
import com.neura.standalonesdk.util.SDKUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String URL = "https://wapi.theneura.com/v1/users/profile/daily_summary?date=";
    private Map<String, Double> datesToHours = new HashMap<>();

    int pendingAnswers = 0;
    private MainActivity mainActivity;
    private static final String dateFormat = "MMM d";
    private static final String textColor = "#007f00";
    private SalaryCalculator salaryCalculator = new SalaryCalculator();
    private boolean isCustom = false;
    private Date customStart = null;
    private Date customend = null;
    private ActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.try_me);
        isFirstTime();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                showWelcome();
                return true;
//            case R.id.action_share:
//                mShareActionProvider = MenuItemCompat.getActionProvider(item);
//                showShare();
//                return true;
        }
        return true;
    }

    private void showShare() {
        Intent i = new Intent(android.content.Intent.ACTION_SEND);
        i.setType("text/plain");
        String monthly = ShareHelper.createMonthly(datesToHours, getFirstDateOfTheMonth(), new Date());
        i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Monthly report for " + getMonthName());
        i.putExtra(android.content.Intent.EXTRA_TEXT, monthly);
        startActivity(Intent.createChooser(i, "Share via"));
    }

    private void mainFlow() {
        mainActivity = this;
        datesToHours = SaveDataHelper.getDataFromFile(getApplicationContext());

        NeuraConnection.initNeuraConnection(this);
        if (!SDKUtils.isConnected(this, NeuraConnection.getmNeuraApiClient())) {
            NeuraConnection.authenticateNeura(this, mainActivity);
        } else {
            showData();
        }
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    private void isFirstTime() {
        Boolean isFirstRun = getSharedPreferences(getApplicationContext())
                .getBoolean("isFirstRun", true);
        if (isFirstRun || !salaryCalculator.isInitSalaryValues(getApplicationContext())) {
            getSharedPreferences(getApplicationContext()).edit().putBoolean("isFirstRun", false).commit();
            showWelcome();
        } else {
            mainFlow();
        }
    }

    private void showWelcome() {
        startActivityForResult(new Intent(this, WelcomeActivity.class), 0);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        mainFlow();
    }

    public synchronized void showData() {
        try {
            if (askForDataForDatesOfMonth()) {
                updateTextViews();
            } else {
                showSpinner(true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTextViews() {
        updateToday();
        updateWeek();
        updateMonth();
//        showGragh();

        showSpinner(false);
        resetPendingAnswers();
    }

    private void updateMonth() {
        final int[] daysCalculated = {0};
        final Double[] timeSpentAtWork = {0.0};
        TextView textView = (TextView) mainActivity.findViewById(R.id.monthTextView);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        // get start of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date current = cal.getTime();
        collectData(daysCalculated, timeSpentAtWork, current);

        if (timeSpentAtWork[0].equals(-1.0)) {
            textView.setText(Html.fromHtml("No data found for all the days since the beginning of the month"));
        } else {
            String time = setText(timeSpentAtWork[0], daysCalculated[0]);
            textView.setText(Html.fromHtml("Time spent at work from the beginning of the month: " + time));
        }
    }

    private void updateWeek() {
        final int[] daysCalculated = {0};
        final Double[] timeSpentAtWork = {0.0};
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.getInstance().getFirstDayOfWeek());
        Date current = cal.getTime();   //start with begging of week
        collectData(daysCalculated, timeSpentAtWork, current);
        TextView textView = (TextView) mainActivity.findViewById(R.id.weekTextView);
        if (timeSpentAtWork[0].equals(-1.0)) {
            textView.setText(Html.fromHtml("No data found for all the days since the beginning of the week"));
        } else {
            String time = setText(timeSpentAtWork[0], daysCalculated[0]);
            textView.setText(Html.fromHtml("Time spent at work this week: " + time));
        }
    }

    private void updateToday() {
        TextView textView = (TextView) mainActivity.findViewById(R.id.todayTextView);
        Double timeSpentAtWork = datesToHours.get(SaveDataHelper.getStringDate(new Date()));
        int workingDays = 0;
        if (timeSpentAtWork > 0) {
            workingDays++;
        }
        String time = setText(timeSpentAtWork, workingDays);
        textView.setText(Html.fromHtml("Time spent at work today: " + time));
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


    public void setDatePicker(View view) {
        SmoothDateRangePickerFragment smoothDateRangePickerFragment = SmoothDateRangePickerFragment.newInstance(
                new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                    @Override
                    public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                               int yearStart, int monthStart,
                                               int dayStart, int yearEnd,
                                               int monthEnd, int dayEnd) {
                        setCustomDates(yearStart, monthStart, dayStart, yearEnd, monthEnd, dayEnd);
                    }
                });

        smoothDateRangePickerFragment.show(getFragmentManager(), "smoothDateRangePicker");
    }

    private void setCustomDates(int yearStart, int monthStart, int dayStart, int yearEnd, int monthEnd, int dayEnd) {
        Date startDate = new Date(yearStart - 1900, monthStart, dayStart);
        Date endDate = new Date(yearEnd - 1900, monthEnd, dayEnd);
        try {
            isCustom = true;
            customStart = startDate;
            customend = endDate;
            if (askForDataForDates(startDate, endDate)) {
                getCustomDataForDates(startDate, endDate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getCustomDataForDates(Date startDate, Date endDate) {
        final int[] workingDays = {0};
        final Double[] timeSpentAtWork = {0.0};
        TextView textView = (TextView) mainActivity.findViewById(R.id.customDatesTextView);
        String startString = new SimpleDateFormat(dateFormat).format(startDate);
        String endString = new SimpleDateFormat(dateFormat).format(endDate);

        collectDataBetweenDates(workingDays, timeSpentAtWork, startDate, endDate);
        if (timeSpentAtWork[0].equals(-1.0)) {
            textView.setText(Html.fromHtml("No data was found for all the days between " + startString + " till " + endString));
        } else {
            String time = setText(timeSpentAtWork[0], workingDays[0]);
            textView.setText(Html.fromHtml("Time spent at work from " + startString + " till " + endString + ": " + time));
        }
        resetCustom();
    }

    private void resetCustom() {
        isCustom = false;
        customStart = null;
        customend = null;
    }

    public void requestDailySummary(final Date date) {
        incPendingAnswers();
//        System.out.println("getting data from server for " + date);
        RequestQueue queue = Volley.newRequestQueue(this);
        final String dateToday = new SimpleDateFormat("yyyy-MM-dd").format(date);
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
        SaveDataHelper.addToFile(dateToday + ":" + timeSpentAtWork + ";", getApplicationContext());
        datesToHours.put(dateToday, timeSpentAtWork);
        decPendingAnswers();
//        System.out.println("pendingAnswers-- now is: " + getPendingAnswers());
        if (getPendingAnswers() == 0) {
            if (isCustom) {
                getCustomDataForDates(customStart, customend);
            }
            updateTextViews();
        }
    }

    private String setText(double timeSpentAtWork, int workingDays) {
        String formatteedTime = SaveDataHelper.getPrettyTimeString(timeSpentAtWork);
        double salary = salaryCalculator.geySalaryForTime(timeSpentAtWork, getApplicationContext(), workingDays);
        return "<b><font color=\"#007f00\">" + formatteedTime + "  - total " + salary + "$</font></b>";
    }

    private double getTimeSpentAtWork(String dailySummaryJson) throws JSONException {
        JSONObject json = new JSONObject(dailySummaryJson);
        JSONArray jsonArray = json.getJSONObject("data").getJSONArray("visitedPlaces");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject visitedPlace = jsonArray.getJSONObject(i);
            if ("work".equals(visitedPlace.getString("label"))) {
                return visitedPlace.getDouble("timeSpentAtPlace");
            }
        }
        return 0;
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

    private void collectData(int[] workingDays, Double[] timeSpentAtWork, Date current) {
        collectDataBetweenDates(workingDays, timeSpentAtWork, current, new Date());
    }

    private void collectDataBetweenDates(int[] workingDays, Double[] timeSpentAtWork, Date current, Date lastDay) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        while (current.before(lastDay) || SaveDataHelper.isSameDay(current, lastDay)) {
            System.out.println("asked about " + current);
            Double currentVal = datesToHours.get(SaveDataHelper.getStringDate(current));
            if (currentVal == null || currentVal.equals(-1.0)) {
                timeSpentAtWork[0] = -1.0;
                return;
            }
            timeSpentAtWork[0] += currentVal;
            if (currentVal > 0) {
                workingDays[0]++;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
            current = cal.getTime();
        }
    }

    private boolean askForDataForDatesOfMonth() throws IOException {
        Date current = getFirstDateOfTheMonth();
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        cal.add(Calendar.DATE, -8);
        return askForDataForDates(cal.getTime(), new Date());
    }

    private Date getFirstDateOfTheMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        // get start of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    private String getMonthName() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy");
        return sdf.format(new Date());
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

    private void showSpinner(boolean showSpinner) {
        ProgressBar spinner = (ProgressBar) mainActivity.findViewById(R.id.progressBar1);
        assert spinner != null;
        if (showSpinner) {
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
        }
    }
}

