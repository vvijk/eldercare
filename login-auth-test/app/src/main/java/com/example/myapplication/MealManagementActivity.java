package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.myapplication.util.FocusOnNewLine;
import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage.MealDay;
import com.example.myapplication.util.PatientMealStorage.Meal;
import com.example.myapplication.util.PatientMealStorage.Caregiver;
import com.example.myapplication.util.PatientMealStorage;
import com.example.myapplication.util.TimeFixer;

public class MealManagementActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout scrolledLayout=null;
    Button btn_mealPlan = null;
    Button btn_patients = null;
    Button btn_back = null;

    View.OnClickListener btn_listener = null;

    PatientMealStorage getMealStorage() {
        return ((GlobalApp) getApplicationContext()).mealStorage;
    }

    Runnable saveCallback = new Runnable() {
        @Override
        public void run() {
            saveAllMeals();
        }
    };

    boolean showingPatients = true;

    String caregiverUUID = "";
    int currentCaregiverId = 1; // TODO(Emarioo): Should come from somewhere else.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_manage);
        scrolledLayout = findViewById(R.id.meal_scroll);
        btn_mealPlan = findViewById(R.id.btn_meal_plan);
        btn_patients = findViewById(R.id.btn_patients);
        btn_back = findViewById(R.id.btn_back);

        btn_back.setOnClickListener(this);

        getMealStorage().initDBConnection();

        caregiverUUID = "Zn1pRMgS8qeVYXYwJgTHxl1VKAI3"; // TODO: Don't hardcode
        currentCaregiverId = getMealStorage().idFromCaregiverUUID(caregiverUUID);
        // if(showingPatients)
        //     refreshPatients();
        // else
        //     refreshMealPlan();

        btn_listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == btn_mealPlan) {
                    refreshMealPlan();
                } else if (view == btn_patients){
                    if(!showingPatients)
                        saveAllMeals();
                    refreshPatients();
                }
            }
        };
        btn_mealPlan.setOnClickListener(btn_listener);
        btn_patients.setOnClickListener(btn_listener);
        getMealStorage().pushRefresher_caregiver(currentCaregiverId, new Runnable() {
            @Override
            public void run() {
                if(showingPatients)
                    refreshPatients();
                else
                    refreshMealPlan(); // TODO: All text view and layouts are deleted and added each time childEventListener runs. It would be better to wait with refreshing until childEventListener is done.
            }
        });
    }
    @Override
    protected void onDestroy() {
        getMealStorage().popRefresher();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Integer deleteMealIndex = (Integer)view.getTag(R.id.clicked_deleteMeal);
        Integer patientId = (Integer)view.getTag(R.id.tag_patientId);
        Boolean addMeal = (Boolean)view.getTag(R.id.tag_template_add_meal);

        if(deleteMealIndex != null) {
            saveAllMeals();
            getMealStorage().caregiver_template_deleteMeal(currentCaregiverId,deleteMealIndex);
    //            refreshMeals();
        } else if(patientId != null) {
            // Button button = (Button) view;
//            System.out.println("Press " + getMealStorage().nameOfPatient(patientId));

            Intent intent = new Intent(getApplicationContext(), PatientMealActivity.class);
            intent.putExtra("caretakerId", patientId);
            intent.putExtra("caregiverId", currentCaregiverId);
            startActivity(intent);
            refreshPatients();
        } else if(addMeal != null) {
            saveAllMeals();
            getMealStorage().caregiver_template_addMeal(currentCaregiverId, getResources().getString(R.string.default_meal_name));
        } else if(btn_back != null) {
            saveAllMeals();
            finish();
        }
    }
    void saveAllMeals() {
        if(showingPatients)
            return;
        for(int i=0;i<scrolledLayout.getChildCount();i++) {
            if(!(scrolledLayout.getChildAt(i) instanceof LinearLayout))
                continue;
            LinearLayout itemLayout = (LinearLayout)scrolledLayout.getChildAt(i);
            Integer mealIndex = (Integer)itemLayout.getTag(R.id.template_mealIndex);
            if(!getMealStorage().caregiver_template_isMealIndexValid(currentCaregiverId, mealIndex))
                continue;

            if(itemLayout.getChildCount()>1) {
                // only save if itemLayout is being edited. if it's not being edited then the meal plan name should be up to date.
                LinearLayout headLayout = (LinearLayout) itemLayout.getChildAt(0);

                TextView view_time = (TextView)headLayout.getChildAt(0);
                TextView view_name = (TextView)headLayout.getChildAt(1);

                String[] split = view_time.getText().toString().split(":");
                if(split.length>1) {
                    try {
                        int hour = Integer.parseInt(split[0]);
                        int minute = Integer.parseInt(split[1]);
                        getMealStorage().caregiver_template_setHourOfMeal(currentCaregiverId, mealIndex, hour);
                        getMealStorage().caregiver_template_setMinuteOfMeal(currentCaregiverId, mealIndex, minute);
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
                getMealStorage().caregiver_template_setNameOfMeal(currentCaregiverId, mealIndex, view_name.getText().toString());

                if(itemLayout.getChildCount()>1) {
                    LinearLayout subLayout = (LinearLayout) itemLayout.getChildAt(1);
                    TextView view_desc = (TextView) subLayout.getChildAt(0);
                    getMealStorage().caregiver_template_setDescriptionOfMeal(currentCaregiverId,mealIndex, view_desc.getText().toString());
                    // Toast.makeText(this,"saved "+view_desc.getText(),Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    void refreshPatients() {
        showingPatients = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn_mealPlan.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.almost_black)));
            btn_patients.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_dark)));
        } else {
            // Colors won't work
        }
        // btn_add.setText(R.string.str_add_patient);
        // TODO(Emarioo): Optimize by reusing view instead of removing them?
        //   Another optimization would be to hide the list instead of removing and recreating them.
        scrolledLayout.removeAllViews();
        int patientCount = getMealStorage().caretakerCountOfCaregiver(currentCaregiverId);
        for(int i=0;i<patientCount;i++) {
            int caretakerId = getMealStorage().caretakerIdFromIndex(currentCaregiverId,i);
            String name = getMealStorage().nameOfCaretaker(caretakerId);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrolledLayout.addView(itemLayout);

            LinearLayout headLayout = new LinearLayout(this);
            headLayout.setOrientation(LinearLayout.HORIZONTAL);
            headLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            itemLayout.addView(headLayout);

            TextView textview = new TextView(this);
            textview.setText(name);
            textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
            textview.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            headLayout.addView(textview);

            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            buttonLayout.setGravity(Gravity.RIGHT);

            headLayout.addView(buttonLayout);

            Button button = new Button(this);
            button.setText(">");
            // button.setText(R.string.patient_meals_edit);
            button.setAllCaps(false);
            button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20); // TODO(Emarioo): Don't hardcode text size
            button.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            button.setTag(R.id.tag_patientId, caretakerId);
            button.setOnClickListener(this);

            buttonLayout.addView(button);
        }
    }
    void refreshMealPlan(){
        // btn_add.setText(R.string.str_add_meal_plan);
        showingPatients = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn_mealPlan.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_dark)));
            btn_patients.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.almost_black)));
        } else {
            // Colors won't work
        }

        scrolledLayout.removeAllViews();

        TextView textview = new TextView(scrolledLayout.getContext());
        textview.setText(getResources().getString(R.string.str_template_meals));
        textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
        textview.setGravity(Gravity.CENTER);
        textview.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        scrolledLayout.addView(textview);

        int mealCount = getMealStorage().caregiver_template_countOfMeals(currentCaregiverId);

        int[] sortedMeals_index = new int[mealCount];
        int[] sortedMeals_time = new int[mealCount];
        int usedCount = 0;
        for(int mealIndex=0;mealIndex<mealCount;mealIndex++) {
            if(!getMealStorage().caregiver_template_isMealIndexValid(currentCaregiverId, mealIndex))
                continue;
            int hour = getMealStorage().caregiver_template_hourOfMeal(currentCaregiverId, mealIndex);
            int minute = getMealStorage().caregiver_template_minuteOfMeal(currentCaregiverId, mealIndex);
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
            textView.setGravity(Gravity.CENTER);
            scrolledLayout.addView(textView);
        } else {
            // TODO(Emarioo): Don't use bubble sort, you are better than this
            for (int i = 0; i < usedCount; i++) {
                boolean swapped = false;
                for (int j = 0; j < usedCount - 1 - i; j++) {
                    if (sortedMeals_time[j + 1] < sortedMeals_time[j]) {
                        int tmp = sortedMeals_time[j];
                        sortedMeals_time[j] = sortedMeals_time[j + 1];
                        sortedMeals_time[j + 1] = tmp;
                        tmp = sortedMeals_index[j];
                        sortedMeals_index[j] = sortedMeals_index[j + 1];
                        sortedMeals_index[j + 1] = tmp;
                        swapped = true;
                    }
                }
                if (!swapped)
                    break;
            }
            for (int i = 0; i < usedCount; i++) {
                int mealIndex = sortedMeals_index[i];
                if (!getMealStorage().caregiver_template_isMealIndexValid(currentCaregiverId, mealIndex))
                    continue;
                // for(int mealIndex=0;mealIndex<mealCount;mealIndex++) {
                //     if(!getMealStorage().caregiver_template_isMealIndexValid(currentCaregiverId, mealIndex))
                //         continue;

                String name = getMealStorage().caregiver_template_nameOfMeal(currentCaregiverId, mealIndex);
                int hour = getMealStorage().caregiver_template_hourOfMeal(currentCaregiverId, mealIndex);
                int minute = getMealStorage().caregiver_template_minuteOfMeal(currentCaregiverId, mealIndex);
                String description = getMealStorage().caregiver_template_descriptionOfMeal(currentCaregiverId, mealIndex);

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.LEFT);
                itemLayout.setTag(R.id.template_mealIndex, mealIndex);
                scrolledLayout.addView(itemLayout);

                // layout for the meal's name and time
                LinearLayout headLayout = new LinearLayout(this);
                headLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                headLayout.setGravity(Gravity.LEFT);
                itemLayout.addView(headLayout);

                String timeStr = "";
                if (hour < 10) timeStr += "0";
                timeStr += hour + ":";
                if (minute < 10) timeStr += "0";
                timeStr += minute;
                refreshMealHeader(headLayout, true, name, timeStr);

                itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));

                LinearLayout subLayout = new LinearLayout(itemLayout.getContext());
                subLayout.setOrientation(LinearLayout.VERTICAL);
                subLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
                subLayout.setPadding(25, 10, 25, 12); // TODO(Emarioo): don't hardcode padding
                subLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(subLayout);

                // NOTE(Emarioo): Disabling editing of description when you click in on a patient.
                //  This is because you would modify the meal plan and thus changing the meals
                //  for other patients too. We could allow you to edit description if each
                //  patient has some kind of individual plan which wouldn't affect other patients.
                TextView editText = null;
                // if(curPatientId!=0){
                //     editText = new TextView(itemLayout.getContext());
                // } else {
                editText = new EditText(itemLayout.getContext());
                ((EditText) editText).addTextChangedListener(new FocusOnNewLine((EditText) editText, saveCallback));
                // }

                editText.setText(description);
                editText.setHint(R.string.str_no_description);
                editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24); // TODO(Emarioo): Don't hardcode text size
                editText.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                subLayout.addView(editText);

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
            {
                Button addButton = new Button(scrolledLayout.getContext());
                addButton.setAllCaps(false);
                addButton.setText(getResources().getString(R.string.str_add_meal));
                addButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // TODO(Emarioo): Don't hardcode text size
                addButton.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                GradientDrawable shape = new GradientDrawable();
                shape.setCornerRadius(16);
                shape.setColor(getResources().getColor(R.color.purple));
                addButton.setBackground(shape);
                addButton.setTag(R.id.tag_template_add_meal, true);
                addButton.setOnClickListener(this);
                scrolledLayout.addView(addButton);
            }
        }
    }
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

        if (editable) {
            view_mealName = new EditText(this);
            ((EditText)view_mealName).addTextChangedListener(new FocusOnNewLine((EditText)view_mealName,saveCallback));
            view_mealTime = new EditText(this);
            ((EditText)view_mealTime).addTextChangedListener(new TimeFixer((EditText)view_mealTime));
            ((EditText)view_mealTime).addTextChangedListener(new FocusOnNewLine((EditText)view_mealTime,saveCallback));
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
}