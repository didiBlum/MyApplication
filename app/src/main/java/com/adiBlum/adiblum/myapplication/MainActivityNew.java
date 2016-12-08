package com.adiBlum.adiblum.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.adiBlum.adiblum.myapplication.helpers.DataFetcherService;
import com.adiBlum.adiblum.myapplication.helpers.PeriodicDataFetchTask;
import com.adiBlum.adiblum.myapplication.helpers.SaveDataHelper;
import com.adiBlum.adiblum.myapplication.helpers.ShareHelper;
import com.neura.standalonesdk.util.SDKUtils;
import com.splunk.mint.Mint;

import java.util.HashMap;
import java.util.Map;

public class MainActivityNew extends AppCompatActivity {

    private static final String IS_FIRST_RUN = "isFirstRun";
    private Map<String, Double> datesToHours = new HashMap<>();

    private MainActivityNew view;
    private PagerAdapter adapter;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_viewer);
        view = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TabLayout tabLayout = setTabs();
        setPageViewer(tabLayout);
        isFirstTime();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                datesToHours = (Map<String, Double>) intent.getSerializableExtra(DataFetcherService.DATA_FETCHER_SERVICE_RESULT);
                showReadyData();
            }
        };

        PeriodicDataFetchTask.scheduleRepeat(getApplicationContext());
        Mint.initAndStartSession(this.getApplication(), "3c9e4d9e");
    }

    private void mainFlow() {
        datesToHours = SaveDataHelper.getDataFromFile(this.getApplicationContext());
        connectToNeura();
    }

    private void connectToNeura() {
        NeuraConnection.initNeuraConnection(this);
        if (!SDKUtils.isConnected(this, NeuraConnection.getmNeuraApiClient())) {
            NeuraConnection.authenticateNeura(this, this);
        } else {
            askForData();
        }
    }

    public synchronized void askForData() {
        if (!isDestroyed()) {
            DataFetcherService.getInstance().askForDataForDatesOfMonth(getApplicationContext());
        }
    }

    public void showReadyData() {
        showSpinner(false);
        updateViews();
    }

    private void updateViews() {
        SummaryActivity summaryActivity = (SummaryActivity) this.adapter.getItem(0);
        summaryActivity.start(datesToHours);

        HistoryActivity historyActivity = (HistoryActivity) this.adapter.getItem(1);
        historyActivity.start(datesToHours);
    }

    private void setPageViewer(TabLayout tabLayout) {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        assert viewPager != null;
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        this.adapter = adapter;
    }

    @NonNull
    private TabLayout setTabs() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        assert tabLayout != null;
        tabLayout.addTab(tabLayout.newTab().setText("Summary"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        return tabLayout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, SaveSalaryDataActivity.class), 1);
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

    private void isFirstTime() {
        Boolean isFirstRun = getSharedPreferences(this.getApplicationContext())
                .getBoolean(IS_FIRST_RUN, true);
        if (isFirstRun) {
            getSharedPreferences(this.getApplicationContext()).edit().putBoolean(IS_FIRST_RUN, false).commit();
            showWelcome();
        } else {
            mainFlow();
        }
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    private void showWelcome() {
        System.out.println("first time");
        startActivityForResult(new Intent(this, WelcomeActivity.class), 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mainFlow();
    }

    private void showSpinner(boolean showSpinner) {
        ProgressBar spinner = (ProgressBar) view.findViewById(R.id.progressBar1);
        assert spinner != null;
        if (showSpinner) {
            spinner.setVisibility(View.VISIBLE);
        } else {
            spinner.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(DataFetcherService.DATA_FETCHER_SERVICE_RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

}