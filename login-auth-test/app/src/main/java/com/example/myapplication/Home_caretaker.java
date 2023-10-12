package com.example.myapplication;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.util.PatientMealStorage;

import java.util.ArrayList;
import java.util.Calendar;

class MealEntry {
    String name;
    String desc;
    int hour;
    int minute;
    boolean eaten;
    String key;
}

public class Home_caretaker extends AppCompatActivity implements AdapterView.OnItemClickListener {
    LinearLayout rootLayout;
    ListView listViewMeals;

    ArrayList<MealEntry> meals = new ArrayList<>();
    MealAdapter mealAdapter;
    AlarmManager alarmManager;

    int weekDayIndex = 0;
    String caretakerUUID;
    int caretakerId;
    PatientMealStorage getMealStorage() { return ((MealApp)getApplicationContext()).mealStorage; }

    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // feature requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_caretaker);
        rootLayout = findViewById(R.id.layout_home_caretaker);

        Intent intent = getIntent();
        caretakerUUID = intent.getStringExtra("caretakerUUID");
        if(caretakerUUID == null) {
            Toast.makeText(this, getResources().getString(R.string.str_caretakerUUID_was_null),Toast.LENGTH_LONG).show();
            caretakerUUID = "kVz12RGTK1W9kaBd7b5imbh3mWg2"; // TODO(Emarioo): Don't hardcode
            caretakerId = getMealStorage().idFromCaretakerUUID(caretakerUUID);
        } else {
            caretakerId = getMealStorage().idFromCaretakerUUID(caretakerUUID);
        }

        getMealStorage().initDBConnection(); // initialize getMealStorage in case it's not
        getMealStorage().pushRefresher_caretaker(caretakerId, new Runnable() {
            @Override
            public void run() {
                int weekDayIndex = getMealStorage().todaysDayIndex();
                Home_caretaker.this.weekDayIndex = weekDayIndex;

                int[] sortedMealIndices = getMealStorage().caretaker_sortedMealIndices(caretakerId, weekDayIndex);

                Calendar calendar = Calendar.getInstance();

                meals.clear();
                for(int mealIndex : sortedMealIndices) {
                    if(!getMealStorage().caretaker_isMealIndexValid(caretakerId, weekDayIndex, mealIndex))
                        continue;

                    MealEntry entry = new MealEntry();
                    entry.name = getMealStorage().caretaker_nameOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.desc = getMealStorage().caretaker_descriptionOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.hour = getMealStorage().caretaker_hourOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.minute = getMealStorage().caretaker_minuteOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.key = getMealStorage().caretaker_keyOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.eaten = getMealStorage().caretaker_eatenOfMeal(caretakerId, weekDayIndex, mealIndex);

                    if(entry.hour * 100 + entry.minute > calendar.get(Calendar.HOUR_OF_DAY)*100 + calendar.get(Calendar.MINUTE)) {
                        getMealStorage().caretaker_setEatenOfMeal(caretakerId, weekDayIndex, mealIndex, false);
                    }
                    meals.add(entry);
                }
                enableNoMealsText(meals.size()==0);
                mealAdapter.notifyDataSetChanged();

                refreshAlarmsForMeals();
            }
        });

        listViewMeals = findViewById(R.id.listViewMeals);
        Button buttonAlarm = findViewById(R.id.buttonAlarm);

        mealAdapter = new MealAdapter(this, meals);
        listViewMeals.setAdapter(mealAdapter);
        listViewMeals.setOnItemClickListener(this);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        buttonAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Home_caretaker.this, AlarmActivity.class);
                startActivityForResult(intent, 1);

            }
        });
        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(POST_NOTIFICATIONS);
        }
    }
    private void refreshAlarmsForMeals(){
        if(!alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, getResources().getString(R.string.missing_alarm_permission),Toast.LENGTH_LONG).show();
        } else {
            for (int index = 2;index < meals.size();index++) {
                MealEntry meal = meals.get(index);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, meal.hour);
                calendar.set(Calendar.MINUTE, meal.minute);
                calendar.set(Calendar.SECOND, 0);

                // System.out.println(calendar.getTimeInMillis() - System.currentTimeMillis());

                long nowTime = System.currentTimeMillis();
                long mealTime = calendar.getTimeInMillis();

                if(mealTime > nowTime && !meal.eaten) {
                    int requestCode = index;

                    long time = calendar.getTimeInMillis();

                    // time = System.currentTimeMillis() + 4000; // debug purpose

                    Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
                    intent.putExtra("name", meal.name);
                    intent.putExtra("time",Helpers.FormatTime(meal.hour,meal.minute));
                    intent.putExtra("desc", meal.desc);
                    intent.putExtra("caretakerUUID", caretakerUUID);
                    intent.putExtra("dayIndex", weekDayIndex);
                    intent.putExtra("mealKey", meal.key);
                    intent.putExtra("noticeCount",0); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
                    intent.putExtra("requestCode",requestCode); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
                    intent.putExtra("alarmAtMillis", time);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                } else {
                    // meal time has already passsed
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Uppdatera UI baserat på resultatet
            // Om du har lagt till extra data i intentet, kan du hämta det här
            mealAdapter.notifyDataSetChanged(); // Uppdatera listan för att reflektera eventuella ändringar
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MealEntry meal = meals.get(position);
        int requestCode = position;

        Calendar calendar = Calendar.getInstance();
        if(meal.hour * 100 + meal.minute > calendar.get(Calendar.HOUR_OF_DAY)*100 + calendar.get(Calendar.MINUTE)) {
            Toast.makeText(this,getResources().getString(R.string.meal_marked_too_early),Toast.LENGTH_LONG).show();
            return;
        }

        getMealStorage().caretaker_setEatenOfMeal(caretakerId, weekDayIndex, position, !meals.get(position).eaten);
        // meals.get(position).eaten = !meals.get(position).eaten;

        Intent intent = new Intent(this, BroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
        if(pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
        // mealAdapter is updated when database is written to, refresher is called and UI updates.
        // mealAdapter.notifyDataSetChanged(); // Uppdatera listan för att reflektera ändringarna i clickedPositions-setet.
    }

    public void enableNoMealsText(boolean show) {
        int viewIndex = 1;
        if(show) {
            TextView textview = null;

            if(rootLayout.getChildCount() > viewIndex) {
                View view = rootLayout.getChildAt(viewIndex);
                Object tagValue = view.getTag(R.id.tag_no_meals);
                if(tagValue != null) {
                    if((Boolean)tagValue) {
                        textview = (TextView)view;
                    }
                }
            }
            if(textview == null) {
                textview = new TextView(this);
                textview.setText(getResources().getString(R.string.str_no_meals));
                textview.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 30); // TODO(Emarioo): Don't hardcode text size
                textview.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                textview.setTag(R.id.tag_no_meals, true);
                textview.setGravity(Gravity.CENTER);
                textview.setTextColor(getResources().getColor(R.color.black));
                // insert text after title text ("Elder Care")
                rootLayout.addView(textview, viewIndex);
            } else {
                // text already exists
            }
        } else {
            if(rootLayout.getChildCount() > viewIndex) {
                View view = rootLayout.getChildAt(viewIndex);
                Object tagValue = view.getTag(R.id.tag_no_meals);
                if(tagValue != null) {
                    if((Boolean)tagValue) {
                        rootLayout.removeView(view);
                    }
                }
            }
        }
    }
}
