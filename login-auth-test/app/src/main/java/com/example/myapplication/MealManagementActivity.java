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

import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage;

public class MealManagementActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout scrolledLayout=null;
    Button btn_mealPlans = null;
    Button btn_patients = null;
    Button btn_add = null;

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
        btn_add = findViewById(R.id.btn_add_meal_plan);

        getMealStorage().initDBConnection();

        getMealStorage().pushRefresher(new Runnable() {
            @Override
            public void run() {
                if(showingPatients)
                    refreshPatients();
                else
                    refreshMealPlans(); // TODO: All text view and layouts are deleted and added each time childEventListener runs. It would be better to wait with refreshing until childEventListener is done.
            }
        });

        btn_listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view == btn_mealPlans) {
                    refreshMealPlans();
                } else if (view == btn_patients){
                    if(!showingPatients)
                        saveAllMeals();
                    refreshPatients();

                    btn_add = findViewById(R.id.btn_add_meal_plan);
                } else if(view == btn_add){
                    if(showingPatients) {
                        Toast.makeText(view.getContext(), "can't add patients, not implemented", Toast.LENGTH_LONG).show();
                    } else {
                        saveAllMeals();
                        getMealStorage().addMealPlan(getResources().getString(R.string.default_meal_plan_name));
                        refreshMealPlans();
                    }
                }
            }
        };
        btn_mealPlans.setOnClickListener(btn_listener);
        btn_patients.setOnClickListener(btn_listener);
        btn_add.setOnClickListener(btn_listener);
    }
    @Override
    protected void onDestroy() {
        getMealStorage().popRefresher();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Integer clickedMealPlan = (Integer)view.getTag(R.id.clicked_meal_plan);
        Integer clickedDelete = (Integer)view.getTag(R.id.clicked_delete_meal_plan);

        if(clickedMealPlan != null) {
            LinearLayout itemLayout = (LinearLayout) view;

            if(itemLayout.getChildCount()>1) {
                LinearLayout headLayout = (LinearLayout) itemLayout.getChildAt(0);

                getMealStorage().setNameOfMealPlan(clickedMealPlan, ((TextView)headLayout.getChildAt(0)).getText().toString());

                itemLayout.removeViews(1,itemLayout.getChildCount()-1);
                itemLayout.setBackgroundColor(getResources().getColor(R.color.purple_dark));

                refreshMealHeader(headLayout, false, null);
            } else {
                itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));

                LinearLayout headLayout = (LinearLayout) itemLayout.getChildAt(0);

                refreshMealHeader(headLayout, true, null);
                LinearLayout subLayout = new LinearLayout(itemLayout.getContext());
                subLayout.setOrientation(LinearLayout.VERTICAL);
                subLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
                subLayout.setPadding(25,10,25,12); // TODO(Emarioo): don't hardcode padding
                subLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(subLayout);

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
                delButton.setTag(R.id.clicked_delete_meal_plan, clickedMealPlan);
                delButton.setOnClickListener(this);
                buttonLayout.addView(delButton);
            }
        } else if(clickedDelete != null) {
            saveAllMeals();
//            Toast.makeText(view.getContext(), "meal plan removal not implemented",Toast.LENGTH_LONG).show();
            getMealStorage().deleteMealPlan(clickedDelete);
        } else if(showingPatients) {
            Button button = (Button)view;
            int patientId = (Integer) button.getTag();
//            System.out.println("Press " + getMealStorage().nameOfPatient(patientId));

            Intent intent = new Intent(getApplicationContext(), PatientMealActivity.class);
            intent.putExtra("patientId", patientId);
            startActivity(intent);
            refreshPatients();
        } else {
            saveAllMeals();
            Button button = (Button)view;
            int mealPlanId = (Integer) button.getTag();
//            System.out.println("Press " + getMealStorage().nameOfMealPlan(mealPlanId));
            Intent intent = new Intent(getApplicationContext(), PatientMealActivity.class);
            intent.putExtra("mealPlanId", mealPlanId);
            startActivity(intent);
            refreshMealPlans();
        }
    }
    void saveAllMeals() {
        for(int i=0;i<scrolledLayout.getChildCount();i++) {
            LinearLayout itemLayout = (LinearLayout)scrolledLayout.getChildAt(i);
            Integer clickedMealPlan = (Integer)itemLayout.getTag(R.id.clicked_meal_plan);

            if(itemLayout.getChildCount()>1) {
                // only save if itemLayout is being edited. if it's not being edited then the meal plan name should be up to date.
                LinearLayout headLayout = (LinearLayout) itemLayout.getChildAt(0);

                getMealStorage().setNameOfMealPlan(clickedMealPlan, ((TextView) headLayout.getChildAt(0)).getText().toString());
            }
        }
    }
    // mealName and mealTime can be null if they should be taken from the existing headerLayout.
    void refreshMealHeader(LinearLayout headLayout, boolean editable, String mealPlanName) {
        // NOTE(Emarioo): view_mealName may be EditText or TextView. We can use TextView since EditText
        //   from it inherits.
        if(mealPlanName == null) {
            TextView view_mealName = (TextView) headLayout.getChildAt(0);
            mealPlanName = view_mealName.getText().toString();
        }

        if(headLayout.getChildCount()>0)
            headLayout.removeViews(0, 1); // don't delete edit button

        TextView view_mealName = null;

        // NOTE(Emarioo): You cannot edit patients here. Probably, we shall see how things end up.
        if(showingPatients)
            editable = false;

        if (editable) {
            view_mealName = new EditText(this);
        } else {
            view_mealName = new TextView(this);
        }
//        view_mealTime.setText(mealTime);
//        view_mealTime.setPadding(25, 8, 25, 8); // TODO(Emarioo): don't hardcode padding
//        view_mealTime.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
//        view_mealTime.setLayoutParams(new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
//        headLayout.addView(view_mealTime);

//        TextView textview = new TextView(this);
//        textview.setText(name);
//        textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
//        textview.setLayoutParams(new ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
//        if(DEBUG_COLORED_LAYOUT)
//            textview.setBackgroundColor(Color.GREEN);

        view_mealName.setText(mealPlanName);
        view_mealName.setPadding(0,0,0,0); // TODO(Emarioo): don't hardcode padding?
        view_mealName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
        view_mealName.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        headLayout.addView(view_mealName, 0);
    }
    void refreshPatients() {
        showingPatients = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            btn_mealPlans.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.almost_black)));
            btn_patients.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_dark)));
        } else {
            // Colors won't work
        }
        btn_add.setText(R.string.str_add_patient);
        // TODO(Emarioo): Optimize by reusing view instead of removing them?
        //   Another optimization would be to hide the list instead of removing and recreating them.
        scrolledLayout.removeAllViews();
        int patientCount = getMealStorage().patientCountOfCaregiver(currentCaregiverId);
        for(int i=0;i<patientCount;i++) {
            int patientId = getMealStorage().patientIdFromIndex(currentCaregiverId,i);
            String name = getMealStorage().nameOfPatient(patientId);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrolledLayout.addView(itemLayout);

            // TODO: Click listener?

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
            if(DEBUG_COLORED_LAYOUT)
                textview.setBackgroundColor(Color.GREEN);

            headLayout.addView(textview);

            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            buttonLayout.setGravity(Gravity.RIGHT);
            if(DEBUG_COLORED_LAYOUT)
                buttonLayout.setBackgroundColor(Color.CYAN);

            headLayout.addView(buttonLayout);

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
        btn_add.setText(R.string.str_add_meal_plan);
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
//            System.out.println(">Bro id "+mealPlanId +", index "+i);
            if(mealPlanId == 0)
                continue;
            String name = getMealStorage().nameOfMealPlan(mealPlanId);

//            System.out.println("Bro id "+mealPlanId +", index "+i);

            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            scrolledLayout.addView(itemLayout);

            itemLayout.setTag(R.id.clicked_meal_plan, mealPlanId);
            itemLayout.setOnClickListener(this);

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
            if(DEBUG_COLORED_LAYOUT)
                textview.setBackgroundColor(Color.GREEN);

            headLayout.addView(textview);

            LinearLayout buttonLayout = new LinearLayout(this);
            buttonLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            buttonLayout.setGravity(Gravity.RIGHT);
            if(DEBUG_COLORED_LAYOUT)
                buttonLayout.setBackgroundColor(Color.CYAN);

            headLayout.addView(buttonLayout);

            Button button = new Button(this);
            button.setText(R.string.patient_meals_edit);
            button.setAllCaps(false);
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