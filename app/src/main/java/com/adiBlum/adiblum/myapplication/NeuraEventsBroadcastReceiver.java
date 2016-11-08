package com.adiBlum.adiblum.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraGCMCommandFactory;

public class NeuraEventsBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "NeuraEventsReceiver";

    public void onReceive(Context context, Intent intent) {
        if (NeuraGCMCommandFactory.getInstance().isNeuraEvent(intent)) {
            NeuraEvent event = NeuraGCMCommandFactory.getInstance().getEvent(intent);
            String eventText = event != null ? event.toString() : "couldn't parse data";
            System.out.println("received Neura event - " + eventText);
            if (event.getEventName().equals("userArrivedToWork")) {
                generateNotification(context, event, "Arrived to work", "Have a great day");
            } else if (event.getEventName().equals("userLeftWork")) {
                generateNotification(context, event, "Left work", "See your daily working time");
            }
        }
    }

    private void generateNotification(Context context, NeuraEvent event, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), context.getApplicationInfo().icon))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(event.getEventName()));

        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }
}
