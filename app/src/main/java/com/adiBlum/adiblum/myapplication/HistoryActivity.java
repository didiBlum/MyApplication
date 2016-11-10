package com.adiBlum.adiblum.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends Activity {

    private static final String dateFormat = "EEE, MMM d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_list);

        final ListView listview = (ListView) findViewById(R.id.history_listView);

        Intent intent = getIntent();
        Map<String, Double> data = (Map<String, Double>)intent.getSerializableExtra("data");

        Date current = DatesHelper.getFirstDateOfTheMonth();
        Calendar cal = Calendar.getInstance();
        cal.setTime(current);
        final ArrayList<String> list = new ArrayList<>();

        while (current.before(new Date()) || SaveDataHelper.isSameDay(current, new Date())) {
            String stringDate = SaveDataHelper.getStringDate(current);
            Double timeAtWork = data.get(stringDate);
            String prettyDate = new SimpleDateFormat(dateFormat).format(current);
            list.add(prettyDate + ": " + SaveDataHelper.getPrettyTimeString(timeAtWork));
            cal.add(Calendar.DATE, 1);
            current = cal.getTime();
        }

        final StableArrayAdapter adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1, list);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

            }

        });
    }

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
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

    }

}

