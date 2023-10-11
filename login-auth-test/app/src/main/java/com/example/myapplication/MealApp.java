package com.example.myapplication;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.example.myapplication.util.PatientMealStorage;

/*
    Stuff in this class can be accessed from any activity with (App)getApplicationContext().
    This is one of the only ways to share Java objects between activities.
*/
public class MealApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // initNotifications();
    }

    public PatientMealStorage mealStorage = new PatientMealStorage();

    // public void initNotifications() {
    //     // TODO(Emarioo): "Meals" is used as name for channel but R.string.meal_channel_name might be better, so it can be
    //     //  translated?
    //     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    //         NotificationChannel channel = new NotificationChannel("meal_channel","Meals", NotificationManager.IMPORTANCE_HIGH);
    //         channel.setDescription("Notifications about meals");
    //
    //         NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    //         manager.createNotificationChannel(channel);
    //     }
    //
    // }
}
