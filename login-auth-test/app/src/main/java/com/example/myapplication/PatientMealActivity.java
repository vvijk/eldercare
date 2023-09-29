package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage;

public class PatientMealActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout scrolledLayout=null;
    TextView text_name=null;
    Button btn_back=null;

    int curPatientId = 0;

    boolean DEBUG_COLORED_LAYOUT = false;
    PatientMealStorage getMealStorage() {
        return ((GlobalApp) getApplicationContext()).mealStorage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_meals);
        scrolledLayout = findViewById(R.id.week_scroll);
        text_name = findViewById(R.id.text_patient_name);
        btn_back = findViewById(R.id.manage_patient_back);
        // layout_description = findViewById(R.id.meal_patient_description);
        btn_back.setOnClickListener(this);

        Intent intent = getIntent();
        int patientId = intent.getIntExtra("patientId", 0);
        // int mealPlanId = intent.getIntExtra("mealPlanId", 0);
        if(patientId != 0) {
            curPatientId = patientId;
            String name = getMealStorage().nameOfPatient(patientId);
            if (name != null) {
                text_name.setText(name);
            } else {
                // error?
                text_name.setText("null");
            }
        } else {
            // error?
        }

        getMealStorage().refreshMealDays(getMealPlanId(), new Runnable() {
            @Override
            public void run() {
                refreshDays();
            }
        });
    }
    @Override
    protected void onDestroy() {
//        getMealStorage().popRefresher();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Integer clickedWeek = (Integer)view.getTag(R.id.clicked_week);

        if(view == btn_back) {
            Button button = (Button)view;
//            System.out.println("BACK");
            finish();
        } else if(clickedWeek!=null) {
            int mealPlanId = getMealPlanId();
            if(mealPlanId == 0)
                return;

            LinearLayout itemLayout = (LinearLayout) view;

            if(itemLayout.getChildCount() > 1) {
                itemLayout.removeViews(1, itemLayout.getChildCount() - 1);
                itemLayout.setBackgroundColor(getResources().getColor(R.color.purple_dark));
            }  else {
                itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));
                String[] weekDays = {
                        // NOTE: Calendar specifies sunday as the first day of the week
                        getResources().getString(R.string.str_sunday),
                        getResources().getString(R.string.str_monday),
                        getResources().getString(R.string.str_tuesday),
                        getResources().getString(R.string.str_wednesday),
                        getResources().getString(R.string.str_thursday),
                        getResources().getString(R.string.str_friday),
                        getResources().getString(R.string.str_saturday),
                };

                int year = 2023;
                Calendar cal = Calendar.getInstance(Locale.UK);  // Monday is the first day of the week for UK same as Sweden.
                cal.set(Calendar.YEAR,year); // TODO: Don't hardcode

                int dayIndex = clickedWeek;
                int dayCount = cal.getActualMaximum(Calendar.DAY_OF_YEAR);
                cal.set(Calendar.DAY_OF_YEAR,dayIndex+1);

                int lastWeek = cal.get(Calendar.WEEK_OF_YEAR);
                while(dayIndex < dayCount) {
                    cal.set(Calendar.DAY_OF_YEAR, dayIndex+1);
                    int weekNumber = cal.get(Calendar.WEEK_OF_YEAR);
                    if(weekNumber != lastWeek)
                        break;
                    int monthNumber = cal.get(Calendar.MONTH)+1;
                    int dayNumber = cal.get(Calendar.DAY_OF_MONTH);
                    int weekDayIndex = cal.get(Calendar.DAY_OF_WEEK)-1;

                    LinearLayout subLayout = new LinearLayout(itemLayout.getContext());
                    subLayout.setOrientation(LinearLayout.HORIZONTAL);
                    subLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
                    subLayout.setPadding(25,8,25,8); // TODO(Emarioo): don't hardcode padding
                    subLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    subLayout.setTag(R.id.clicked_weekday, dayIndex);
                    subLayout.setOnClickListener(this);
                    itemLayout.addView(subLayout);
                    {
                        TextView textview = new TextView(itemLayout.getContext());
                        textview.setText(weekDays[weekDayIndex]);
                        textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
                        textview.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        subLayout.addView(textview);
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
                        subLayout.addView(textview);
                    }
                    dayIndex++;
                }
            }
        }
    }

    // refresh the items (meals, days)
    void refreshDays(){
        // TODO(Emarioo): Optimize by reusing views instead of removing them?
        //   Another optimization would be to hide the list instead of removing and recreating them.
        scrolledLayout.removeAllViews();

        Calendar calendar = Calendar.getInstance(Locale.UK);
        int dayCount = 2;

        String[] weekDays = {
                // NOTE: Calendar specifies sunday as the first day of the week
                getResources().getString(R.string.str_sunday),
                getResources().getString(R.string.str_monday),
                getResources().getString(R.string.str_tuesday),
                getResources().getString(R.string.str_wednesday),
                getResources().getString(R.string.str_thursday),
                getResources().getString(R.string.str_friday),
                getResources().getString(R.string.str_saturday),
        };

        for(int dayIndex=0;dayIndex<dayCount;dayIndex++) {
            int weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK)-1;
            int monthNumber = calendar.get(Calendar.MONTH)+1;
            int dayNumber = calendar.get(Calendar.DAY_OF_MONTH);

            // item layout contains the name of the day (monday 3/5) and the meals that day (breakfast, lunch...).
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            itemLayout.setOnClickListener(this);
            scrolledLayout.addView(itemLayout);

            LinearLayout headLayout = new LinearLayout(itemLayout.getContext());
            headLayout.setOrientation(LinearLayout.HORIZONTAL);
            headLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
            headLayout.setPadding(25,8,25,8); // TODO(Emarioo): don't hardcode padding
            headLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            headLayout.setOnClickListener(this);
            itemLayout.addView(headLayout);
            {
                TextView textview = new TextView(itemLayout.getContext());
                textview.setText(weekDays[weekDayIndex]);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
                textview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                headLayout.addView(textview);
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
                headLayout.addView(textview);
            }

            LinearLayout mealLayout = new LinearLayout(itemLayout.getContext());
            mealLayout.setOrientation(LinearLayout.HORIZONTAL);
            mealLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
            mealLayout.setPadding(25,8,25,8); // TODO(Emarioo): don't hardcode padding
            mealLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mealLayout.setOnClickListener(this);
            itemLayout.addView(mealLayout);

            refreshMeals(mealLayout);

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }
    void refreshMeals(LinearLayout layout) {
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