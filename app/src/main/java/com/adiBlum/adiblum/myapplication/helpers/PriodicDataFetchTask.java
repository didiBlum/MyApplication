package com.adiBlum.adiblum.myapplication.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;

public class PriodicDataFetchTask extends GcmTaskService {

    public static final String DATA_FETCHER_SERVICE_RESULT = "DataFetcherServiceResult";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onRunTask(TaskParams taskParams) {
        DataFetcherService.getInstance().askForDataForDatesOfMonth(getApplicationContext());
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static void scheduleRepeat(Context context) {
        //in this method, single Repeating task is scheduled (the target service that will be called is MyTaskService.class)
        try {
            PeriodicTask periodic = new PeriodicTask.Builder()
                    //specify target service - must extend GcmTaskService
                    .setService(PriodicDataFetchTask.class)
                    //repeat every 60 * 60 seconds
                    .setPeriod(60 * 60)
                    //specify how much earlier the task can be executed (in seconds)
                    .setFlex(30)
                    //tag that is unique to this task (can be used to cancel task)
                    .setTag(DATA_FETCHER_SERVICE_RESULT)
                    //whether the task persists after device reboot
                    .setPersisted(true)
                    //if another task with same tag is already scheduled, replace it with this task
                    .setUpdateCurrent(true)
                    //set required network state, this line is optional
                    .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                    //request that charging must be connected, this line is optional
                    .setRequiresCharging(false)
                    .build();
            GcmNetworkManager.getInstance(context).schedule(periodic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PriodicDataFetchTask() {
    }
}
