package com.adiBlum.adiblum.myapplication.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adiBlum.adiblum.myapplication.R;
import com.adiBlum.adiblum.myapplication.helpers.DatesHelper;
import com.adiBlum.adiblum.myapplication.helpers.SaveDataHelper;
import com.adiBlum.adiblum.myapplication.model.AllLoginData;
import com.adiBlum.adiblum.myapplication.model.DateLogData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryActivity extends Fragment {

    private static final String dateFormat = "EEE, MMM d";
    private static final String hoursFormat = "HH:mm";

    private View view;
    AllLoginData allLoginData;
    ListView listview;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.getActivity();
        view = inflater.inflate(R.layout.history_list, container, false);
        return view;
    }

    public void start(AllLoginData allLoginData) {
        this.allLoginData = allLoginData;
        if (isVisible()) {
            listview = (ListView) view.findViewById(R.id.history_listView);
            setList(listview);
        }
    }

    private void setList(ListView listview) {
        Date current = DatesHelper.getOldestDay(allLoginData.getDateToLoginData());
        if (current == null) {
            current = DatesHelper.getFirstDateOfTheMonth();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        final List<Map<String, String>> list = getListOfData(current, cal);
        setAdapter(listview, list);


    }

    @NonNull
    private List<Map<String, String>> getListOfData(Date current, Calendar cal) {
        final List<Map<String, String>> list = new ArrayList<>();
//        final List<String> monthsHeader = new ArrayList<>();

        while (current.before(new Date()) || SaveDataHelper.isSameDay(current, new Date())) {
            addDateDataMap(current, list);
            cal.add(Calendar.DATE, 1);
            current = cal.getTime();
        }

        Collections.reverse(list);
        return list;
    }

    private void setAdapter(ListView listview, List<Map<String, String>> list) {
        ListViewAdapter adapter = new ListViewAdapter(this, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
//                int pos=position+1;
//                Toast.makeText(MainActivity.this, Integer.toString(pos)+" Clicked", Toast.LENGTH_SHORT).show();
            }

        });
    }

//    private void serAdapter(ListView listview, List<String> list, List<String> monthsHeader) {
//        final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(),
//                android.R.layout.simple_list_item_1, list, monthsHeader);
//        listview.setAdapter(adapter);
//
//        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, final View view,
//                                    int position, long id) {
//
//            }
//
//        });
//    }

    private void addDateDataMap(Date current, List<Map<String, String>> list) {
        Map<String, String> dataForDate = new HashMap<>();
        DateLogData dateLogData = allLoginData.getDataForDate(current);
        Double timeAtWork = dateLogData.getTotalTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        SimpleDateFormat simpleHoursFormat = new SimpleDateFormat(hoursFormat, Locale.getDefault());
        String prettyDate = simpleDateFormat.format(current);
        if (prettyDate.equals(simpleDateFormat.format(new Date()))) {
            prettyDate = "Today";
        }
        String prettyTimeString = SaveDataHelper.getPrettyTimeString(timeAtWork);
        if (timeAtWork > 0) {
            dataForDate.put("DATE", prettyDate);
            dataForDate.put("TIME_AT_PLACE", prettyTimeString);
        }
        if (timeAtWork <= 0) {
            dataForDate.put("DATE", prettyDate);
            dataForDate.put("TIME_AT_PLACE", prettyTimeString);
        }

        long firstLogin = dateLogData.getFirstLogin();
        if (firstLogin != -1) {
            String text = DatesHelper.getTimestampTime(firstLogin)+ " - ";
            if (dateLogData.getLastLogout() != -1) {
                String lastLogout = DatesHelper.getTimestampTime(dateLogData.getLastLogout());
                text += lastLogout;
            }
            dataForDate.put("RANGE", text);
        }

        System.out.println(dateLogData);

        list.add(dataForDate);
    }


}

