package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class MealManagementActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout scrolledLayout=null;
    Button btn_mealPlans = null;
    Button btn_patients = null;

    View.OnClickListener btn_listener = null;

    PatientMealStorage getMealStorage() {
        return ((GlobalApp) getApplicationContext()).mealStorage;
    }

    boolean DEBUG_COLORED_LAYOUT = false;

    boolean showingPatients = false;

    int currentCaregiverId = 1; // TODO(Emarioo): Should come from somewhere else.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_manage);
        scrolledLayout = findViewById(R.id.meal_scroll);
        btn_mealPlans = findViewById(R.id.btn_meal_plans);
        btn_patients = findViewById(R.id.btn_patients);

        btn_listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == btn_mealPlans) {
                    refreshMealPlans();
                } else if (view == btn_patients){
                    refreshPatients();
                } else {
                    // shouldn't happen
                }
            }
        };
        btn_mealPlans.setOnClickListener(btn_listener);
        btn_patients.setOnClickListener(btn_listener);

        refreshPatients();
    }

    @Override
    public void onClick(View view) {
        Button button = (Button)view;
        if(showingPatients) {
            int patientId = (Integer) button.getTag();
//            System.out.println("Press " + getMealStorage().nameOfPatient(patientId));

            Intent intent = new Intent(getApplicationContext(), PatientMealActivity.class);
            intent.putExtra("patientId", patientId);
            startActivity(intent);
        } else {
            int mealPlanId = (Integer) button.getTag();
//            System.out.println("Press " + getMealStorage().nameOfMealPlan(mealPlanId));
            Intent intent = new Intent(getApplicationContext(), PatientMealActivity.class);
            intent.putExtra("mealPlanId", mealPlanId);
            startActivity(intent);
        }
    }
    void refreshPatients() {
        showingPatients = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn_mealPlans.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.almost_black)));
            btn_patients.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_dark)));
        } else {
            // Colors won't work
        }
        // TODO(Emarioo): Optimize by reusing view instead of removing them?
        //   Another optimization would be to hide the list instead of removing and recreating them.
        scrolledLayout.removeAllViews();
        int patientCount = getMealStorage().patientCountOfCaregiver(currentCaregiverId);
        for(int i=0;i<patientCount;i++) {
            int patientId = getMealStorage().patientIdFromIndex(currentCaregiverId,i);
            String name = getMealStorage().nameOfPatient(patientId);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrolledLayout.addView(itemLayout);

            TextView textview = new TextView(this);
            textview.setText(name);
            textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
            textview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            if(DEBUG_COLORED_LAYOUT)
                textview.setBackgroundColor(Color.GREEN);

            itemLayout.addView(textview);

            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            buttonLayout.setGravity(Gravity.RIGHT);
            if(DEBUG_COLORED_LAYOUT)
                buttonLayout.setBackgroundColor(Color.CYAN);

            itemLayout.addView(buttonLayout);

            Button button = new Button(this);
            button.setText(R.string.patient_meals_edit);
            button.setAllCaps(false);
            button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20); // TODO(Emarioo): Don't hardcode text size
            button.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setTag(patientId);
            button.setOnClickListener(this);

            buttonLayout.addView(button);
        }
    }
    void refreshMealPlans(){
        showingPatients = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn_mealPlans.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_dark)));
            btn_patients.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.almost_black)));
        } else {
            // Colors won't work
        }
        // TODO(Emarioo): Optimize by reusing view instead of removing them?
        //   Another optimization would be to hide the list instead of removing and recreating them.
        scrolledLayout.removeAllViews();
        int mealPlanCount = getMealStorage().countOfMealPlans();
        for(int i=0;i<mealPlanCount;i++) {
            int mealPlanId = getMealStorage().mealPlanIdFromIndex(i);
            String name = getMealStorage().nameOfMealPlan(mealPlanId);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrolledLayout.addView(itemLayout);

            TextView textview = new TextView(this);
            textview.setText(name);
            textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
            textview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            if(DEBUG_COLORED_LAYOUT)
                textview.setBackgroundColor(Color.GREEN);

            itemLayout.addView(textview);

            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            buttonLayout.setGravity(Gravity.RIGHT);
            if(DEBUG_COLORED_LAYOUT)
                buttonLayout.setBackgroundColor(Color.CYAN);

            itemLayout.addView(buttonLayout);

            Button button = new Button(this);
            button.setText(R.string.patient_meals_edit);
            button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20); // TODO(Emarioo): Don't hardcode text size
            button.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setTag(mealPlanId);
            button.setOnClickListener(this);

            buttonLayout.addView(button);
        }
    }
}