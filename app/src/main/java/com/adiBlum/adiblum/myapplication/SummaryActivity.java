package com.adiBlum.adiblum.myapplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adiBlum.adiblum.myapplication.helpers.SalaryCalculator;
import com.adiBlum.adiblum.myapplication.helpers.SaveDataHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class SummaryActivity extends Fragment {

    private View summaryActivity;
    private SalaryCalculator salaryCalculator = new SalaryCalculator();
    private Map<String, Double> datesToHours;
    private static final String dateFormat = "MMM d";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_with_cells, container, false);
        summaryActivity = view;
//        updateTextViews();
        return view;
    }

    public void start(Map<String, Double> datesToHours) {
        this.datesToHours = datesToHours;
        updateTextViews();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void updateTextViews() {
        updateToday();
        updateWeek();
        updateMonth();
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


//    public void setDatePicker(View view) {
//        SmoothDateRangePickerFragment smoothDateRangePickerFragment = SmoothDateRangePickerFragment.newInstance(
//                new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
//                    @Override
//                    public void onDateRangeSet(SmoothDateRangePickerFragment view,
//                                               int yearStart, int monthStart,
//                                               int dayStart, int yearEnd,
//                                               int monthEnd, int dayEnd) {
//                        setCustomDates(yearStart, monthStart, dayStart, yearEnd, monthEnd, dayEnd);
//                    }
//                });
//
//        smoothDateRangePickerFragment.show(getActivity().getFragmentManager(), "smoothDateRangePicker");
//    }
//
//    private void setCustomDates(int yearStart, int monthStart, int dayStart, int yearEnd, int monthEnd, int dayEnd) {
//        Date startDate = new Date(yearStart - 1900, monthStart, dayStart);
//        Date endDate = new Date(yearEnd - 1900, monthEnd, dayEnd);
//        try {
//            isCustom = true;
//            customStart = startDate;
//            customEnd = endDate;
//            if (askForDataForDates(startDate, endDate)) {
//                getCustomDataForDates(startDate, endDate);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void getCustomDataForDates(Date startDate, Date endDate) {
//        final int[] workingDays = {0};
//        final Double[] timeSpentAtWork = {0.0};
//        TextView customDataTextView = (TextView) summaryActivity.findViewById(R.id.customTextView);
//        TextView customTitleTextview = (TextView) summaryActivity.findViewById(R.id.custom_title_textview);
//        assert customDataTextView != null;
//        customDataTextView.setVisibility(View.VISIBLE);
//        String startString = new SimpleDateFormat(dateFormat).format(startDate);
//        String endString = new SimpleDateFormat(dateFormat).format(endDate);
//
//        collectDataBetweenDates(workingDays, timeSpentAtWork, startDate, endDate);
//        assert customDataTextView != null;
//        assert customTitleTextview != null;
//        customTitleTextview.setText(startString + " - " + endString);
//        if (timeSpentAtWork[0].equals(-1.0)) {
//            customDataTextView.setText("No data was found for all the days");
//        } else {
//            String time = getTextForData(timeSpentAtWork[0], workingDays[0]);
//            customDataTextView.setText(time);
//        }
//        resetCustom();
//    }
//
//    private void resetCustom() {
//        isCustom = false;
//        customStart = null;
//        customEnd = null;
//    }

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
            Double currentVal = datesToHours.get(SaveDataHelper.getStringDate(end));
            if (currentVal == null || currentVal.equals(-1.0)) {
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
}