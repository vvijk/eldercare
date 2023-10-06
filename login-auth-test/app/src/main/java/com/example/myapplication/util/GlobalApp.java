package com.example.myapplication.util;

import android.app.Application;

/*
    Stuff in this class can be accessed from any activity with (App)getApplicationContext().
    This is one of the only ways to share Java objects between activities.
*/
public class GlobalApp extends Application {

    public PatientMealStorage mealStorage = new PatientMealStorage();
}
