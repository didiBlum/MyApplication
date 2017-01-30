package com.adiBlum.adiblum.myapplication.activities;

import android.content.Context;
import android.os.Bundle;
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

    private View view;
    AllLoginData allLoginData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.getActivity();
        view = inflater.inflate(R.layout.history_list, container, false);
        return view;
    }

    public void start(AllLoginData allLoginData) {
        this.allLoginData = allLoginData;
        if (isVisible()) {
            final ListView listview = (ListView) view.findViewById(R.id.history_listView);
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
        final List<String> list = new ArrayList<>();
        final List<String> monthsHeader = new ArrayList<>();

        while (current.before(new Date()) || SaveDataHelper.isSameDay(current, new Date())) {
            addDateData(current, list, monthsHeader);
            cal.add(Calendar.DATE, 1);
            current = cal.getTime();
        }

        Collections.reverse(list);

        final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, list, monthsHeader);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

            }

        });
    }

    private void addDateData(Date current, List<String> list, List<String> monthsIndex) {
        Double timeAtWork = allLoginData.getDataForDate(current).getTotalTime();
        String prettyDate = new SimpleDateFormat(dateFormat, Locale.getDefault()).format(current);
        if (prettyDate.equals(new SimpleDateFormat(dateFormat, Locale.getDefault()).format(new Date()))) {
            prettyDate = "Today";
        }
        String prettyTimeString = SaveDataHelper.getPrettyTimeString(timeAtWork);
        if (timeAtWork > 0) {
            list.add("<b>" + prettyDate + ":</b> " + prettyTimeString);
        }
        if (timeAtWork <= 0) {
            list.add("<font color=#9fa8b7><b>" + prettyDate + ":</b> " + prettyTimeString + "</font>");
        }

        if (DatesHelper.getLastDayOfMonth(current).equals(current)) {
            String monthName = DatesHelper.getMonthName(current);
            list.add("<font color=#29ABE1><b>" + monthName  + "</b></font>");
            monthsIndex.add(monthName);
        }
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<>();
        List<String> headers;

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects, List<String> headers) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
            this.headers = headers;
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            View v = super.getView(position, view, viewGroup);

            TextView textView = (TextView) v;
            CharSequence text = textView.getText();
            textView.setText(Html.fromHtml(text.toString()));
            textView.setTextSize(16);

            return v;
        }
    }
}

