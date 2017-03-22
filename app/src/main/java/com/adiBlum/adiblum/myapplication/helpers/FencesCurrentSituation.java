package com.adiBlum.adiblum.myapplication.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.gson.Gson;
import com.neura.resources.situation.SituationData;

public class FencesCurrentSituation {

    private static final String IS_WORK_EXIST = "isWorkExist";
    public static final String USER_SITUATION_RESULT = "UserSituation";
    private static final int RADIUS = 500;
    private static int PLACE_PICKER_REQUEST = 3;
    private static int WELCOME_REQUEST = 0;
    private static int PERMISSION_REQUEST = 4;
    boolean firstTimeHasWork = false;

    private GoogleApiClient mGoogleApiClient;

    public void fetchCurrentSituation(final Context context, Activity activity) {

        if (mGoogleApiClient == null) {
            buildApiClient(context);
        }

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("adi - no permissions");
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
            return;
        }
        Awareness.SnapshotApi.getLocation(mGoogleApiClient)
                .setResultCallback(new ResultCallback<LocationResult>() {
                    @Override
                    public void onResult(@NonNull LocationResult locationResult) {
                        if (!locationResult.getStatus().isSuccess()) {
                            System.out.println("Awareness failed");
                            return;
                        }
                        Location location = locationResult.getLocation();
                        double currentLat = location.getLatitude();
                        double currentLon = location.getLongitude();
                        SharedPreferences sharedPreferences = getSharedPreferences(context);
                        double workLat = Double.parseDouble(sharedPreferences.getString(SemanticsHelper.WORK_LAT, ""));
                        double workLon = Double.parseDouble(sharedPreferences.getString(SemanticsHelper.WORK_LON, ""));
                        System.out.println("work: " + workLat + "," + workLon );
                        System.out.println("current: " + currentLat + "," + currentLon);
                        double distance = LocationHelper.getDistance(currentLat, currentLon, workLat, workLon);
                        if (distance < RADIUS) {
                            sendSituationDataResults(context, true);
                        } else sendSituationDataResults(context, false);
                    }
                });
    }

    private void sendSituationDataResults(Context context, boolean inWork) {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent intent = new Intent(USER_SITUATION_RESULT);
        System.out.println("sendSituationDataResults update in work? " + inWork);
        intent.putExtra(USER_SITUATION_RESULT, String.valueOf(inWork));
        broadcastManager.sendBroadcast(intent);
        System.out.println("Awareness sent");
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    private void buildApiClient(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Awareness.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {

                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

}
