package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.example.myapplication.util.MealStorage;
import com.example.myapplication.util.TimeFixer;

public class MealActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout scrolledLayout=null;
    TextView text_name=null;
    Button btn_back=null;
    Button btn_info=null;

    Button btn_recipient=null; // temporary

    String curRecipientUID = null;
    String curCaregiverUID = null;
    int curRecipientId = 0;
    int curCaregiverId = 0;

    Runnable saveCallback = new Runnable() {
        @Override
        public void run() {
            saveAllMeals();
        }
    };

    MealStorage getMealStorage() {
        return ((MealApp) getApplicationContext()).mealStorage;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipient_meals);
        scrolledLayout = findViewById(R.id.week_scroll);
        text_name = findViewById(R.id.text_recipient_name);
        btn_back = findViewById(R.id.manage_recipient_back);
        btn_info = findViewById(R.id.btn_recipient_info);
        btn_back.setOnClickListener(this);
        btn_info.setOnClickListener(this);

        btn_recipient = findViewById(R.id.btn_home_recipient); // temporary
        btn_recipient.setOnClickListener(this);// temporary

        Intent intent = getIntent();

        curCaregiverUID = intent.getStringExtra("caregiverUID");
        if(curCaregiverUID != null) {
            curCaregiverId = getMealStorage().idFromCaregiverUID(curCaregiverUID);
        } else {
            // bad, throw error?
        }
        curRecipientUID = intent.getStringExtra("recipientUID");
        if(curRecipientUID != null) {
            curRecipientId = getMealStorage().idFromCaretakerUID(curRecipientUID);
            String name = getMealStorage().nameOfCaretaker(curRecipientId);
            if (name != null) {
                text_name.setText(name);
            } else {
                // TODO(Emarioo): This may happen if the name of caretaker isn't cached.
                //  MealStorage will not fetch caretaker's name. If the name was fetched by
                //  MealManagementActivity before you got to this activity things will be fine.
                //  If not, you end up here.
                // error?
                text_name.setText("Missing name");
            }
        } else {
            // error?
        }
        getMealStorage().pushRefresher_caretaker(curRecipientId, new Runnable() {
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

        // temporary
        if(view == btn_recipient) {
            saveAllMeals();
            Intent intent = new Intent(getApplicationContext(), RecipientHome.class);
            intent.putExtra("recipientUID", curRecipientUID);
            intent.putExtra("caregiverUID", curCaregiverUID);
            startActivity(intent);
        }

        if(view == btn_back) {
            Button button = (Button)view;
//            System.out.println("BACK");
            finish();
        } else if(add_meal_at_dayIndex != null) {
            saveAllMeals();
            getMealStorage().caretaker_addMeal(curRecipientId, add_meal_at_dayIndex, getResources().getString(R.string.default_meal_name));
        } else if(deleteMeal != null) {
            saveAllMeals();
            getMealStorage().caretaker_deleteMeal(curRecipientId, dayIndex, deleteMeal);
        } else if(replaceMeal_dayIndex != null) {
            saveAllMeals();
            getMealStorage().caretaker_replaceMealsWithTemplate(curRecipientId, replaceMeal_dayIndex, curCaregiverId);
        } else if(view == btn_info) {
            saveAllMeals();
            Intent intent = new Intent(getApplicationContext(), PatientProfile.class);
            intent.putExtra("recipientUID", curRecipientUID);
            intent.putExtra("caregiverUID", curCaregiverUID);
            startActivity(intent);
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

            int weekDayIndex = (7 + sunday_first_weekDayIndex - 1) % 7;

            // item layout contains the name of the day (monday 3/5) and the meals that day (breakfast, lunch...).
            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);
            dayLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            dayLayout.setOnClickListener(this);
            dayLayout.setTag(R.id.tag_dayIndex, weekDayIndex);
            dayLayout.setPadding(0,0,0,20);  // TODO(Emarioo): don't hardcode padding?
            scrolledLayout.addView(dayLayout);

            LinearLayout headLayout = new LinearLayout(dayLayout.getContext());
            headLayout.setOrientation(LinearLayout.HORIZONTAL);
            headLayout.setBackgroundColor(getResources().getColor(R.color.purple_dark));
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
            mealLayout.setBackgroundColor(getResources().getColor(R.color.purple_dark));
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
        // int mealCount = getMealStorage().caretaker_countOfMeals(curRecipientId, weekDayIndex);

        int[] sortedMeals_index = getMealStorage().caretaker_sortedMealIndices(curRecipientId, weekDayIndex);
        if(sortedMeals_index.length == 0){
            TextView textView = new TextView(this);
            textView.setText(getResources().getString(R.string.str_no_meals));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
            textView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            textView.setGravity(Gravity.CENTER);
            mealLayout.addView(textView);
        } else {
            for(int i=0;i<sortedMeals_index.length;i++) {
                int mealIndex = sortedMeals_index[i];
                if(!getMealStorage().caretaker_isMealIndexValid(curRecipientId, weekDayIndex, mealIndex))
                    continue;

                String name = getMealStorage().caretaker_nameOfMeal(curRecipientId, weekDayIndex, mealIndex);
                int hour = getMealStorage().caretaker_hourOfMeal(curRecipientId, weekDayIndex, mealIndex);
                int minute = getMealStorage().caretaker_minuteOfMeal(curRecipientId, weekDayIndex, mealIndex);
                String description = getMealStorage().caretaker_descriptionOfMeal(curRecipientId, weekDayIndex, mealIndex);

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

                refreshMealHeader(headLayout, true, name, Helpers.FormatTime(hour, minute));

                itemLayout.setBackgroundColor(getResources().getColor(R.color.dry_green_brigher));

                LinearLayout subLayout = new LinearLayout(itemLayout.getContext());
                subLayout.setOrientation(LinearLayout.VERTICAL);
                subLayout.setBackgroundColor(getResources().getColor(R.color.dry_green));
                subLayout.setPadding(25,10,25,12); // TODO(Emarioo): don't hardcode padding
                subLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                itemLayout.addView(subLayout);

                // NOTE(Emarioo): Disabling editing of description when you click in on a recipient.
                //  This is because you would modify the meal plan and thus changing the meals
                //  for other recipients too. We could allow you to edit description if each
                //  recipient has some kind of individual plan which wouldn't affect other recipients.
                TextView editText = null;
                // if(currecipientId!=0){
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
            addButton.setTextColor(getResources().getColor(R.color.black));
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
            replaceButton.setTextColor(getResources().getColor(R.color.black));
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
                getMealStorage().caretaker_setNameOfMeal(curRecipientId, weekDayIndex, mealIndex, view_name.getText().toString());

                String[] split = view_time.getText().toString().split(":");
                if(split.length>1) {
                    try {
                        int hour = Integer.parseInt(split[0]);
                        int minute = Integer.parseInt(split[1]);
                        getMealStorage().caretaker_setHourOfMeal(curRecipientId, weekDayIndex, mealIndex, hour);
                        getMealStorage().caretaker_setMinuteOfMeal(curRecipientId, weekDayIndex, mealIndex, minute);
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

                getMealStorage().caretaker_setDescriptionOfMeal(curRecipientId, weekDayIndex, mealIndex, view_desc.getText().toString());
            }
        }
    }
}