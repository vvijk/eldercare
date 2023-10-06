package com.example.simplelistviewdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ReminderBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String meal = intent.getStringExtra("meal");
        ArrayList<Integer> clickedMealList = intent.getIntegerArrayListExtra("clickedMeals");
        Set<Integer> clickedMealsSet = new HashSet<>(clickedMealList);

        //Kolla om maten är i kryssad
        if(!clickedMealsSet.contains(meal)) {
            // Skicka påminnelse
            Toast.makeText(context, "Du har inte markerat " + meal, Toast.LENGTH_SHORT).show();
        }
        // En else här då för att föra statestik osv...

    }
}

