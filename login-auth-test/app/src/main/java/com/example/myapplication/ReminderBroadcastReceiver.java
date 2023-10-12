package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        String time = intent.getStringExtra("time");
        String desc = intent.getStringExtra("desc");
        boolean withActions = intent.getBooleanExtra("withActions", false);

        String mealKey = intent.getStringExtra("mealKey");
        int weekDayIndex = intent.getIntExtra("dayIndex", 0);
        String caretakerUUID = intent.getStringExtra("caretakerUUID");

        // ArrayList<Integer> clickedMealList = intent.getIntegerArrayListExtra("clickedMeals");
        // Set<Integer> clickedMealsSet = new HashSet<>(clickedMealList);

        // System.out.println("BROADCAST " + meal);
        //Kolla om maten är i kryssad
        // if(!clickedMealsSet.contains(meal)) {
        //     // Skicka påminnelse
        //     Toast.makeText(context, "Du har inte markerat " + meal, Toast.LENGTH_SHORT).show();
        // }
        // En else här då för att föra statestik osv...

        // Toast.makeText(context, "Reminder broadcast yay " + meal, Toast.LENGTH_SHORT).show();

        createNotification(context, name, time, desc, mealKey, weekDayIndex, caretakerUUID, withActions);
    }

    private void createNotification(Context context, String name, String time, String desc, String mealKey, int weekDayIndex,
                                    String caretakerUUID, boolean withActions) {
        final String CHANNEL_ID = "meals_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
            String channelName = context.getResources().getString(R.string.channel_meals_name);
            if(channel == null)
                channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getResources().getString(R.string.channel_meals_desc));
            // if(!channel.getName().equals(channelName)) // does equal do what you expect or is there a mistake here?
            //     channel.setName(channelName);

            manager.createNotificationChannel(channel);
        }
        // TODO: What to do with intent?
        // Create an explicit intent for an Activity in your app.
        // Intent intent = new Intent(this, AlertDetails.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // TODO: Notification with buttons to specify whether you have eaten or not.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(name + " " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                // .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        final int NOTIFICATION_ID = 9213;
        if(withActions) {
            final int REQUEST_CODE_EATEN = 31231;
            final int REQUEST_CODE_NOT_EATEN = 31232;
            Intent intent = new Intent(context, MealBroadcast.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("notificationId", NOTIFICATION_ID);
            intent.putExtra("haveEaten", true);
            intent.putExtra("caretakerUUID", caretakerUUID);
            intent.putExtra("dayIndex", weekDayIndex);
            intent.putExtra("mealKey", mealKey);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_EATEN, intent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);

            Intent intent_not = new Intent(context, MealBroadcast.class);
            intent_not.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent_not.putExtra("notificationId", NOTIFICATION_ID);
            intent_not.putExtra("haveEaten",false);
            intent_not.putExtra("caretakerUUID", caretakerUUID);
            intent_not.putExtra("dayIndex", weekDayIndex);
            intent_not.putExtra("mealKey", mealKey);
            PendingIntent pendingIntent_not = PendingIntent.getBroadcast(context, REQUEST_CODE_NOT_EATEN, intent_not, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addAction(new NotificationCompat.Action(0, "Eaten", pendingIntent));
            builder.addAction(new NotificationCompat.Action(0, "Not eaten", pendingIntent_not));
        }
        if(desc.isEmpty()) {
            builder.setContentText(context.getResources().getString(R.string.str_no_description));
        } else {
            builder.setContentText(desc);
        }
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, context.getResources().getString(R.string.missing_alarm_permission), Toast.LENGTH_LONG).show();
            return;
        }
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}

