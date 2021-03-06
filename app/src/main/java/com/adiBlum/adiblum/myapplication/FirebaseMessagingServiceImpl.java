package com.adiBlum.adiblum.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class FirebaseMessagingServiceImpl extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String, String> data = message.getData();
        if (NeuraPushCommandFactory.getInstance().isNeuraEvent(data)) {
            NeuraEvent event = NeuraPushCommandFactory.getInstance().getEvent(data);
            String eventText = event != null ? event.toString() : "couldn't parse data";
            System.out.println("received Neura event - " + eventText);

            assert event != null;
            String format = getTime(event);
            if (event.getEventName().equals("userArrivedToWork")) {
                generateNotification(getApplicationContext(), event, "Arrived to work at " + format, "");
            } else if (event.getEventName().equals("userLeftWork")) {
                generateNotification(getApplicationContext(), event, "Left work at " + format, "See your daily working time");
            }
        }
    }

    private String getTime(NeuraEvent event) {
        long eventTimestamp = event.getEventTimestamp();
        Date date = new Date(eventTimestamp * 1000);
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
    }

    private void generateNotification(Context context, NeuraEvent event, String title, String text) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_stat_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), context.getApplicationInfo().icon))
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(event.getEventName()));

        PendingIntent resultPendingIntent = getPendingIntent();
        builder.setContentIntent(resultPendingIntent);
        Notification notification = builder.build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((int) System.currentTimeMillis(), notification);
    }

    private PendingIntent getPendingIntent() {
        Intent resultIntent = new Intent(this, MainActivityNew.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivityNew.class);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
