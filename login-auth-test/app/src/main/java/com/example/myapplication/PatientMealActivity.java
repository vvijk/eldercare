package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

import com.example.myapplication.util.FocusOnNewLine;
import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage;
import com.example.myapplication.util.TimeFixer;

public class PatientMealActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout scrolledLayout=null;
    TextView text_name=null;
    Button btn_back=null;

    int curCaretakerId = 0;
    int curCaregiverId = 0;

    Runnable saveCallback = new Runnable() {
        @Override
        public void run() {
            saveAllMeals();
        }
    };

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
        curCaregiverId = intent.getIntExtra("caregiverId", -1);
        if(curCaregiverId == -1) {
            // bad, throw error?
        }
        curCaretakerId = intent.getIntExtra("caretakerId", -1);
        // int mealPlanId = intent.getIntExtra("mealPlanId", 0);
        if(curCaretakerId != -1) {
            String name = getMealStorage().nameOfCaretaker(curCaretakerId);
            if (name != null) {
                text_name.setText(name);
            } else {
                // error?
                text_name.setText("null");
            }
        } else {
            // error?
        }
        getMealStorage().pushRefresher_caretaker(curCaretakerId, new Runnable() {
            @Override
            public void run() {
                refreshDays();
            }
        });
    }
    @Override
    protected void onDestroy() {
        saveAllMeals();
        getMealStorage().popRefresher();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        Integer add_meal_at_dayIndex = (Integer)view.getTag(R.id.tag_add_meal_at_day);
        Integer deleteMeal = (Integer)view.getTag(R.id.tag_deleteMeal);
        Integer dayIndex = (Integer)view.getTag(R.id.tag_dayIndex);
        Integer replaceMeal_dayIndex = (Integer)view.getTag(R.id.tag_replace_template_meal);

        if(view == btn_back) {
            Button button = (Button)view;
//            System.out.println("BACK");
            finish();
        } else if(add_meal_at_dayIndex != null) {
            saveAllMeals();
            getMealStorage().caretaker_addMeal(curCaretakerId, add_meal_at_dayIndex, getResources().getString(R.string.default_meal_name));
        } else if(deleteMeal != null) {
            saveAllMeals();
            getMealStorage().caretaker_deleteMeal(curCaretakerId, dayIndex, deleteMeal);
        } else if(replaceMeal_dayIndex != null) {
            saveAllMeals();
            getMealStorage().caretaker_replaceMealsWithTemplate(curCaretakerId, replaceMeal_dayIndex, curCaregiverId);
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
            int sunday_first_weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK)-1;
            int monthNumber = calendar.get(Calendar.MONTH)+1;
            int dayNumber = calendar.get(Calendar.DAY_OF_MONTH);

            int weekDayIndex =  (7 + sunday_first_weekDayIndex - 1) % 7;

            // item layout contains the name of the day (monday 3/5) and the meals that day (breakfast, lunch...).
            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dayLayout.setOnClickListener(this);
            dayLayout.setTag(R.id.tag_dayIndex, weekDayIndex);
            dayLayout.setPadding(0,0,0,10);  // TODO(Emarioo): don't hardcode padding?
            scrolledLayout.addView(dayLayout);

            LinearLayout headLayout = new LinearLayout(dayLayout.getContext());
            headLayout.setOrientation(LinearLayout.HORIZONTAL);
            headLayout.setBackgroundColor(getResources().getColor(R.color.purple_normal));
            headLayout.setPadding(25,8,25,8); // TODO(Emarioo): don't hardcode padding
            headLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            headLayout.setOnClickListener(this);
            dayLayout.addView(headLayout);
            {
                TextView textview = new TextView(dayLayout.getContext());
                textview.setText(weekDays[sunday_first_weekDayIndex]);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
                textview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                headLayout.addView(textview);
            }
            {
                TextView textview = new TextView(dayLayout.getContext());
                textview.setText(monthNumber + "/" + dayNumber);
                textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25); // TODO(Emarioo): Don't hardcode text size
                textview.setGravity(Gravity.RIGHT);
                textview.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
                textview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                headLayout.addView(textview);
            }

            LinearLayout mealLayout = new LinearLayout(dayLayout.getContext());
            mealLayout.setOrientation(LinearLayout.VERTICAL);
            // mealLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
            mealLayout.setBackgroundColor(getResources().getColor(R.color.purple_normal));
            mealLayout.setPadding(25,8,25,8); // TODO(Emarioo): don't hardcode padding
            mealLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mealLayout.setOnClickListener(this);
            mealLayout.setGravity(Gravity.CENTER);
            dayLayout.addView(mealLayout);

            refreshMeals(mealLayout, weekDayIndex);

            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
    }
    void refreshMeals(LinearLayout mealLayout, int weekDayIndex) {
        int mealCount = getMealStorage().caretaker_countOfMeals(curCaretakerId, weekDayIndex);

        int[] sortedMeals_index = new int[mealCount];
        int[] sortedMeals_time = new int[mealCount];
        int usedCount = 0;
        for(int mealIndex=0;mealIndex<mealCount;mealIndex++) {
            if(!getMealStorage().caretaker_isMealIndexValid(curCaretakerId, weekDayIndex, mealIndex))
                continue;
            int hour = getMealStorage().caretaker_hourOfMeal(curCaretakerId, weekDayIndex, mealIndex);
            int minute = getMealStorage().caretaker_minuteOfMeal(curCaretakerId, weekDayIndex, mealIndex);
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
            mealLayout.addView(textView);
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
                if(!getMealStorage().caretaker_isMealIndexValid(curCaretakerId, weekDayIndex, mealIndex))
                    continue;

                String name = getMealStorage().caretaker_nameOfMeal(curCaretakerId, weekDayIndex, mealIndex);
                int hour = getMealStorage().caretaker_hourOfMeal(curCaretakerId, weekDayIndex, mealIndex);
                int minute = getMealStorage().caretaker_minuteOfMeal(curCaretakerId, weekDayIndex, mealIndex);
                String description = getMealStorage().caretaker_descriptionOfMeal(curCaretakerId, weekDayIndex, mealIndex);

                LinearLayout itemLayout = new LinearLayout(this);
                itemLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.setOrientation(LinearLayout.VERTICAL);
                itemLayout.setGravity(Gravity.LEFT);
                itemLayout.setTag(R.id.tag_mealIndex, mealIndex);
                mealLayout.addView(itemLayout);

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
                refreshMealHeader(headLayout, true, name, timeStr);

                itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));

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
                // if(curPatientId!=0){
                //     editText = new TextView(itemLayout.getContext());
                // } else {
                editText = new EditText(itemLayout.getContext());
                editText.addTextChangedListener(new FocusOnNewLine((EditText)editText, saveCallback));
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
                delButton.setTag(R.id.tag_deleteMeal, mealIndex);
                delButton.setTag(R.id.tag_dayIndex, weekDayIndex);
                delButton.setOnClickListener(this);
                buttonLayout.addView(delButton);
            }
        }

        LinearLayout footLayout = new LinearLayout(mealLayout.getContext());
        footLayout.setOrientation(LinearLayout.HORIZONTAL);
        footLayout.setGravity(Gravity.CENTER);
        footLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mealLayout.addView(footLayout);
        {
            Button addButton = new Button(footLayout.getContext());
            addButton.setAllCaps(false);
            addButton.setText(getResources().getString(R.string.str_add_meal));
            addButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // TODO(Emarioo): Don't hardcode text size
            addButton.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(16);
            shape.setColor(getResources().getColor(R.color.purple));
            addButton.setBackground(shape);
            addButton.setTag(R.id.tag_add_meal_at_day, weekDayIndex);
            addButton.setOnClickListener(this);
            addButton.setTextColor(getResources().getColor(R.color.white));
            footLayout.addView(addButton);
        }
        {
            Button replaceButton = new Button(footLayout.getContext());
            replaceButton.setAllCaps(false);
            replaceButton.setText(getResources().getString(R.string.btn_replace_template_meal));
            replaceButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18); // TODO(Emarioo): Don't hardcode text size
            replaceButton.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(16);
            shape.setColor(getResources().getColor(R.color.purple));
            replaceButton.setBackground(shape);
            replaceButton.setTag(R.id.tag_replace_template_meal, weekDayIndex);
            replaceButton.setOnClickListener(this);
            replaceButton.setTextColor(getResources().getColor(R.color.white));
            footLayout.addView(replaceButton);
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
            ((EditText)view_mealName).addTextChangedListener(new FocusOnNewLine((EditText)view_mealName, saveCallback));
            view_mealTime = new EditText(this);
            ((EditText)view_mealTime).addTextChangedListener(new TimeFixer((EditText)view_mealTime));
            ((EditText)view_mealTime).addTextChangedListener(new FocusOnNewLine((EditText)view_mealTime, saveCallback));
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
    void saveAllMeals() {
        for(int i=0;i<scrolledLayout.getChildCount();i++) {
            if(!(scrolledLayout.getChildAt(i) instanceof LinearLayout))
                continue;
            LinearLayout dayLayout = (LinearLayout)scrolledLayout.getChildAt(i);
            Integer weekDayIndex = (Integer)dayLayout.getTag(R.id.tag_dayIndex);

            if(dayLayout.getChildCount() != 2)
                continue;
            if(!(dayLayout.getChildAt(1) instanceof LinearLayout)) {
                continue;
            }
            LinearLayout mealLayout = (LinearLayout) dayLayout.getChildAt(1);

            for(int j=0;j<mealLayout.getChildCount();j++) {
                if(!(mealLayout.getChildAt(j) instanceof LinearLayout))
                    continue;
                LinearLayout itemLayout = (LinearLayout) mealLayout.getChildAt(j);
                Integer mealIndex = (Integer)itemLayout.getTag(R.id.tag_mealIndex);
                if(mealIndex==null)
                    continue; // there are no meals

                LinearLayout headLayout = (LinearLayout) itemLayout.getChildAt(0);
                TextView view_time = (TextView)headLayout.getChildAt(0);
                TextView view_name = (TextView)headLayout.getChildAt(1);
                getMealStorage().caretaker_setNameOfMeal(curCaretakerId, weekDayIndex, mealIndex, view_name.getText().toString());

                String[] split = view_time.getText().toString().split(":");
                if(split.length>1) {
                    try {
                        int hour = Integer.parseInt(split[0]);
                        int minute = Integer.parseInt(split[1]);
                        getMealStorage().caretaker_setHourOfMeal(curCaretakerId, weekDayIndex, mealIndex, hour);
                        getMealStorage().caretaker_setMinuteOfMeal(curCaretakerId, weekDayIndex, mealIndex, minute);
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

                LinearLayout subLayout = (LinearLayout) itemLayout.getChildAt(1);
                TextView view_desc = (TextView)subLayout.getChildAt(0);

                getMealStorage().caretaker_setDescriptionOfMeal(curCaretakerId, weekDayIndex, mealIndex, view_desc.getText().toString());
            }
        }
    }
}