package com.example.myapplication;

import android.app.AlarmManager;
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
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        String time = intent.getStringExtra("time");
        String desc = intent.getStringExtra("desc");
        int noticeCount = intent.getIntExtra("noticeCount", 0);
        int requestCode = intent.getIntExtra("requestCode", 0);
        long alarmAtMillis = intent.getLongExtra("alarmAtMillis", 0);

        String mealKey = intent.getStringExtra("mealKey");
        int weekDayIndex = intent.getIntExtra("dayIndex", 0);
        String caretakerUUID = intent.getStringExtra("caretakerUUID");

        if(noticeCount < 2) { // 1 initial reminder, 2 reminders of the reminder
            // setup the next notification 45 minutes later
            long nextNotice = alarmAtMillis + 45 * 60 * 1000; // 45 minutes later
            // long nextNotice = alarmAtMillis + 30 * 1000; // debug

            // NOTE(Emarioo): Is it safe to reuse intent?
            intent.putExtra("noticeCount", noticeCount + 1); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
            intent.putExtra("alarmAtMillis", nextNotice); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextNotice, pendingIntent);
        }

        // Toast.makeText(context, "Reminder broadcast yay " + meal, Toast.LENGTH_SHORT).show();

        createNotification(context, name, time, desc, mealKey, weekDayIndex, caretakerUUID, noticeCount > 0, requestCode);
    }

    private void createNotification(Context context, String name, String time, String desc, String mealKey, int weekDayIndex,
                                    String caretakerUUID, boolean withActions, int alarmRequestCode) {
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
            intent.putExtra("nextAlarmsRequestCode", alarmRequestCode);
            intent.putExtra("haveEaten", true);
            intent.putExtra("caretakerUUID", caretakerUUID);
            intent.putExtra("dayIndex", weekDayIndex);
            intent.putExtra("mealKey", mealKey);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_EATEN, intent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);

            Intent intent_not = new Intent(context, MealBroadcast.class);
            intent_not.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent_not.putExtra("notificationId", NOTIFICATION_ID);
            intent_not.putExtra("nextAlarmsRequestCode", alarmRequestCode);
            intent_not.putExtra("haveEaten",false);
            intent_not.putExtra("caretakerUUID", caretakerUUID);
            intent_not.putExtra("dayIndex", weekDayIndex);
            intent_not.putExtra("mealKey", mealKey);
            PendingIntent pendingIntent_not = PendingIntent.getBroadcast(context, REQUEST_CODE_NOT_EATEN, intent_not, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);

            builder.addAction(new NotificationCompat.Action(0, context.getResources().getString(R.string.notif_eaten), pendingIntent));
            builder.addAction(new NotificationCompat.Action(0, context.getResources().getString(R.string.notif_not_eaten), pendingIntent_not));
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

