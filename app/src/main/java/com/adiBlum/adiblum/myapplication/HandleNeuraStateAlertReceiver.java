package com.adiBlum.adiblum.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;

import com.neura.android.statealert.SensorsManager;
import com.neura.standalonesdk.util.NeuraStateAlertReceiver;

public class HandleNeuraStateAlertReceiver extends NeuraStateAlertReceiver {

    @Override
    public void onDetectedMissingPermission(Context context, String permission) {
        Toast.makeText(context, "Neura detected mission permission : " + permission, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDetectedMissingPermissionAfterUserPressedNeverAskAgain(Context context, String permission) {
        Toast.makeText(context, "Neura detected mission permission BUT user already pressed 'Never ask again': " + permission, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSensorDisabled(Context context, SensorsManager.Type sensorType) {
        if (sensorType.equals(SensorsManager.Type.location)) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle("Location is disabled")
                    .setContentText("Enable it to help us track your working hours")
                    .setSmallIcon(R.drawable.ic_stat_icon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), context.getApplicationInfo().icon))
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis());

            PendingIntent resultPendingIntent = getPendingIntent(context);
            builder.setContentIntent(resultPendingIntent);
            Notification notification = builder.build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify((int) System.currentTimeMillis(), notification);
        }

        //You may open the settings with an intent, in an activity's context :
        //startActivityForResult(new Intent(SensorsManager.getInstance().getSensorAction(sensorType), REQUEST_CODE));
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent resultIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivityNew.class);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}