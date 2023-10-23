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
import android.os.Build;
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

import com.example.myapplication.util.MealStorage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import android.app.AlertDialog;
import android.content.DialogInterface;


class MealEntry {
    String name;
    String desc;
    int hour;
    int minute;
    boolean eaten;
    String key;
}

public class RecipientHome extends AppCompatActivity implements AdapterView.OnItemClickListener {
    LinearLayout rootLayout;
    ListView listViewMeals;

    ArrayList<MealEntry> meals = new ArrayList<>();
    MealAdapter mealAdapter;
    AlarmManager alarmManager;
    Button logout_btn;
    int weekDayIndex = 0;
    String recipientUID;
    int caretakerId;
    MealStorage getMealStorage() { return ((MealApp)getApplicationContext()).mealStorage; }

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
        setContentView(R.layout.activity_home_recipient);
        rootLayout = findViewById(R.id.layout_recipient_home);
        logout_btn = findViewById(R.id.recipientLogOut);

        dbLibrary lib = new dbLibrary(this);
        recipientUID = lib.getUserID();

        // Intent intent = getIntent();
        // recipientUID = intent.getStringExtra("recipientUID");
        if(recipientUID == null) {
            Toast.makeText(this, getResources().getString(R.string.str_recipientUID_was_null),Toast.LENGTH_LONG).show();
            recipientUID = "kVz12RGTK1W9kaBd7b5imbh3mWg2"; // TODO(Emarioo): Don't hardcode
            caretakerId = getMealStorage().idFromCaretakerUID(recipientUID);
        } else {
            caretakerId = getMealStorage().idFromCaretakerUID(recipientUID);
        }

        getMealStorage().initDBConnection(); // initialize getMealStorage in case it's not
        getMealStorage().pushRefresher_caretaker(caretakerId, new Runnable() {
            @Override
            public void run() {
                int weekDayIndex = getMealStorage().todaysDayIndex();
                RecipientHome.this.weekDayIndex = weekDayIndex;

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
                Intent intent = new Intent(RecipientHome.this, AlarmActivity.class);
                intent.putExtra("recipientUID", recipientUID);
                startActivityForResult(intent, 1);

            }
        });

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
                */
                showLogoutConfirmationDialog();
            }
        });

        if (ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(POST_NOTIFICATIONS);
        }
    }
    private void showLogoutConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.sureLogOut);
        builder.setPositiveButton(R.string.Ja, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton(R.string.Nej, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void refreshAlarmsForMeals(){
        int firstRequestCode = 9929;
        int nextRequestCode = firstRequestCode;
        int dayCount = 2;
        for (int dayOffset=0;dayOffset<dayCount;dayOffset++){
            if(dayOffset == 0) {
                for (int index = 0;index < meals.size();index++) {
                    MealEntry meal = meals.get(index);

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.HOUR_OF_DAY, meal.hour);
                    calendar.set(Calendar.MINUTE, meal.minute);
                    calendar.set(Calendar.SECOND, 0);

                    long nowTime = System.currentTimeMillis();
                    long mealTime = calendar.getTimeInMillis();

                    if (mealTime > nowTime && !meal.eaten) {
                        int requestCode = nextRequestCode++; // TODO: 10 will collide meals if there are 10

                        long time = calendar.getTimeInMillis();

                        // time = System.currentTimeMillis() + 4000; // debug purpose
                        // System.out.println(meal.name +", time left: "+ ((calendar.getTimeInMillis() - System.currentTimeMillis()) / (60.f*60*1000)));

                        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
                        intent.putExtra("name", meal.name);
                        intent.putExtra("time", Helpers.FormatTime(meal.hour, meal.minute));
                        intent.putExtra("desc", meal.desc);
                        intent.putExtra("recipientUID", recipientUID);
                        intent.putExtra("dayIndex", weekDayIndex);
                        intent.putExtra("mealKey", meal.key);
                        intent.putExtra("noticeCount", 0); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
                        intent.putExtra("requestCode", requestCode); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
                        intent.putExtra("alarmAtMillis", time);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                    } else {
                        // meal time has already passsed
                    }
                }
            } else {
                int weekDay = (weekDayIndex + dayOffset) % 7;
                int mealCount = getMealStorage().caretaker_countOfMeals(caretakerId, weekDay);
                for (int index = 0;index < mealCount;index++) {
                    MealStorage.Meal meal = getMealStorage().caretaker_getMeal(caretakerId, weekDay, index);

                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, dayOffset);
                    calendar.set(Calendar.HOUR_OF_DAY, meal.hour);
                    calendar.set(Calendar.MINUTE, meal.minute);
                    calendar.set(Calendar.SECOND, 0);

                    long nowTime = System.currentTimeMillis();
                    long mealTime = calendar.getTimeInMillis();

                    if (mealTime > nowTime && !meal.eaten) {
                        int requestCode = nextRequestCode++; // TODO: 10 will collide meals if there are 10

                        long time = calendar.getTimeInMillis();

                        // time = System.currentTimeMillis() + 4000; // debug purpose
                        // System.out.println(meal.name +", time left: "+ ((calendar.getTimeInMillis() - System.currentTimeMillis()) / (60.f*60*1000)));

                        Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
                        intent.putExtra("name", meal.name);
                        intent.putExtra("time", Helpers.FormatTime(meal.hour, meal.minute));
                        intent.putExtra("desc", meal.desc);
                        intent.putExtra("recipientUID", recipientUID);
                        intent.putExtra("dayIndex", weekDayIndex);
                        intent.putExtra("mealKey", meal.key);
                        intent.putExtra("noticeCount", 0); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
                        intent.putExtra("requestCode", requestCode); // 0 is for the initial notification, no buttons. 1+ will have eaten or not eaten
                        intent.putExtra("alarmAtMillis", time);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
                    } else {
                        // meal time has already passsed
                    }
                }
            }
        }
        // The purpose of this is to remove alarms for meals that have been deleted from the database.
        // Those meals should not cause a reminder anymore.
        int lastRequestCode = firstRequestCode + 100;
        for(int rq=nextRequestCode;rq<=lastRequestCode;rq++) {
            Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, rq, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
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

        Intent intent = new Intent(this, BroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE);
        if(pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
        // mealAdapter is updated when database is written to, refresher is called and UI updates.
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
