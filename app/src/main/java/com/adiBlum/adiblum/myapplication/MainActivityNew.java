package com.adiBlum.adiblum.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;

import com.adiBlum.adiblum.myapplication.helpers.DataFetcherService;
import com.adiBlum.adiblum.myapplication.helpers.PeriodicDataFetchTask;
import com.adiBlum.adiblum.myapplication.helpers.ShareHelper;
import com.neura.standalonesdk.util.SDKUtils;
import com.splunk.mint.Mint;

import java.util.HashMap;
import java.util.Map;

public class MainActivityNew extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String IS_FIRST_RUN = "isFirstRun";
    private Map<String, Double> datesToHours = new HashMap<>();

    private MainActivityNew view;
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
        view = this;
        isFirstTime();
        setBroadcast();
        PeriodicDataFetchTask.scheduleRepeat(getApplicationContext());
        Mint.initAndStartSession(this.getApplication(), "3c9e4d9e");
    }

    private void setBroadcast() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(DataFetcherService.DATA_FETCHER_SERVICE_RESULT)) {
                    datesToHours = (Map<String, Double>) intent.getSerializableExtra(DataFetcherService.DATA_FETCHER_SERVICE_RESULT);
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
        tabsFragment.updateViews(datesToHours);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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
                startActivityForResult(new Intent(getApplicationContext(), SaveSalaryDataActivity.class), 1);
                return true;
            case R.id.action_share:
                showShare();
                return true;
        }
        return false;
    }
}