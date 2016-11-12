package com.adiBlum.adiblum.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.adiBlum.adiblum.myapplication.helpers.DatesHelper;
import com.adiBlum.adiblum.myapplication.helpers.SaveDataHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends Fragment {

    private static final String dateFormat = "EEE, MMM d";

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.getActivity();
        view = inflater.inflate(R.layout.history_list, container, false);
        final ListView listview = (ListView) view.findViewById(R.id.history_listView);
//        setList(listview);

        return view;
    }


    private void setList(ListView listview) {
        Intent intent = getActivity().getIntent();
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

        final StableArrayAdapter adapter = new StableArrayAdapter(getActivity(),
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

