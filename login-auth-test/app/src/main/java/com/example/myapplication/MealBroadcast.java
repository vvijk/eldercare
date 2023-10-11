package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.util.PatientMealStorage;

public class MealBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean haveEaten = intent.getBooleanExtra("haveEaten", false);
        String mealKey = intent.getStringExtra("mealKey");
        int weekDayIndex = intent.getIntExtra("dayIndex", 0);
        String caretakerUUID = intent.getStringExtra("caretakerUUID");
        int id = intent.getIntExtra("notificationId",0);

        System.out.println("MEAL BROAD " + mealKey);
        PatientMealStorage storage = new PatientMealStorage();
        storage.initDBConnection();

        // Using firebase directly instead of pushRefresher because it does a lot of unnecessary stuff for a simple write operation.
        storage.db_meals.child(caretakerUUID).child(storage.dayref(weekDayIndex)).child(mealKey).child("eaten").setValue(haveEaten);

        if(id != 0) {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.cancel(id);
        }
        // int caretakerId = storage.idFromCaretakerUUID(caretakerUUID);
        // System.out.println("caretaker id  " + caretakerId);
        // storage.pushRefresher_caretaker(caretakerId, new Runnable() {
        //     @Override
        //     public void run() {
        //         System.out.println("SET EATEN " + haveEaten);
        //         storage.caretaker_setEatenOfMeal(caretakerId, weekDayIndex, mealKey, haveEaten);
        //     }
        // });
    }
}
