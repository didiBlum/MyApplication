package com.adiBlum.adiblum.myapplication.activities;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.adiBlum.adiblum.myapplication.R;

public class ListViewAdapter extends BaseAdapter {

    public List<Map<String, String>> list;
    Fragment fragment;
    TextView txtFirst;
    TextView txtSecond;
    TextView txtThird;
    TextView txtFourth;

    public ListViewAdapter(Fragment fragment, List<Map<String, String>> list) {
        super();
        this.fragment = fragment;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = fragment.getLayoutInflater(new Bundle());

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.history_col, null);
            txtFirst = (TextView) convertView.findViewById(R.id.date_col);
            txtSecond = (TextView) convertView.findViewById(R.id.time_in_place_col);
            txtThird = (TextView) convertView.findViewById(R.id.time_range_col);
        }

        Map<String, String> map = list.get(position);
        txtFirst.setText(map.get("DATE"));
        String time_at_place = map.get("TIME_AT_PLACE");
        txtSecond.setText(time_at_place);
        if (time_at_place.equals("No data") || time_at_place.equals("Out of work")) {
            txtFirst.setTextColor(Color.GRAY);
            txtSecond.setTextColor(Color.GRAY);
        }
        txtThird.setText(map.get("RANGE"));
        return convertView;
    }

}