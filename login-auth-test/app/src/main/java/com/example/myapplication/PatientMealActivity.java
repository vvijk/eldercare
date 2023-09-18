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

import com.example.myapplication.util.PatientMealStorage;
import com.google.android.gms.actions.ItemListIntents;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class PatientMealActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout scrolledLayout=null;
    TextView text_name=null;
    Button btn_back=null;
    LinearLayout layout_description=null;

    boolean showPatientDay = false;
    int curMealPlanId = 0;
    int curPatientId = 0;

    LinearLayout lastOpenedWeek = null;

    boolean DEBUG_COLORED_LAYOUT = false;
    PatientMealStorage mealStorage = new PatientMealStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_days);
        scrolledLayout = findViewById(R.id.week_scroll);
        text_name = findViewById(R.id.text_patient_name);
        btn_back = findViewById(R.id.manage_patient_back);
        layout_description = findViewById(R.id.meal_patient_description);
        btn_back.setOnClickListener(this);

        Intent intent = getIntent();
        int patientId = intent.getIntExtra("patientId", 0);
        int mealPlanId = intent.getIntExtra("mealPlanId", 0);
        if(patientId != 0) {
            curPatientId = patientId;
            showPatientDay = true;
            String name = mealStorage.nameOfPatient(patientId);
            if (name != null) {
                text_name.setText(name);
            } else {
                // error?
                text_name.setText("null");
            }
            int patientActiveMealPlanId = mealStorage.mealPlanIdOfPatient(patientId);
            String mealPlanName = null;
            if(patientActiveMealPlanId==0) {
                mealPlanName = "No plan";  // TODO: Don't hardcode text
            } else {
                mealPlanName = mealStorage.nameOfMealPlan(patientActiveMealPlanId);
                layout_description.setPadding(20,20,20, 20); // TODO: Don't hardcode padding
            }
            TextView textView = new TextView(this);
            textView.setText("Meal plan: " + mealPlanName); // TODO: Don't hardcode text
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO: Don't hardcode text size
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(Color.BLACK); // TODO: Pick a better color and move it into colors.xml
            textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
            layout_description.addView(textView);
        } else if(mealPlanId != 0){
            showPatientDay = false;
            curMealPlanId = mealPlanId;
            String name = mealStorage.nameOfMealPlan(mealPlanId);
            if (name != null) {
                text_name.setText(name);
            } else {
                // error?
                text_name.setText("null");
            }
        } else {
            // what?
        }

        refreshDays();
    }

    @Override
    public void onClick(View view) {

        if(view == btn_back) {
            Button button = (Button)view;
            System.out.println("BACK");
            finish();
        } else {
            int dayIndex = (Integer) view.getTag();
            int mealPlanId = 0;
            if(showPatientDay){
                mealPlanId = mealStorage.mealPlanIdOfPatient(curPatientId);
            } else {
                mealPlanId = curMealPlanId;
            }
            if(mealPlanId == 0)
                return;
            int mealDayCount = mealStorage.countOfMealDays(mealPlanId);

            System.out.println("I "+dayIndex +", C "+mealDayCount);

            LinearLayout subLayout = (LinearLayout) view;

            if(lastOpenedWeek != null) {
                lastOpenedWeek.removeViews(1,lastOpenedWeek.getChildCount()-1);
                lastOpenedWeek.setBackgroundColor(getResources().getColor(R.color.purple_dark));
            }
            if(lastOpenedWeek == subLayout) {
                lastOpenedWeek = null;
            } else {
                lastOpenedWeek = subLayout;
                subLayout.setBackgroundColor(Color.rgb(27,50,15)); // TODO: don't hardcode color (dry dark green)
                String[] weekDays = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
                int len = 0;
                while(len < 7 && dayIndex < mealDayCount) {
                    int weekNumber = 1 + dayIndex / 7; // TODO: This is flawed because of leap years.
                    int monthNumber = 1 + dayIndex / 30;
                    int dayNumber = 1 + dayIndex % 30;
                    int weekDayIndex = dayIndex % 7;
                    dayIndex++;
                    len++;
                    LinearLayout itemLayout = new LinearLayout(subLayout.getContext());
                    itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                    itemLayout.setBackgroundColor(Color.rgb(24,40,10)); // TODO: don't hardcode color (dry dark green)
                    itemLayout.setPadding(25,8,25,8); // TODO: don't hardcode padding
                    itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    subLayout.addView(itemLayout);
                    {
                        TextView textview = new TextView(subLayout.getContext());
                        textview.setText(weekDays[weekDayIndex]); // TODO: Fix hardcoded string "Weak"
                        textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO: Don't hardcode text size
                        textview.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        itemLayout.addView(textview);
                    }
                    {
                        TextView textview = new TextView(itemLayout.getContext());
                        textview.setText(monthNumber + "/" + dayNumber);
                        textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO: Don't hardcode text size
                        textview.setGravity(Gravity.RIGHT);
                        textview.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
                        textview.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        if (DEBUG_COLORED_LAYOUT)
                            textview.setBackgroundColor(Color.GREEN);
                        itemLayout.addView(textview);
                    }
                }
            }
        }
    }

    void refreshDays(){
        // TODO: Optimize by reusing view instead of removing them?
        //   Another optimization would be to hide the list instead of removing and recreating them.
        scrolledLayout.removeAllViews();
        int mealPlanId = 0;
        if(showPatientDay){
            mealPlanId = mealStorage.mealPlanIdOfPatient(curPatientId);
        } else {
            mealPlanId = curMealPlanId;
        }
        if(mealPlanId == 0)
            return;
        int dayCount = mealStorage.countOfMealDays(mealPlanId);
        int lastWeek = -1;
        for(int dayIndex=0;dayIndex<dayCount;dayIndex++) {
            int weekNumber = 1 + dayIndex / 7; // TODO: This is flawed because of leap years.
            int monthNumber = 1 + dayIndex / 30;
            int dayNumber = 1 + dayIndex % 30;

            if(lastWeek == weekNumber) {
                continue; // week already covered;
            }
            lastWeek = weekNumber;

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setTag(dayIndex);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            itemLayout.setOnClickListener(this);
            scrolledLayout.addView(itemLayout);
            {
                TextView textview = new TextView(itemLayout.getContext());
                textview.setText("Weak " + weekNumber); // TODO: Fix hardcoded string "Weak"
                textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO: Don't hardcode text size
                textview.setGravity(Gravity.CENTER);
                textview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(textview);
            }
        }
    }
}