package com.adiBlum.adiblum.myapplication.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adiBlum.adiblum.myapplication.R;
import com.adiBlum.adiblum.myapplication.helpers.DatesHelper;
import com.adiBlum.adiblum.myapplication.helpers.SalaryCalculator;
import com.adiBlum.adiblum.myapplication.helpers.SaveDataHelper;
import com.adiBlum.adiblum.myapplication.model.AllLoginData;
import com.neura.resources.situation.SituationData;
import com.neura.resources.situation.SubSituationData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class SummaryActivity extends Fragment {

    private View summaryActivity;
    private SalaryCalculator salaryCalculator = new SalaryCalculator();
    private AllLoginData allLoginData;
    private static final String dateFormat = "MMM d";
    SituationData situationData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_with_cells, container, false);
        summaryActivity = view;
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        updateTextViews();
//        updateCurrent();
//    }

    public void start(AllLoginData allLoginData) {
        this.allLoginData = allLoginData;
        updateTextViews();
        showSpinner(false);
    }

    public void updateSituation(SituationData situationData) {
        this.situationData = situationData;
        updateCurrent();
    }

    private void updateCurrent() {
        if (isVisible() && situationData != null) {
            System.out.println("situation is: " + situationData);
            TextView textView = (TextView) summaryActivity.findViewById(R.id.currentlyAtWork);
            ImageView imageView = (ImageView) summaryActivity.findViewById(R.id.currentlyImageView);
            SubSituationData currentSituation = situationData.getCurrentSituation();
            if (isSituationInWork(currentSituation)) {
                long startTimestamp = currentSituation.getStartTimestamp();
                System.out.println("since: " + startTimestamp);
                String date = DatesHelper.getTimestampTime(startTimestamp);
                System.out.println("since date: " + startTimestamp);
                textView.setText("Currently: At Work (since: " + date + ")");
                imageView.setImageResource(R.drawable.inside);
            } else {
                SubSituationData previousSituation = situationData.getPreviousSituation();
                if (isSituationInWork(previousSituation)) {
                    long startTimestamp = currentSituation.getStartTimestamp();
                    String date = DatesHelper.getTimestampTime(startTimestamp);
                    textView.setText("Currently: Out of work (left at " + date + ")");
                } else {
                    textView.setText("Currently: Out of work");
                }
                imageView.setImageResource(R.drawable.outside);
            }
        }
    }

    private boolean isSituationInWork(SubSituationData situationData) {
        return situationData != null &&
                situationData.getPlace() != null &&
                "work".equals(situationData.getPlace().getSemanticType());
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void updateTextViews() {
        if (isVisible() && allLoginData != null) {
            updateToday();
            updateWeek();
            updateMonth();
        }
    }

    private void showSpinner(boolean showSpinner) {
        if (summaryActivity == null) return;
        ProgressBar spinner = (ProgressBar) summaryActivity.findViewById(R.id.progressBar);
        assert spinner != null;
        if (showSpinner) {
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
        }
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
            titleView.setText(titleView.getText() + " (since " + new SimpleDateFormat(dateFormat, Locale.getDefault()).format(earliestDate) + ")");
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
        System.out.println("update today with - " + allLoginData.getDateToLoginData().size());
        System.out.println("update today with - " + allLoginData);
        TextView textView = (TextView) summaryActivity.findViewById(R.id.dailyTextView);
        Double timeSpentAtWork = allLoginData.getDataForDate(new Date(), getContext()).getTotalTime();
        int workingDays = 0;
        if (timeSpentAtWork > 0) {
            workingDays++;
        }
        String time = getTextForData(timeSpentAtWork, workingDays);
        if (textView != null) {
            textView.setText(time);
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

    private Date getEarliestDateMissingData(Date start, Date end) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        while (end.after(start) || SaveDataHelper.isSameDay(start, end)) {
            double currentVal = allLoginData.getDataForDate(end, getContext()).getTotalTime();
            if (currentVal == -1.0) {
                if (SaveDataHelper.isSameDay(end, new Date())) {
                    return new Date();
                } else {
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    return cal.getTime();
                }
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
            double currentVal = allLoginData.getDataForDate(end, getContext()).getTotalTime();
            if (currentVal ==-1.0) {
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
}