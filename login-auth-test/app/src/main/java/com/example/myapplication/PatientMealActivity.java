package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.telecom.Call;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import com.example.myapplication.util.GlobalApp;
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
    PatientMealStorage getMealStorage() {
        return ((GlobalApp) getApplicationContext()).mealStorage;
    }

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
            String name = getMealStorage().nameOfPatient(patientId);
            if (name != null) {
                text_name.setText(name);
            } else {
                // error?
                text_name.setText("null");
            }
            int patientActiveMealPlanId = getMealStorage().mealPlanIdOfPatient(patientId);
            String mealPlanName = null;
            if(patientActiveMealPlanId==0) {
                mealPlanName = getResources().getString(R.string.str_no_plan);
            } else {
                mealPlanName = getMealStorage().nameOfMealPlan(patientActiveMealPlanId);
                layout_description.setPadding(20,20,20, 20); // TODO(Emarioo): Don't hardcode padding
            }
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.str_meal_plan)+": " + mealPlanName);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
            textView.setGravity(Gravity.CENTER);
            textView.setTextColor(getResources().getColor(R.color.black)); // TODO(Emarioo): Pick a better color and move it into colors.xml
            textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

            layout_description.addView(textView);

            layout_description.setOnClickListener(this);
        } else if(mealPlanId != 0){
            showPatientDay = false;
            curMealPlanId = mealPlanId;
            String name = getMealStorage().nameOfMealPlan(mealPlanId);
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
        Integer clickedWeek = (Integer)view.getTag(R.id.clicked_week);
        Integer clickedWeekDay = (Integer)view.getTag(R.id.clicked_weekday);
        Integer clickedMealPlanId = (Integer)view.getTag(R.id.clicked_meal_plan);

        if(view == btn_back) {
            Button button = (Button)view;
//            System.out.println("BACK");
            finish();
        } else if(clickedWeekDay!=null) {
            int dayIndex = clickedWeekDay;
            if(showPatientDay) {
                Intent intent = new Intent(getApplicationContext(), MealDayActivity.class);
                intent.putExtra("patientId", curPatientId);
                intent.putExtra("dayIndex", dayIndex);
                startActivity(intent);
            } else {
                Intent intent = new Intent(getApplicationContext(), MealDayActivity.class);
                intent.putExtra("mealPlanId", curMealPlanId);
                intent.putExtra("dayIndex", dayIndex);
                startActivity(intent);
            }
        } else if(clickedMealPlanId != null) {
            if(curPatientId != 0 && showPatientDay){
                getMealStorage().setMealPlanIdOfPatient(curPatientId, clickedMealPlanId);

                String mealPlanName = getMealStorage().nameOfMealPlan(clickedMealPlanId);
                TextView view_activeMealPlan = (TextView)layout_description.getChildAt(0);
                view_activeMealPlan.setText(getResources().getString(R.string.str_meal_plan) +": "+ mealPlanName);

                refreshDays();
            }
        } else if(layout_description == view){
            if(layout_description.getChildCount()>1){
                layout_description.setBackgroundColor(getResources().getColor(R.color.purple));
                TextView view_activeMealPlan = (TextView)layout_description.getChildAt(0);
                view_activeMealPlan.setTextColor(getResources().getColor(R.color.black));
                layout_description.removeViews(1,layout_description.getChildCount()-1);
            } else {
                TextView view_activeMealPlan = (TextView)layout_description.getChildAt(0);
                view_activeMealPlan.setTextColor(getResources().getColor(R.color.white));
                layout_description.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));
                int mealPlanCount = getMealStorage().countOfMealPlans();
                for(int i=0;i<mealPlanCount;i++){
                    int mealPlanId = getMealStorage().mealPlanIdFromIndex(i);
                    String name = getMealStorage().nameOfMealPlan(mealPlanId);

                    TextView textview = new TextView(layout_description.getContext());
                    textview.setText(name);
                    textview.setTextColor(getResources().getColor(R.color.black));
                    textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
                    textview.setGravity(Gravity.CENTER);
                    textview.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    textview.setTextColor(getResources().getColor(R.color.white));
                    textview.setBackgroundColor(getResources().getColor(R.color.dry_green));
                    textview.setTag(R.id.clicked_meal_plan, mealPlanId);
                    textview.setOnClickListener(this);
                    layout_description.addView(textview);
                }
            }
        } else if(clickedWeek!=null) {
            int mealPlanId = 0;
            if(showPatientDay){
                mealPlanId = getMealStorage().mealPlanIdOfPatient(curPatientId);
            } else {
                mealPlanId = curMealPlanId;
            }
            if(mealPlanId == 0)
                return;
            int mealDayCount = getMealStorage().countOfMealDays(mealPlanId);

            int dayIndex = clickedWeek;
//            System.out.println("I "+dayIndex +", C "+mealDayCount);

            LinearLayout subLayout = (LinearLayout) view;

            if(lastOpenedWeek != null) {
                lastOpenedWeek.removeViews(1,lastOpenedWeek.getChildCount()-1);
                lastOpenedWeek.setBackgroundColor(getResources().getColor(R.color.purple_dark));
            }
            if(lastOpenedWeek == subLayout) {
                lastOpenedWeek = null;
            } else {
                lastOpenedWeek = subLayout;
                subLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));
                String[] weekDays = {
                        getResources().getString(R.string.str_monday),
                        getResources().getString(R.string.str_tuesday),
                        getResources().getString(R.string.str_wednesday),
                        getResources().getString(R.string.str_thursday),
                        getResources().getString(R.string.str_friday),
                        getResources().getString(R.string.str_saturday),
                        getResources().getString(R.string.str_sunday)
                };

