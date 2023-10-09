package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Home_caretaker extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private Set<Integer> clickedPositions = new HashSet<>(); // Håller reda på klickade positioner
    private HashMap<String, String> mealTimes = new HashMap<>();

    
    ListView listViewMeals;
    // String[] meals = {"Frukost", "Mellanmål", "Lunch", "Mellanmål", "Middag"};
    ArrayList<String> meals = new ArrayList<>();
    MealAdapter mealAdapter;
    AlarmManager alarmManager;

    String caretakerUUID;
    int caretakerId;
    PatientMealStorage getMealStorage() { return ((GlobalApp)getApplicationContext()).mealStorage; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_caretaker);

        // meals.add("Frukost");
        // meals.add("Mellanmål");
        // meals.add("Lunch");
        // meals.add("Mellanmål");
        // meals.add("Middag");

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
                    String mealName = getMealStorage().caretaker_nameOfMeal(caretakerId, weekDayIndex, mealIndex);
                    meals.add(mealName);
                }
                if(meals.size()==0)
                    meals.add("No meals"); // TODO: The UI should display that there are no meals, but how? It shouldn't be done here.
                mealAdapter.notifyDataSetChanged();
            }
        });

        //Initsiera tiderna för måltid
        mealTimes.put("Frukost", "08:00");
        mealTimes.put("Mellanmål", "10:00");
        mealTimes.put("Lunch", "12:30");
        mealTimes.put("Mellanmål", "15:00");
        mealTimes.put("Middag", "18:00");

        listViewMeals = findViewById(R.id.listViewMeals);
        Button buttonAlarm = findViewById(R.id.buttonAlarm);

        mealAdapter = new MealAdapter(this, meals, clickedPositions);
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
    }

    private void setAlarmForMeals(){
        for(Map.Entry<String, String> entry : mealTimes.entrySet()){
            String meal = entry.getKey();
            String time = entry.getValue();

            //Dela upp tiden
            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            //Sätt tiden för påminelsen
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.HOUR_OF_DAY, hour);
            calendar.set(calendar.MINUTE, minute);
            calendar.set(calendar.SECOND, 0);

            //Skapa intent för BroadcastReceiver
            Intent intent = new Intent(this, ReminderBroadcastReceiver.class);
            intent.putExtra("meal", meal);
            ArrayList<Integer> clickedMealList = new ArrayList<>(clickedPositions);
            intent.putExtra("clickedMeals", clickedMealList);

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
        String meal = meals.get(position);

        Toast.makeText(Home_caretaker.this, "Clicked: " + meal, Toast.LENGTH_SHORT).show();

        if (!clickedPositions.add(position)) {
            clickedPositions.remove(position);
        }

        //Stäng av påminelsen för maten
        Intent intent = new Intent(this, BroadcastReceiver.class);
        intent.putExtra("meal", meal);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);

        mealAdapter.notifyDataSetChanged(); // Uppdatera listan för att reflektera ändringarna i clickedPositions-setet.
    }
}
