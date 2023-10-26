package com.example.myapplication;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.example.myapplication.util.MealStorage;
import com.example.myapplication.util.LogStorage;

/*
    Stuff in this class can be accessed from any activity with (App)getApplicationContext().
    This is one of the only ways to share Java objects between activities.
*/
public class MainApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public MealStorage mealStorage = new MealStorage();
    public LogStorage logStorage = new LogStorage();
}
