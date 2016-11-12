package com.adiBlum.adiblum.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adiBlum.adiblum.myapplication.helpers.DatesHelper;
import com.adiBlum.adiblum.myapplication.helpers.SalaryCalculator;
import com.adiBlum.adiblum.myapplication.helpers.SaveDataHelper;
import com.adiBlum.adiblum.myapplication.helpers.ShareHelper;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;
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
import java.util.Map;

public class SummaryActivity extends Fragment {

    private static final String IS_FIRST_RUN = "isFirstRun";
    private static final String dateFormat = "MMM d";
    public static final String URL = "https://wapi.theneura.com/v1/users/profile/daily_summary?date=";

    private Map<String, Double> datesToHours = new HashMap<>();
    int pendingAnswers = 0;
    private View summaryActivity;
    private SalaryCalculator salaryCalculator = new SalaryCalculator();
    private boolean isCustom = false;
    private Date customStart = null;
    private Date customEnd = null;

    //    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_with_cells);
//        isFirstTime();
//    }
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
////        summaryActivity = (FragmentActivity) activity;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        summaryActivity = super.getActivity();
        View view = inflater.inflate(R.layout.activity_with_cells, container, false);
        summaryActivity = view;
        isFirstTime();
        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(getActivity(), SaveSalaryDataActivity.class), 1);
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

    private void mainFlow() {
        datesToHours = SaveDataHelper.getDataFromFile(getActivity().getApplicationContext());

        NeuraConnection.initNeuraConnection(getActivity());
        if (!SDKUtils.isConnected(getActivity(), NeuraConnection.getmNeuraApiClient())) {
//            NeuraConnection.authenticateNeura(getActivity(), summaryActivity);
        } else {
            showData();
        }
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    private void isFirstTime() {
        Boolean isFirstRun = getSharedPreferences(getActivity().getApplicationContext())
                .getBoolean(IS_FIRST_RUN, true);
        if (isFirstRun) {
            getSharedPreferences(getActivity().getApplicationContext()).edit().putBoolean(IS_FIRST_RUN, false).commit();
            showWelcome();
        } else {
            mainFlow();
        }
    }

    private void showWelcome() {
        startActivityForResult(new Intent(getActivity(), WelcomeActivity.class), 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        showSpinner(false);
        resetPendingAnswers();
    }

    private void updateMonth() {
        TextView textView = (TextView) summaryActivity.findViewById(R.id.monthlyTextview);
        TextView titleView = (TextView) summaryActivity.findViewById(R.id.monthly_title_textview);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);

        // get start of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date current = cal.getTime();
        setTextviewForValues(textView, titleView, current);
    }

    private void setTextviewForValues(TextView textView, TextView titleView, Date start) {
        final int[] daysCalculated = {0};
        final Double[] timeSpentAtWork = {0.0};
        boolean missingData = collectData(daysCalculated, timeSpentAtWork, start);
        assert textView != null;
        String time = getTextForData(timeSpentAtWork[0], daysCalculated[0]);
        textView.setText(time);
        if (missingData) {
            Date earliestDate = getEarliestDateMissingData(start, new Date());
            assert titleView != null;
            titleView.setText(titleView.getText() + " (since " + new SimpleDateFormat(dateFormat).format(earliestDate) + ")");
        }
    }

    private void updateWeek() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.getInstance().getFirstDayOfWeek());
        Date current = cal.getTime();   //start with begging of week
        TextView textView = (TextView) summaryActivity.findViewById(R.id.weeklyTextView);
        TextView titleView = (TextView) summaryActivity.findViewById(R.id.weekly_title_textview);
        setTextviewForValues(textView, titleView, current);
    }

    private void updateToday() {
        TextView textView = (TextView) summaryActivity.findViewById(R.id.dailyTextView);
        Double timeSpentAtWork = datesToHours.get(SaveDataHelper.getStringDate(new Date()));
        int workingDays = 0;
        if (timeSpentAtWork > 0) {
            workingDays++;
        }
        String time = getTextForData(timeSpentAtWork, workingDays);
        if (textView != null) {
            textView.setText(time);
        }
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

        smoothDateRangePickerFragment.show(getActivity().getFragmentManager(), "smoothDateRangePicker");
    }

    private void setCustomDates(int yearStart, int monthStart, int dayStart, int yearEnd, int monthEnd, int dayEnd) {
        Date startDate = new Date(yearStart - 1900, monthStart, dayStart);
        Date endDate = new Date(yearEnd - 1900, monthEnd, dayEnd);
        try {
            isCustom = true;
            customStart = startDate;
            customEnd = endDate;
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
        TextView customDataTextView = (TextView) summaryActivity.findViewById(R.id.customTextView);
        TextView customTitleTextview = (TextView) summaryActivity.findViewById(R.id.custom_title_textview);
        assert customDataTextView != null;
        customDataTextView.setVisibility(View.VISIBLE);
        String startString = new SimpleDateFormat(dateFormat).format(startDate);
        String endString = new SimpleDateFormat(dateFormat).format(endDate);

        collectDataBetweenDates(workingDays, timeSpentAtWork, startDate, endDate);
        assert customDataTextView != null;
        assert customTitleTextview != null;
        customTitleTextview.setText(startString + " - " + endString);
        if (timeSpentAtWork[0].equals(-1.0)) {
            customDataTextView.setText("No data was found for all the days");
        } else {
            String time = getTextForData(timeSpentAtWork[0], workingDays[0]);
            customDataTextView.setText(time);
        }
        resetCustom();
    }

    private void resetCustom() {
        isCustom = false;
        customStart = null;
        customEnd = null;
    }

//    public void requestDailySummaryFromClient(final Date date) {
//        incPendingAnswers();
////        System.out.println("getting data from server for " + date);
//        final String dateToday = new SimpleDateFormat("yyyy-MM-dd").format(date);
//
//        NeuraApiClient client = NeuraConnection.getClient();
//        client.getDailySummary(date.getTime(), new DailySummaryCallbacks() {
//            @Override
//            public void onSuccess(DailySummaryData dailySummaryData) {
//                double timeSpentAtWork = getTimeSpentAtWork(dailySummaryData);
//                handleResultForDay(timeSpentAtWork, dateToday);
//            }
//
//            @Override
//            public void onFailure(Bundle bundle, int i) {
//                decPendingAnswers();
//            }
//        });
//
//    }

    private void handleResultForDay(double timeSpentAtWork, String dateToday) {
//        System.out.println("timeSpentAtWork: " + timeSpentAtWork / 60 / 60 + " hours");
        SaveDataHelper.addToFile(dateToday + ":" + timeSpentAtWork + ";", getActivity().getApplicationContext());
        datesToHours.put(dateToday, timeSpentAtWork);
        decPendingAnswers();
//        System.out.println("pendingAnswers-- now is: " + getPendingAnswers());
        if (getPendingAnswers() == 0) {
            if (isCustom) {
                getCustomDataForDates(customStart, customEnd);
            }
            updateTextViews();
        }
    }

    private String getTextForData(double timeSpentAtWork, int workingDays) {
        String formatteedTime = SaveDataHelper.getPrettyTimeString(timeSpentAtWork);
        double salary = salaryCalculator.geySalaryForTime(timeSpentAtWork, getActivity().getApplicationContext(), workingDays);
        if (salary == SalaryCalculator.EMPTY_VALUE) {
            return formatteedTime;
        } else {
            return formatteedTime + "- $" + salary;
        }
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

    private Date getEarliestDateMissingData(Date start, Date end) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        while (end.after(start) || SaveDataHelper.isSameDay(start, end)) {
            Double currentVal = datesToHours.get(SaveDataHelper.getStringDate(end));
            if (currentVal == null || currentVal.equals(-1.0)) {
                cal.add(Calendar.DAY_OF_YEAR, 1);
                return cal.getTime();
            }
            cal.add(Calendar.DAY_OF_YEAR, -1);
            end = cal.getTime();
        }
        return null;
    }

    private boolean collectData(int[] workingDays, Double[] timeSpentAtWork, Date current) {
        return collectDataBetweenDates(workingDays, timeSpentAtWork, current, new Date());
    }

    // is data missing
    private boolean collectDataBetweenDates(int[] workingDays, Double[] timeSpentAtWork,
                                            Date start, Date end) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        while (end.after(start) || SaveDataHelper.isSameDay(start, end)) {
            Double currentVal = datesToHours.get(SaveDataHelper.getStringDate(end));
            if (currentVal == null || currentVal.equals(-1.0)) {
                return true;
            }
            timeSpentAtWork[0] += currentVal;
            if (currentVal > 0) {
                workingDays[0]++;
            }
            cal.add(Calendar.DAY_OF_YEAR, -1);
            end = cal.getTime();
        }
        return false;
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

    private void showSpinner(boolean showSpinner) {
        ProgressBar spinner = (ProgressBar) summaryActivity.findViewById(R.id.progressBar1);
        assert spinner != null;
        if (showSpinner) {
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
        }
    }

    public void requestDailySummary(final Date date) {
        incPendingAnswers();
//        System.out.println("getting data from server for " + date);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
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
                String bearer = "Bearer " + NeuraConnection.getAccessToken(getActivity().getApplicationContext());
//                System.out.println("bearer: " + bearer);

                params.put("Authorization", bearer);
                return params;
            }
        };
        queue.add(postRequest);
    }

    private void handleHttpResultForDay(double timeSpentAtWork, String dateToday) {
//        System.out.println("timeSpentAtWork: " + timeSpentAtWork / 60 / 60 + " hours");
        SaveDataHelper.addToFile(dateToday + ":" + timeSpentAtWork + ";", getActivity().getApplicationContext());
        datesToHours.put(dateToday, timeSpentAtWork);
        decPendingAnswers();
//        System.out.println("pendingAnswers-- now is: " + getPendingAnswers());
        if (getPendingAnswers() == 0) {
            if (isCustom) {
                getCustomDataForDates(customStart, customEnd);
            }
            updateTextViews();
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
}