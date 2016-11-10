package com.adiBlum.adiblum.myapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neura.standalonesdk.events.NeuraEvent;
import com.neura.standalonesdk.events.NeuraPushCommandFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirebaseMessagingServiceImpl extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Map data = message.getData();
        if (NeuraPushCommandFactory.getInstance().isNeuraEvent(data)) {
            NeuraEvent event = NeuraPushCommandFactory.getInstance().getEvent(data);
            String eventText = event != null ? event.toString() : "couldn't parse data";
            System.out.println("received Neura event - " + eventText);
            if (event.getEventName().equals("userArrivedToWork")) {
                generateNotification(getApplicationContext(), event, "Arrived to work", "Have a great day!");
            } else if (event.getEventName().equals("userLeftWork")) {
                generateNotification(getApplicationContext(), event, "Left work", "See your daily working time");
            }
        }
    }

//    private String getArrivedToWorkMessage(){
//        List<String> messages = new ArrayList<>();
//        messages.add("Have a great day!");
//        messages.add("");
//        messages.add("");
//        messages.add("");
//        messages.add("");
//    }

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
