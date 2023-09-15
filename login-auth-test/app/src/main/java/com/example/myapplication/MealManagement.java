package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

class PatientMealStorage {

}
public class MealManagement extends AppCompatActivity {

    ScrollView scrollView=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_manage);
        scrollView = findViewById(R.id.meal_scroll);
        // Intent intent = new Intent(getApplicationContext(), Login.class);
        // startActivity(intent);

        TextView textview = new TextView(this);
        textview.setText("Hi!");
        textview.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        scrollView.addView(textview);
    }
}