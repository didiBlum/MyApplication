package com.adiBlum.adiblum.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.adiBlum.adiblum.myapplication.helpers.DataFetcherService;
import com.google.gson.Gson;
import com.neura.resources.situation.SituationData;

import java.util.Map;

public class TabFragment extends Fragment {

    public static TabLayout tabLayout;
    public static ViewPager viewPager;
    int mNumOfTabs = 2;
    SummaryActivity summaryActivity = new SummaryActivity();
    HistoryActivity historyActivity = new HistoryActivity();
    MyAdapter adapter;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /**
         *Inflate tab_layout and setup Views.
         */
        view = inflater.inflate(R.layout.tab_viewer, null);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.viewpager);

        /**
         *Set an Apater for the View Pager
         */
        adapter = new MyAdapter(getChildFragmentManager());
        viewPager.setAdapter(adapter);

        /**
         * Now , this is a workaround ,
         * The setupWithViewPager dose't works without the runnable .
         * Maybe a Support Library Bug .
         */

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.setupWithViewPager(viewPager);
            }
        });

        return view;
    }

    public void updateViews(Map<String, Double> datesToHours) {
        SummaryActivity summaryActivity = (SummaryActivity) getSummary();
        summaryActivity.start(datesToHours);

        HistoryActivity historyActivity = (HistoryActivity) adapter.getItem(1);
        historyActivity.start(datesToHours);
    }

    private Fragment getSummary() {
        return adapter.getItem(0);
    }

    public void handleUserSituation(Intent intent) {
        Gson gson = new Gson();
        String strObj = intent.getStringExtra(DataFetcherService.USER_SITUATION_RESULT);
        SituationData situationData = gson.fromJson(strObj, SituationData.class);
        SummaryActivity summaryActivity = (SummaryActivity) getSummary();
        summaryActivity.updateSituation(situationData);
    }

    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return fragment with respect to Position .
         */

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return summaryActivity;
                case 1:
                    return historyActivity;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }

        /**
         * This method returns the title of the tab according to the position.
         */

        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Summary";
                case 1:
                    return "History";
            }
            return null;
        }
    }

}