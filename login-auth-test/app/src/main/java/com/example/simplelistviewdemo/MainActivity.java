package com.example.simplelistviewdemo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.channels.InterruptedByTimeoutException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private Set<Integer> clickedPositions = new HashSet<>(); // Håller reda på klickade positioner
    private HashMap<String, String> mealTimes = new HashMap<>();


    ListView listViewMeals;
    String[] meals = {"Frukost", "Mellanmål", "Lunch", "Mellanmål", "Middag"};
    MealAdapter mealAdapter;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Intent intent = new Intent(MainActivity.this, AlarmActivity.class);
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
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

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
    //När en list-item klickas på.
        // När en list-item klickas på.
        String meal = meals[position];
        Toast.makeText(MainActivity.this, "Clicked: " + meal, Toast.LENGTH_SHORT).show();

        if (!clickedPositions.add(position)) {
            clickedPositions.remove(position);
        }

        //Stäng av påminelsen för maten
        Intent intent = new Intent(this, BroadcastReceiver.class);
        intent.putExtra("meal", meal);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,0);
        alarmManager.cancel(pendingIntent);

        mealAdapter.notifyDataSetChanged(); // Uppdatera listan för att reflektera ändringarna i clickedPositions-setet.
    }
}