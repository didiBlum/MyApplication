//package com.adiBlum.adiblum.myapplication.activities;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.view.View;
//
//import com.adiBlum.adiblum.myapplication.R;
//import com.adiBlum.adiblum.myapplication.helpers.DatesHelper;
//import com.adiBlum.adiblum.myapplication.helpers.ShareHelper;
//import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;
//
//import java.io.IOException;
//import java.util.Date;
//
//public class ShareReportActivity extends Activity {
//
//    private Date customStart = DatesHelper.getFirstDateOfTheMonth();
//    private Date customEnd = new Date();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.set_salary);
//
//    }
//
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
//        smoothDateRangePickerFragment.show(getFragmentManager(), "smoothDateRangePicker");
//    }
//
//    private void setCustomDates(int yearStart, int monthStart, int dayStart, int yearEnd, int monthEnd, int dayEnd) {
//        Date startDate = new Date(yearStart - 1900, monthStart, dayStart);
//        Date endDate = new Date(yearEnd - 1900, monthEnd, dayEnd);
//        try {
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
//    private void showShare() {
//        Intent i = ShareHelper.getIntentForShare(allLoginData, customStart, customEnd);
//        startActivity(Intent.createChooser(i, "Share via"));
//    }
//}