//                Calendar calendar = Calendar.getInstance();
//                calendar.get(Calendar.);

                int len = 0;
                while(len < 7 && dayIndex < mealDayCount) {
                    int weekNumber = 1 + dayIndex / 7; // TODO(Emarioo): This is flawed because of leap years.
                    int monthNumber = 1 + dayIndex / 30;
                    int dayNumber = 1 + dayIndex % 30;
                    int weekDayIndex = dayIndex % 7;
                    len++;
                    LinearLayout itemLayout = new LinearLayout(subLayout.getContext());
                    itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                    itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
                    itemLayout.setPadding(25,8,25,8); // TODO(Emarioo): don't hardcode padding
                    itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    itemLayout.setTag(R.id.clicked_weekday, dayIndex);
                    itemLayout.setOnClickListener(this);
                    subLayout.addView(itemLayout);
                    {
                        TextView textview = new TextView(subLayout.getContext());
                        textview.setText(weekDays[weekDayIndex]);
                        textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
                        textview.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        itemLayout.addView(textview);
                    }
                    {
                        TextView textview = new TextView(itemLayout.getContext());
                        textview.setText(monthNumber + "/" + dayNumber);
                        textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
                        textview.setGravity(Gravity.RIGHT);
                        textview.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
                        textview.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        if (DEBUG_COLORED_LAYOUT)
                            textview.setBackgroundColor(Color.GREEN);
                        itemLayout.addView(textview);
                    }
                    dayIndex++;
                }
            }
        }
    }

    void refreshDays(){
        // TODO(Emarioo): Optimize by reusing view instead of removing them?
        //   Another optimization would be to hide the list instead of removing and recreating them.
        scrolledLayout.removeAllViews();
        int mealPlanId = 0;
        if(showPatientDay){
            mealPlanId = getMealStorage().mealPlanIdOfPatient(curPatientId);
        } else {
            mealPlanId = curMealPlanId;
        }
        if(mealPlanId == 0)
            return;
        int dayCount = getMealStorage().countOfMealDays(mealPlanId);
        int lastWeek = -1;
        for(int dayIndex=0;dayIndex<dayCount;dayIndex++) {
            int weekNumber = 1 + dayIndex / 7; // TODO(Emarioo): This is flawed because of leap years.
            int monthNumber = 1 + dayIndex / 30;
            int dayNumber = 1 + dayIndex % 30;

            if(lastWeek == weekNumber) {
                continue; // week already covered;
            }
            lastWeek = weekNumber;

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setTag(R.id.clicked_week, dayIndex);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            itemLayout.setOnClickListener(this);
            scrolledLayout.addView(itemLayout);
            {
                TextView textview = new TextView(itemLayout.getContext());
                textview.setText(getResources().getString(R.string.str_week)+" " + weekNumber);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
                textview.setGravity(Gravity.CENTER);
                textview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(textview);
            }
        }
    }
}