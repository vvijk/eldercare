package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.util.LogStorage;
import com.example.myapplication.util.MealStorage;

public class MealBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean haveEaten = intent.getBooleanExtra("haveEaten", false);
        String mealKey = intent.getStringExtra("mealKey");
        int weekDayIndex = intent.getIntExtra("dayIndex", 0);
        String recipientUID = intent.getStringExtra("recipientUID");
        int id = intent.getIntExtra("notificationId",0);
        int noticeCount = intent.getIntExtra("noticeCount", 0);
        String logMealData = intent.getStringExtra("logMealData");
        int requestCode = intent.getIntExtra("nextAlarmsRequestCode",0);

        // System.out.println("MEAL BROADCAST " + mealKey);
        MealStorage storage = new MealStorage();
        storage.initDBConnection();

        // Using firebase directly instead of pushRefresher because it does a lot of unnecessary stuff for a simple write operation.
        storage.db_meals.child(recipientUID).child(storage.dayref(weekDayIndex)).child(mealKey).child("eaten").setValue(haveEaten);

        if(id != 0) {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.cancel(id);
        }

        LogStorage logStorage = new LogStorage();
        logStorage.initDBConnection();

        if(haveEaten) {
            // Stop reminder if you pressed "eaten", we don't need to ask caretaker anymore.
            Intent old_intent = new Intent(context, BroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, old_intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
            }

            logStorage.submitLog(LogStorage.Category.MEAL_CONFIRM, recipientUID, null, logMealData);
        } else {
            if(noticeCount == 2) {
                logStorage.submitLog(LogStorage.Category.MEAL_MISS, recipientUID, null, logMealData);
            } else {
                logStorage.submitLog(LogStorage.Category.MEAL_SKIP, recipientUID, null, logMealData);
            }
        }
    }
}
