package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.PaintDrawable;
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

public class MealDayActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout scrolledLayout=null;
    TextView text_name=null;
    Button btn_back=null;
    TextView meal_day_name = null;
    TextView meal_day_date = null;
    Button btn_add_meal = null;
//    LinearLayout layout_description=null;

    boolean showPatientDay = false;
    int curMealPlanId = 0;
    int curPatientId = 0;
    int curDayIndex = 0;

    boolean DEBUG_COLORED_LAYOUT = false;
    PatientMealStorage mealStorage = new PatientMealStorage();

    int getMealPlanId() {
        if(showPatientDay) {
            return mealStorage.mealPlanIdOfPatient(curPatientId);
        } else {
            return curMealPlanId;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_day);
        scrolledLayout = findViewById(R.id.meal_scroll);
        text_name = findViewById(R.id.meal_patient_name);
        meal_day_name = findViewById(R.id.meal_day_text);
        meal_day_date = findViewById(R.id.meal_day_date);
        btn_back = findViewById(R.id.btn_meal_day_back);
//        layout_description = findViewById(R.id.meal_patient_description);
        btn_back.setOnClickListener(this);

        btn_add_meal = findViewById(R.id.btn_add_meal);
        btn_add_meal.setOnClickListener(this);

        Intent intent = getIntent();
        int patientId = intent.getIntExtra("patientId", 0);
        int mealPlanId = intent.getIntExtra("mealPlanId", 0);
        int dayIndex = intent.getIntExtra("dayIndex", -1);
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
//            int patientActiveMealPlanId = mealStorage.mealPlanIdOfPatient(patientId);
//            String mealPlanName = null;
//            if(patientActiveMealPlanId==0) {
//                mealPlanName = "No plan";  // TODO(Emarioo): Don't hardcode text
//            } else {
//                mealPlanName = mealStorage.nameOfMealPlan(patientActiveMealPlanId);
//                layout_description.setPadding(20,20,20, 20); // TODO(Emarioo): Don't hardcode padding
//            }
//            TextView textView = new TextView(this);
//            textView.setText("Meal plan: " + mealPlanName); // TODO(Emarioo): Don't hardcode text
//            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
//            textView.setGravity(Gravity.CENTER);
//            textView.setTextColor(Color.BLACK); // TODO(Emarioo): Pick a better color and move it into colors.xml
//            textView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
//            textView.setLayoutParams(new ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT));
//            layout_description.addView(textView);
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

        // NOTE(Emarioo): Copy pasted from PatientMealActivity.
        int weekNumber = 1 + dayIndex / 7; // TODO(Emarioo): This is flawed because of leap years.
        int monthNumber = 1 + dayIndex / 30;
        int dayNumber = 1 + dayIndex % 30;
        int weekDayIndex = dayIndex % 7;

        String[] weekDays = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"}; // TODO(Emarioo): Don't hardcode text
        meal_day_name.setText(weekDays[weekDayIndex]);
        meal_day_date.setText(monthNumber + "/" + dayNumber);

        refreshMeals();
    }

    @Override
    public void onClick(View view) {
        Integer mealIndex = (Integer)view.getTag(R.id.clicked_mealIndex);
        Integer deleteMealIndex = (Integer)view.getTag(R.id.clicked_deleteMeal);
        if(view == btn_back) {
            Button button = (Button)view;
            System.out.println("BACK");
            finish();
        } else if (view == btn_add_meal) {
            mealStorage.addMeal(getMealPlanId(), curDayIndex, getResources().getString(R.string.default_meal_name));
            refreshMeals();
        } else if (mealIndex != null) {
            LinearLayout itemLayout = (LinearLayout) view;

            int mealPlanId = 0;
            if(showPatientDay){
                mealPlanId = mealStorage.mealPlanIdOfPatient(curPatientId);
            } else {
                mealPlanId = curMealPlanId;
            }
            if(mealPlanId == 0)
                return;
            int mealDayCount = mealStorage.countOfMealDays(mealPlanId);

            int dayIndex = curDayIndex;

            if(itemLayout.getChildCount() > 1) {
                itemLayout.removeViews(1,itemLayout.getChildCount()-1);
                itemLayout.setBackgroundColor(getResources().getColor(R.color.purple_dark));
            } else {
                String description = mealStorage.descriptionOfMeal(mealPlanId, dayIndex, mealIndex);
                itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));

                LinearLayout subLayout = new LinearLayout(itemLayout.getContext());
                subLayout.setOrientation(LinearLayout.VERTICAL);
                subLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
                subLayout.setPadding(25,10,25,12); // TODO(Emarioo): don't hardcode padding
                subLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(subLayout);
                if(description.length() == 0){
                    TextView textview = new TextView(itemLayout.getContext());
                    textview.setText("No description"); // TODO(Emarioo): Don't hardcode text
                    textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // TODO(Emarioo): Don't hardcode text size
                    textview.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    subLayout.addView(textview);
                } else {
                    TextView textview = new TextView(itemLayout.getContext());
                    textview.setText(description);
                    textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
                    textview.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    subLayout.addView(textview);
                }
                LinearLayout buttonLayout = new LinearLayout(this);
                buttonLayout.setOrientation(LinearLayout.HORIZONTAL);
                buttonLayout.setGravity(Gravity.RIGHT);
                buttonLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                subLayout.addView(buttonLayout);

                Button delButton = new Button(itemLayout.getContext());
                delButton.setAllCaps(false);
                delButton.setText(getResources().getString(R.string.str_delete));
                delButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // TODO(Emarioo): Don't hardcode text size
                delButton.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                GradientDrawable shape = new GradientDrawable();
                shape.setCornerRadius(16);
                shape.setColor(getResources().getColor(R.color.delete_button));
                delButton.setBackground(shape);
                delButton.setTag(R.id.clicked_deleteMeal, mealIndex);
                delButton.setOnClickListener(this);
                buttonLayout.addView(delButton);
            }
        } else if(deleteMealIndex != null) {
            mealStorage.deleteMeal(getMealPlanId(),curDayIndex,deleteMealIndex);
            refreshMeals();
        }
    }

    void refreshMeals() {
        int mealPlanId = 0;
        if(showPatientDay) {
            mealPlanId = mealStorage.mealPlanIdOfPatient(curPatientId);
        } else {
            mealPlanId = curMealPlanId;
        }
        int mealCount = mealStorage.countOfMeals(mealPlanId, curDayIndex);

        scrolledLayout.removeAllViews();

        if(mealCount == 0){
            TextView textView = new TextView(this);
            textView.setText("There are no meals"); // TODO(Emarioo): Don't hardcode text
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20); // TODO(Emarioo): Don't hardcode text size
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            scrolledLayout.addView(textView);
        } else {
            for(int mealIndex=0;mealIndex<mealCount;mealIndex++) {
                String name = mealStorage.nameOfMeal(mealPlanId, curDayIndex, mealIndex);
                int hour = mealStorage.hourOfMeal(mealPlanId, curDayIndex, mealIndex);
                int minute = mealStorage.minuteOfMeal(mealPlanId, curDayIndex, mealIndex);

                // layout that expands and shows description and other information regarding the meal
                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.LEFT);
                scrolledLayout.addView(itemLayout);

                itemLayout.setTag(R.id.clicked_mealIndex, mealIndex);
                itemLayout.setOnClickListener(this);

                // layout for the meal's name and time
                LinearLayout headLayout = new LinearLayout(this);
                headLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                headLayout.setGravity(Gravity.LEFT);
                itemLayout.addView(headLayout);
                {
                    TextView textView = new TextView(this);
                    textView.setText(name);
                    textView.setPadding(25,8,25,8); // TODO(Emarioo): don't hardcode padding
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
                    textView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    headLayout.addView(textView);
                }
                {
                    TextView textView = new TextView(this);
                    textView.setPadding(25,8,25,8); // TODO(Emarioo): don't hardcode padding
                    // TODO(Emarioo): Make a function for code below: String formatClock(hour, minute)
                    String timeStr = "";
                    if(hour < 10) timeStr += "0";
                    timeStr += hour + ":";
                    if(minute < 10) timeStr += "0";
                    timeStr += minute;
                    textView.setText(timeStr); // TODO(Emarioo): Don't hardcode text

                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
                    textView.setGravity(Gravity.RIGHT);
                    textView.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    headLayout.addView(textView);
                }
            }
        }
    }
}