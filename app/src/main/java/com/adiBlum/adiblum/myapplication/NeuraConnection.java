package com.adiBlum.adiblum.myapplication;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.neura.resources.authentication.AuthenticateCallback;
import com.neura.resources.authentication.AuthenticateData;
import com.neura.resources.data.PickerCallback;
import com.neura.sdk.callbacks.GetPermissionsRequestCallbacks;
import com.neura.sdk.object.AuthenticationRequest;
import com.neura.sdk.object.Permission;
import com.neura.sdk.service.SubscriptionRequestCallbacks;
import com.neura.standalonesdk.service.NeuraApiClient;
import com.neura.standalonesdk.util.Builder;
import com.neura.standalonesdk.util.SDKUtils;

import java.util.ArrayList;
import java.util.List;

public class NeuraConnection {

    public static NeuraApiClient getmNeuraApiClient() {
        return mNeuraApiClient;
    }

    private static NeuraApiClient mNeuraApiClient;
    public static final String USER_TOKEN = "userToken";

    public static void initNeuraConnection(Context ctx) {
        Builder builder = new Builder(ctx); //Mandatory
        mNeuraApiClient = builder.build();
        mNeuraApiClient.setAppUid(ctx.getResources().getString(R.string.app_uid_production)); //Mandatory
        mNeuraApiClient.setAppSecret(ctx.getResources().getString(R.string.app_secret_production)); //Mandatory
        mNeuraApiClient.enableLogFile(true); //Optional
        mNeuraApiClient.enableNeuraHandingStateAlertMessages(false);
        mNeuraApiClient.connect(); //Mandatory
    }

    public static void authenticateNeura(final Context ctx, final MainActivityNew mainActivity) {
        mNeuraApiClient.getAppPermissions(new GetPermissionsRequestCallbacks() {
            @Override
            public IBinder asBinder() {
                return null;
            }

            @Override
            public void onSuccess(final List<Permission> permissions) throws RemoteException {
                Handler uiHandler = new Handler(Looper.getMainLooper());

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<Permission> mPermissions = new ArrayList<>(permissions);
                        AuthenticationRequest mAuthenticateRequest = new AuthenticationRequest();
                        mAuthenticateRequest.setAppId(mNeuraApiClient.getAppUid());
                        mAuthenticateRequest.setAppSecret(mNeuraApiClient.getAppSecret());
                        mAuthenticateRequest.setPermissions(mPermissions);
                        mNeuraApiClient.authenticate(mAuthenticateRequest, new AuthenticateCallback() {
                            @Override
                            public void onSuccess(AuthenticateData authenticateData) {
                                System.out.println("Successfully authenticate with neura. "
                                        + "NeuraUserId = " + authenticateData.getNeuraUserId() + " "
                                        + "AccessToken = " + authenticateData.getAccessToken());
                                String userToken = authenticateData.getAccessToken();
                                saveAccessToken(userToken);
                                setAction(ctx);
                                subscribeToEvents(mPermissions);
                                mNeuraApiClient.registerFirebaseToken(
                                        mainActivity, FirebaseInstanceId.getInstance().getToken());
                                System.out.println("token is: " + FirebaseInstanceId.getInstance().getToken());
                                mainActivity.askForData();
                            }

                            private void saveAccessToken(String userToken) {
                                SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
                                editor.putString(USER_TOKEN, userToken);
                                editor.apply();
                            }

                            @Override
                            public void onFailure(int i) {
                                Log.e(getClass().getSimpleName(), "Failed to authenticate with neura. "
                                        + "Reason : " + SDKUtils.errorCodeToString(i));
                            }
                        });
                    }
                };
                uiHandler.post(runnable);
            }

            @Override
            public void onFailure(Bundle resultData, int errorCode) throws RemoteException {
                System.out.println("onFailure " + errorCode);
            }
        });
    }

    private static void setAction(final Context ctx) {

//        if (SDKUtils.isConnected(ctx, mNeuraApiClient)) {
        String eventName = "userArrivedToWork";
        if (mNeuraApiClient.isMissingDataForEvent(eventName)) { //1
            mNeuraApiClient.getMissingDataForEvent(eventName, new PickerCallback() {
                @Override
                public void onResult(boolean b) {
                    //Result when returning from home picker
//                        setCurrentUserLocation(ctx);
                }
            });
        }
//        }
    }

    public static String getAccessToken(Context ctx) {
        return getSharedPreferences(ctx).getString(USER_TOKEN, "");
    }

    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static NeuraApiClient getClient() {
        return mNeuraApiClient;
    }

    private static void subscribeToEvents(ArrayList<Permission> mPermissions) {
        for (int i = 0; i < mPermissions.size(); i++) {
            mNeuraApiClient.subscribeToEvent(mPermissions.get(i).getName(),
                    "YourEventIdentifier_" + mPermissions.get(i).getName(), true,
                    new SubscriptionRequestCallbacks() {
                        @Override
                        public void onSuccess(String eventName, Bundle bundle, String s1) {
                            Log.i(getClass().getSimpleName(),
                                    "Successfully subscribed to event " + eventName);
                        }

                        @Override
                        public void onFailure(String eventName, Bundle bundle, int i) {
                            Log.e(getClass().getSimpleName(),
                                    "Failed to subscribe to event " + eventName);
                        }
                    });
        }
    }

}
