package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class MealEntry {
    String name;
    String desc;
    int hour;
    int minute;
    boolean eaten;
}

public class Home_caretaker extends AppCompatActivity implements AdapterView.OnItemClickListener {
    LinearLayout rootLayout;
    ListView listViewMeals;

    ArrayList<MealEntry> meals = new ArrayList<>();
    MealAdapter mealAdapter;
    AlarmManager alarmManager;
    Button logout_btn;
    String caretakerUUID;
    int caretakerId;
    PatientMealStorage getMealStorage() { return ((GlobalApp)getApplicationContext()).mealStorage; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_caretaker);
        rootLayout = findViewById(R.id.layout_home_caretaker);
        logout_btn = findViewById(R.id.caretakerLogOut);

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

                int[] sortedMealIndices = getMealStorage().caretaker_sortedMealIndices(caretakerId, weekDayIndex);

                meals.clear();
                for(int mealIndex : sortedMealIndices) {
                    if(!getMealStorage().caretaker_isMealIndexValid(caretakerId, weekDayIndex, mealIndex))
                        continue;
                    MealEntry entry = new MealEntry();
                    entry.name = getMealStorage().caretaker_nameOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.desc = getMealStorage().caretaker_descriptionOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.hour = getMealStorage().caretaker_hourOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.minute = getMealStorage().caretaker_minuteOfMeal(caretakerId, weekDayIndex, mealIndex);
                    entry.eaten = false; // getMealStorage().caretaker_eatenOfMeal(caretakerId, weekDayIndex, mealIndex);
                    // TODO: Eaten should be reset when we move on to the next week.
                    meals.add(entry);
                }
                enableNoMealsText(meals.size()==0);
                mealAdapter.notifyDataSetChanged();
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

        logout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setAlarmForMeals(){
        // TODO: If we call setAlarmForMeals twice then the previous alarms should be replaced by new ones.
        //  Currently we just add new alarms every time this function is called. The user will end up with tons of alarms.
        for(MealEntry meal : meals){
            //Sätt tiden för påminelsen
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.HOUR_OF_DAY, meal.hour);
            calendar.set(calendar.MINUTE, meal.minute);
            // calendar.set(calendar.SECOND, 0); // probably not needed because it's 0 by default?

            //Skapa intent för BroadcastReceiver
            Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
            intent.putExtra("meal", meal.name);

            // NOTE(Emarioo): What is this, is it important? It comes from a previous version of this code.
            // ArrayList<Integer> clickedMealList = new ArrayList<>(clickedPositions);
            // intent.putExtra("clickedMeals", clickedMealList);

            //Skapar en pendingIntent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            //Sätt påminelsen
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
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
        // När en list-item klickas på.
        // String meal = meals[position];
        String meal = meals.get(position).name;

        // Toast.makeText(Home_caretaker.this, "Clicked: " + meal, Toast.LENGTH_SHORT).show();

        meals.get(position).eaten = !meals.get(position).eaten;

        //Stäng av påminelsen för maten
        Intent intent = new Intent(this, BroadcastReceiver.class);
        intent.putExtra("meal", meal);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        mealAdapter.notifyDataSetChanged(); // Uppdatera listan för att reflektera ändringarna i clickedPositions-setet.
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
