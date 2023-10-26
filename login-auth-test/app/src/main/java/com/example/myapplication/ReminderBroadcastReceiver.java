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

import com.example.myapplication.util.LogStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    // public final int REMINDER_DELAY_SECONDS = 45 * 60; // seconds
    public static final int REMINDER_DELAY_SECONDS = 15; // debug

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
        String recipientUID = intent.getStringExtra("recipientUID");

        LogStorage logStorage = new LogStorage();
        logStorage.initDBConnection();

        if(noticeCount < 2) { // 1 initial reminder, 2 reminders of the reminder
            // setup the next notification
            long nextNotice = alarmAtMillis + REMINDER_DELAY_SECONDS * 1000;

            // NOTE(Emarioo): Is it safe to reuse intent?
            intent.putExtra("noticeCount", noticeCount + 1); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
            intent.putExtra("alarmAtMillis", nextNotice); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextNotice, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, nextNotice, pendingIntent);
                }
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, nextNotice, pendingIntent);
            }
        } else {

        }

        // Toast.makeText(context, "Reminder broadcast yay " + meal, Toast.LENGTH_SHORT).show();

        createNotification(context, name, time, desc, mealKey, weekDayIndex, recipientUID, noticeCount > 0, requestCode, noticeCount);
    }

    private void createNotification(Context context, String name, String time, String desc, String mealKey, int weekDayIndex,
                                    String recipientUID, boolean withActions, int alarmRequestCode, int noticeCount) {
        final String CHANNEL_ID = "meals_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
            String channelName = context.getResources().getString(R.string.channel_meals_name);
            if(channel == null)
                channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(context.getResources().getString(R.string.channel_meals_desc));

            manager.createNotificationChannel(channel);
        }

        final int NOTIFICATION_ID = 9213;
        final int REQUEST_CODE_EATEN = 31231;
        final int REQUEST_CODE_NOT_EATEN = 31232;
        final int REQUEST_CODE_DISMISS = 31233;
        String logMealData = time + " "+name + ": "+desc;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(name + " " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if(noticeCount != 0) {
            Intent dismiss_intent = new Intent(context, MealBroadcast.class);
            dismiss_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            dismiss_intent.putExtra("notificationId", NOTIFICATION_ID);
            dismiss_intent.putExtra("nextAlarmsRequestCode", alarmRequestCode);
            dismiss_intent.putExtra("haveEaten", false);
            dismiss_intent.putExtra("recipientUID", recipientUID);
            dismiss_intent.putExtra("dayIndex", weekDayIndex);
            dismiss_intent.putExtra("mealKey", mealKey);
            dismiss_intent.putExtra("noticeCount", noticeCount);
            dismiss_intent.putExtra("logMealData", logMealData);
            PendingIntent dismiss_pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_DISMISS, dismiss_intent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setDeleteIntent(dismiss_pendingIntent);
        }

        if(withActions) {
            Intent intent = new Intent(context, MealBroadcast.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("notificationId", NOTIFICATION_ID);
            intent.putExtra("nextAlarmsRequestCode", alarmRequestCode);
            intent.putExtra("haveEaten", true);
            intent.putExtra("recipientUID", recipientUID);
            intent.putExtra("dayIndex", weekDayIndex);
            intent.putExtra("mealKey", mealKey);
            intent.putExtra("noticeCount", noticeCount);
            intent.putExtra("logMealData", logMealData);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_EATEN, intent, PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);

            Intent intent_not = new Intent(context, MealBroadcast.class);
            intent_not.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent_not.putExtra("notificationId", NOTIFICATION_ID);
            intent_not.putExtra("nextAlarmsRequestCode", alarmRequestCode);
            intent_not.putExtra("haveEaten",false);
            intent_not.putExtra("recipientUID", recipientUID);
            intent_not.putExtra("dayIndex", weekDayIndex);
            intent_not.putExtra("mealKey", mealKey);
            intent_not.putExtra("noticeCount", noticeCount);
            intent_not.putExtra("logMealData", logMealData);
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

