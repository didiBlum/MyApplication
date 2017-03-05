package com.adiBlum.adiblum.myapplication.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.adiBlum.adiblum.myapplication.NeuraConnection;
import com.adiBlum.adiblum.myapplication.R;
import com.adiBlum.adiblum.myapplication.TabFragment;
import com.adiBlum.adiblum.myapplication.helpers.DataFetcherService;
import com.adiBlum.adiblum.myapplication.helpers.ShareHelper;
import com.adiBlum.adiblum.myapplication.model.AllLoginData;
import com.leavjenn.smoothdaterangepicker.date.SmoothDateRangePickerFragment;
import com.neura.standalonesdk.util.SDKUtils;
import com.splunk.mint.Mint;

import java.util.Calendar;
import java.util.Date;

public class MainActivityNew extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String IS_FIRST_RUN = "isFirstRun";
    private static final String INSTALLATION_DATE = "installationDate";
    public static final int SAVE_SALARY_ACTIVITY_RESULT = 1;
    public static final int WELCOME_ACTIVITY_RESULT = 0;
    public static final int SETTINGS_ACTIVITY_RESULT = 4;
    private AllLoginData allLoginData;

    private BroadcastReceiver broadcastReceiver;

    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
    FragmentManager mFragmentManager;
    FragmentTransaction mFragmentTransaction;
    TabFragment tabsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.shitstuff);
        setDrawer();
        isFirstTime();
        setBroadcast();
        Mint.initAndStartSession(this.getApplication(), "3c9e4d9e");
//        PeriodicDataFetchTask.scheduleRepeat(getApplicationContext());
    }


    private void setBroadcast() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(DataFetcherService.DATA_FETCHER_SERVICE_RESULT)) {
                    allLoginData = (AllLoginData) intent.getSerializableExtra(DataFetcherService.DATA_FETCHER_SERVICE_RESULT);
                    showReadyData();
                }
                if (intent.getAction().equals(DataFetcherService.USER_SITUATION_RESULT)) {
                    tabsFragment.handleUserSituation(intent);
                }
            }
        };
    }

    private void setDrawer() {
        /**
         * Lets inflate the very first fragment
         * Here , we are inflating the TabFragment as the first Fragment
         */
        mFragmentManager = getSupportFragmentManager();
        mFragmentTransaction = mFragmentManager.beginTransaction();
        tabsFragment = new TabFragment();
        mFragmentTransaction.replace(R.id.containerView, tabsFragment).commit();

        mNavigationView.setNavigationItemSelectedListener(this);

        /**
         * Setup Drawer Toggle of the Toolbar
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.app_name,
                R.string.app_name);

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    private void mainFlow() {
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
            DataFetcherService instance = DataFetcherService.getInstance();
            instance.askForDataForDatesOfMonth(getApplicationContext());
            instance.getUserSituation(getApplicationContext());
        }
    }

    public void showReadyData() {
        tabsFragment.updateViews(allLoginData);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void showShare(Date start, Date end) {
//        Intent i = ShareHelper.getIntentForShare(allLoginData, DatesHelper.getFirstDateOfTheMonth(), new Date());
        Intent i = ShareHelper.getIntentForShare(start, end, getApplicationContext());
        startActivity(Intent.createChooser(i, "Share via"));
    }

    private void showFeedback() throws PackageManager.NameNotFoundException {
        Intent i = ShareHelper.getIntentForFeedback(getApplicationContext());
        startActivity(Intent.createChooser(i, "Send Feedback"));
    }

    private void isFirstTime() {
        Boolean isFirstRun = getSharedPreferences(this.getApplicationContext())
                .getBoolean(IS_FIRST_RUN, true);
        if (isFirstRun) {
            getSharedPreferences(this.getApplicationContext()).edit().putBoolean(IS_FIRST_RUN, false).commit();
            getSharedPreferences(this.getApplicationContext()).edit().putLong(INSTALLATION_DATE, new Date().getTime()).apply();
            showWelcome();
        } else {
            mainFlow();
        }
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void showWelcome() {
        System.out.println("first time");
        startActivityForResult(new Intent(this, WelcomeActivity.class), WELCOME_ACTIVITY_RESULT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mainFlow();
    }


    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(DataFetcherService.DATA_FETCHER_SERVICE_RESULT)
        );
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter(DataFetcherService.USER_SITUATION_RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onStop();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawers();
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(getApplicationContext(), SaveSalaryDataActivity.class), SAVE_SALARY_ACTIVITY_RESULT);
                return true;
            case R.id.action_share:
//                showShare(DatesHelper.getFirstDateOfTheMonth(), new Date());
                setDatePicker(this.getCurrentFocus());
                return true;
            case R.id.notifications_settings:
                showSettings();
                return true;
            case R.id.feedback:
                try {
                    showFeedback();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return false;
    }

    private void showSettings() {
        startActivityForResult(new Intent(getApplicationContext(), SettingsActivity.class), SETTINGS_ACTIVITY_RESULT);
    }

    public void setDatePicker(View view) {
        Calendar calendar = Calendar.getInstance();
        SmoothDateRangePickerFragment smoothDateRangePickerFragment = SmoothDateRangePickerFragment.newInstance(
                new SmoothDateRangePickerFragment.OnDateRangeSetListener() {
                    @Override
                    public void onDateRangeSet(SmoothDateRangePickerFragment view,
                                               int yearStart, int monthStart,
                                               int dayStart, int yearEnd,
                                               int monthEnd, int dayEnd) {
                        Date startDate = new Date(yearStart - 1900, monthStart, dayStart);
                        Date endDate = new Date(yearEnd - 1900, monthEnd, dayEnd);
//                        askForData(startDate, endDate);
                        showShare(startDate, endDate); // todo - wait here!
                    }
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1);

        smoothDateRangePickerFragment.setMaxDate(calendar);
        smoothDateRangePickerFragment.show(getFragmentManager(), "smoothDateRangePicker");
    }
}