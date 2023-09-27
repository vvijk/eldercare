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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage;
import com.google.android.gms.actions.ItemListIntents;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

// Great name i know. The person who is reading this is more than welcome to change it. - Emarioo
class TimeFixer implements TextWatcher {
    public EditText view = null;
    public TimeFixer(EditText view) {
        this.view = view;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // i: the index where characters were added or removed
        // i1: amount of removed characters
        // i2: amount of added characters
//        System.out.println("Text: "+i+" "+i1 + " "+i2);

        if(view.getText().length() > 1) {
            if(view.getText().length()>2) {
                if(view.getText().charAt(2) == ':')
                    return;
            } else {
                if (view.getText().charAt(1) == ':')
                    return;
            }
            String cleanedString = "";
            for(int j=0;j<view.getText().length();j++) {
                if(view.getText().toString().charAt(j)!=':')
                    cleanedString += view.getText().toString().charAt(j);
            }
            String str = cleanedString.toString().substring(0,2) + ":";
            if(cleanedString.length() > 2) {
                str += cleanedString.toString().substring(2);
            }
            view.setText(str);
            if(i1 > 0) {
                view.setSelection(i);
            } else if (i + i2 == 2)
                    view.setSelection(i + i2 + 1);
            else
                view.setSelection(i + i2);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}

public class MealDayActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout scrolledLayout=null;
    TextView text_name=null;
    Button btn_back=null;
    TextView meal_day_name = null;
    TextView meal_day_date = null;
    Button btn_add_meal = null;

    boolean showPatientDay = false;
    int curMealPlanId = 0;
    int curPatientId = 0;
    int curDayIndex = 0;

    boolean DEBUG_COLORED_LAYOUT = false;
    PatientMealStorage getMealStorage() {
        return ((GlobalApp) getApplicationContext()).mealStorage;
    }

    int getMealPlanId() {
        if(showPatientDay) {
            return getMealStorage().mealPlanIdOfPatient(curPatientId);
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
            String name = getMealStorage().nameOfPatient(patientId);
            if (name != null) {
                text_name.setText(name);
            } else {
                // error?
                text_name.setText("null");
            }
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
        curDayIndex = dayIndex;

        // NOTE(Emarioo): Copy pasted from PatientMealActivity.
        int weekNumber = 1 + dayIndex / 7; // TODO(Emarioo): This is flawed because of leap years.
        int monthNumber = 1 + dayIndex / 30;
        int dayNumber = 1 + dayIndex % 30;
        int weekDayIndex = dayIndex % 7;

        String[] weekDays = {
                getResources().getString(R.string.str_monday),
                getResources().getString(R.string.str_tuesday),
                getResources().getString(R.string.str_wednesday),
                getResources().getString(R.string.str_thursday),
                getResources().getString(R.string.str_friday),
                getResources().getString(R.string.str_saturday),
                getResources().getString(R.string.str_sunday)
        };
        meal_day_name.setText(weekDays[weekDayIndex]);
        meal_day_date.setText(monthNumber + "/" + dayNumber);

        refreshMeals();
        getMealStorage().pushRefresher(getMealPlanId(),dayIndex, new Runnable() {
            @Override
            public void run() {
                refreshMeals();
            }
        });
    }
    @Override
    protected void onDestroy() {
        getMealStorage().popRefresher();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        saveAllMeals(); // save before super.onStop since it might destroy stuff saveAllMeals needs

        super.onStop();
    }


    @Override
    public void onClick(View view) {
        Integer mealIndex = (Integer)view.getTag(R.id.clicked_mealIndex);
        Integer deleteMealIndex = (Integer)view.getTag(R.id.clicked_deleteMeal);
        if(view == btn_back) {
            saveAllMeals();
            Button button = (Button)view;
//            System.out.println("BACK");
            finish();
        } else if (view == btn_add_meal) {
            if(showPatientDay) {
                Toast.makeText(view.getContext(),"Cannot add meal in patient view",Toast.LENGTH_LONG).show();
            } else {
                saveAllMeals();
                getMealStorage().addMeal(getMealPlanId(), curDayIndex, getResources().getString(R.string.default_meal_name));
                // refreshMeals(); // addMeal will cause childEvent to run refresher
            }
        } else if (mealIndex != null) {
            LinearLayout itemLayout = (LinearLayout) view;

            int mealPlanId = 0;
            if(showPatientDay){
                mealPlanId = getMealStorage().mealPlanIdOfPatient(curPatientId);
            } else {
                mealPlanId = curMealPlanId;
            }
            if(mealPlanId == 0)
                return;
            int mealDayCount = 365; // TODO: Don't hardcode
            // getMealStorage().countOfMealDays(mealPlanId);

            int dayIndex = curDayIndex;

            if(itemLayout.getChildCount() > 1) {
                // NOTE(Emarioo): The "patient view" cannot modify meals so we don't need to save them
                if(!showPatientDay) {
                    saveAllMeals();
//                    saveMeal(itemLayout, mealIndex);
                }

                itemLayout.removeViews(1,itemLayout.getChildCount()-1);
                itemLayout.setBackgroundColor(getResources().getColor(R.color.purple_dark));

                LinearLayout headLayout = (LinearLayout) itemLayout.getChildAt(0);

                refreshMealHeader(headLayout, false, null, null);

            } else {
                String description = getMealStorage().descriptionOfMeal(mealPlanId, dayIndex, mealIndex);
                itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));

                LinearLayout headLayout = (LinearLayout) itemLayout.getChildAt(0);

                refreshMealHeader(headLayout, true, null, null);
                LinearLayout subLayout = new LinearLayout(itemLayout.getContext());
                subLayout.setOrientation(LinearLayout.VERTICAL);
                subLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
                subLayout.setPadding(25,10,25,12); // TODO(Emarioo): don't hardcode padding
                subLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(subLayout);

                // NOTE(Emarioo): Disabling editing of description when you click in on a patient.
                //  This is because you would modify the meal plan and thus changing the meals
                //  for other patients too. We could allow you to edit description if each
                //  patient has some kind of individual plan which wouldn't affect other patients.
                TextView editText = null;
                if(curPatientId!=0){
                    editText = new TextView(itemLayout.getContext());
                } else {
                    editText = new EditText(itemLayout.getContext());
                }

                editText.setText(description);
                editText.setHint(R.string.str_no_description);
                editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24); // TODO(Emarioo): Don't hardcode text size
                editText.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                subLayout.addView(editText);
//                    editText.setOnFocusChangeListener(this);

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
            saveAllMeals();
            getMealStorage().deleteMeal(getMealPlanId(),curDayIndex,deleteMealIndex);
//            refreshMeals();
        }
    }
    void saveMeal(LinearLayout itemLayout, int mealIndex) {
        int mealPlanId = getMealPlanId();
        int dayIndex = curDayIndex;

        // TODO(Emarioo): Only save the meals that has been modified. You don't want
        //  to spam the database. Use a list to track which meals were modified.
        //  TimeFixer (TextWatcher) knows when somethings changes.

        LinearLayout headLayout = (LinearLayout)itemLayout.getChildAt(0);
        TextView view_time = (TextView)headLayout.getChildAt(0);
        TextView view_name = (TextView)headLayout.getChildAt(1);

        String[] split = view_time.getText().toString().split(":");
        if(split.length>1) {
            try {
                int hour = Integer.parseInt(split[0]);
                int minute = Integer.parseInt(split[1]);
                getMealStorage().setHourOfMeal(mealPlanId, dayIndex, mealIndex, hour);
                getMealStorage().setMinuteOfMeal(mealPlanId, dayIndex, mealIndex, minute);
            } catch (Exception e) {
                // TODO(Emarioo): Handle parse exception. Toast the user?
                //   Tell the user which meal was bad. We shouldn't tell the user that here because this function
                //   will save the content right before exiting this activity and then it will be to late for
                //   the user to do anything about the bad format.
                Toast.makeText(this, R.string.meal_time_bad_format, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, R.string.meal_time_missing_colon, Toast.LENGTH_LONG).show();
        }
        getMealStorage().setNameOfMeal(mealPlanId,dayIndex,mealIndex, view_name.getText().toString());

        if(itemLayout.getChildCount()>1) {
            LinearLayout subLayout = (LinearLayout) itemLayout.getChildAt(1);
            TextView view_desc = (TextView) subLayout.getChildAt(0);
            getMealStorage().setDescriptionOfMeal(mealPlanId, dayIndex, mealIndex, view_desc.getText().toString());
            // Toast.makeText(this,"saved "+view_desc.getText(),Toast.LENGTH_LONG).show();
        }
    }
    void saveAllMeals() {
        int mealPlanId = getMealPlanId();
        int mealCount = getMealStorage().countOfMeals(mealPlanId, curDayIndex);
        int index = 0;
        for (int mealIndex = 0; mealIndex < mealCount; mealIndex++) {

            if(!getMealStorage().isMealIndexValid(mealPlanId, curDayIndex, mealIndex))
                continue;

            if (scrolledLayout.getChildCount() <= index)
                break;

            // is it okay to do getChildAt, what if a meal is added suddenly, indices might become misplaced.
            LinearLayout layout = (LinearLayout)scrolledLayout.getChildAt(index);
            saveMeal(layout, mealIndex);
            index++;
        }
    }
    // mealName and mealTime can be null if they should be taken from the existing headerLayout.
    void refreshMealHeader(LinearLayout headLayout, boolean editable, String mealName, String mealTime) {
        // NOTE(Emarioo): view_mealName may be EditText or TextView. We can use TextView since EditText
        //   from it inherits.
        if (mealTime == null){
            TextView view_mealTime = (TextView)headLayout.getChildAt(0);
            mealTime = view_mealTime.getText().toString();
        }
        if(mealName == null) {
            TextView view_mealName = (TextView) headLayout.getChildAt(1);
            mealName = view_mealName.getText().toString();

        }

        if(headLayout.getChildCount()>0)
            headLayout.removeViews(0,headLayout.getChildCount());

        TextView view_mealName = null;
        TextView view_mealTime = null;

        // NOTE(Emarioo): Disabling editing of meal when you click in on a patient.
        //  This is because you would modify the meal plan and thus changing the meals
        //  for other patients too. We could allow you to edit description if each
        //  patient has some kind of individual plan which wouldn't affect other patients.
        if(curPatientId!=0)
            editable = false;

        if (editable) {
            view_mealName = new EditText(this);
            view_mealTime = new EditText(this);
            ((EditText)view_mealTime).addTextChangedListener(new TimeFixer((EditText)view_mealTime));
        } else {
            view_mealName = new TextView(this);
            view_mealTime = new TextView(this);
        }
        view_mealTime.setText(mealTime);
        view_mealTime.setPadding(25, 8, 25, 8); // TODO(Emarioo): don't hardcode padding
        view_mealTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
        view_mealTime.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        headLayout.addView(view_mealTime);

        view_mealName.setText(mealName);
        view_mealName.setPadding(25, 8, 25, 8); // TODO(Emarioo): don't hardcode padding
        view_mealName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
        view_mealName.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        headLayout.addView(view_mealName);

    }

    void refreshMeals() {
        int mealPlanId = getMealPlanId();
        int mealCount = getMealStorage().countOfMeals(mealPlanId, curDayIndex);

        scrolledLayout.removeAllViews();

        int[] sortedMeals_index = new int[mealCount];
        int[] sortedMeals_time = new int[mealCount];
        int usedCount = 0;
        for(int mealIndex=0;mealIndex<mealCount;mealIndex++) {
            if(!getMealStorage().isMealIndexValid(mealPlanId, curDayIndex, mealIndex))
                continue;
            int hour = getMealStorage().hourOfMeal(mealPlanId, curDayIndex, mealIndex);
            int minute = getMealStorage().minuteOfMeal(mealPlanId, curDayIndex, mealIndex);
            sortedMeals_time[usedCount] = hour*100+minute;
            sortedMeals_index[usedCount] = mealIndex;
            usedCount++;
        }
        if(usedCount == 0){
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.str_no_meals));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            scrolledLayout.addView(textView);
        } else {
            // TODO(Emarioo): Don't use bubble sort, you are better than this
            for(int i=0;i<usedCount;i++) {
                boolean swapped = false;
                for(int j=0;j<usedCount - 1 - i;j++) {
                    if (sortedMeals_time[j+1] < sortedMeals_time[j]) {
                        int tmp = sortedMeals_time[j];
                        sortedMeals_time[j] = sortedMeals_time[j+1];
                        sortedMeals_time[j+1] = tmp;
                        tmp = sortedMeals_index[j];
                        sortedMeals_index[j] = sortedMeals_index[j+1];
                        sortedMeals_index[j+1] = tmp;
                        swapped = true;
                    }
                }
                if(!swapped)
                    break;
            }
            for(int i=0;i<usedCount;i++) {
                int mealIndex = sortedMeals_index[i];
                if(!getMealStorage().isMealIndexValid(mealPlanId,curDayIndex,mealIndex))
                    continue;

                String name = getMealStorage().nameOfMeal(mealPlanId, curDayIndex, mealIndex);
                int hour = getMealStorage().hourOfMeal(mealPlanId, curDayIndex, mealIndex);
                int minute = getMealStorage().minuteOfMeal(mealPlanId, curDayIndex, mealIndex);

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

                String timeStr = "";
                if(hour < 10) timeStr += "0";
                timeStr += hour + ":";
                if(minute < 10) timeStr += "0";
                timeStr += minute;
                refreshMealHeader(headLayout, false, name, timeStr);
            }
        }
    }
}