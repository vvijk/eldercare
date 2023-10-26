package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.util.MealStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyNotificationManager{

    private static MyNotificationManager instance;
    // private ValueEventListener valueEventListener;
    // private DatabaseReference databaseReference;
    DatabaseReference caretakersRef;

    private Context context;

    private MyNotificationManager(Context context) {
        // Initialize Firebase Database
        this.context = context;
    }

    public static MyNotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new MyNotificationManager(context);
        }
        return instance;
    }

    public void notificationListener() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUser = auth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference careGiverTakerReference = database.getReference("users/caregivers/" + currentUser + "/caretakers");
        DatabaseReference mealsRef = database.getReference("meals");
        caretakersRef = database.getReference("users/caretakers");

        ArrayList<String> patients = new ArrayList<>();
        ArrayList<String> patientsInMeal;

        patients = getPatientFromGiver(careGiverTakerReference);
        checkPatientMealPatient(mealsRef, patients);
        checkLarm(caretakersRef, patients);
    }

    public void checkLarm(DatabaseReference caretakersRef, ArrayList<String>caretakerUIDs){

        caretakersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String caretakerUID = snapshot.getKey();  // Retrieve the caretaker UID
                    for(int i=0; i < caretakerUIDs.size(); i++){
                        if(caretakerUID.equals(caretakerUIDs.get(i))){
                            boolean larm = false;
                            if(snapshot.child("larm").getValue() != null)
                                larm = snapshot.child("larm").getValue(Boolean.class);

                            Log.d("lars", "hitta caretaker: " + caretakerUID);
                            if (larm){
                                String title = context.getString(R.string.notif_alarm_title);
                                String msg = context.getString(R.string.notif_alarm_msg, snapshot.child("firstName").getValue(String.class) + " " + snapshot.child("lastName").getValue(String.class));
                                caretakersRef.child(caretakerUID).child("larm").setValue(false);
                                makeNotification(title, msg);
                            }
                        }
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });

    }

    public ArrayList<String> getPatientFromGiver(DatabaseReference careGiverTakerReference) {
        ArrayList<String> caretakerUIDs = new ArrayList<>();
        careGiverTakerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String caretakerUID = snapshot.getKey();  // Retrieve the caretaker UID
                    caretakerUIDs.add(caretakerUID);
                    //Log.d("larss", caretakerUID);
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors
            }
        });

        return caretakerUIDs;
    }

    //returnerar en lista på currentUser (caregivers) alla patienter.
    public ArrayList<String> checkPatientMealPatient(DatabaseReference mealsRef, ArrayList<String>caretakerUIDs){
        ArrayList<String> patientsInMeal = new ArrayList<>();

        mealsRef.addValueEventListener(new ValueEventListener() { //gör en lyssnare på /meals.
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //kollar alla barn i /meals
                for(DataSnapshot patientSnapshot : snapshot.getChildren()){
                    for(int i=0; i < caretakerUIDs.size(); i++){
                        if(patientSnapshot.getKey().equals(caretakerUIDs.get(i))){//jämför och kollar så att caretakern finns med i meals.
                            //kollar så vi går in på rätt dag
                            checkDay(patientSnapshot.getKey(), mealsRef);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return patientsInMeal;
    }

    private void checkDay(String patientUID, DatabaseReference mealsRef){
        Date currentDate = new Date();

        // Format the date to display the first three letters of the day (e.g., "Mon")
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        String todaysDay = sdf.format(currentDate).toLowerCase();
        String day = ("fri");

        DatabaseReference finalMealsRef = mealsRef.child(patientUID).child(day);

        finalMealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for(DataSnapshot mealSnapshot : dataSnapshot.getChildren()){   //går igenom alla Mål, frukost, lunch .....
                       // Log.d("larss", "här har vi: "+mealSnapshot.getKey());
                        checkTime(mealSnapshot, patientUID);

                    }
                } else {
                    Log.d("CurrentDayData", "No data found for the current day.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any errors that may occur during the retrieval
                Log.e("FirebaseError", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }

    private void checkTime(DataSnapshot mealSnapshot, String patientUID){
        caretakersRef.child(patientUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int hourInt = 0;
                int minuteInt = 0;
                boolean checkEaten = false;
                boolean notified = false;
                // DatabaseReference mealTypeRef = mealRef.child(mealType);

                if(mealSnapshot.child("hour").getValue() != null){
                    String hour = mealSnapshot.child("hour").getValue().toString();
                    Log.d("larss", "hour är:" + hour);
                    hourInt = Integer.parseInt(hour);
                }
                if(mealSnapshot.child("minute").getValue() != null){
                    String minute = mealSnapshot.child("minute").getValue().toString();
                    Log.d("larss", "min är:" + minute);
                    minuteInt = Integer.parseInt(minute);
                }

                if(mealSnapshot.child("eaten").getValue() != null){
                    checkEaten = mealSnapshot.child("eaten").getValue(Boolean.class);
                }

                if(mealSnapshot.child("notified").getValue() != null){
                    notified = mealSnapshot.child("notified").getValue(Boolean.class);
                }

                String title = context.getString(R.string.notif_not_eaten_title);
                String desc = "";
                if(mealSnapshot.child("desc").getValue() != null)
                    desc = ": " + mealSnapshot.child("desc").getValue(String.class);
                String msg = context.getString(R.string.notif_not_eaten_msg,snapshot.child("firstName").getValue(String.class)+ " "+snapshot.child("lastName").getValue(String.class),
                    Helpers.FormatTime(hourInt, minuteInt) + " " + mealSnapshot.child("name").getValue(String.class), desc);

                //om recipient inte har ätit
                if(!checkEaten && !notified){
                    Calendar currentTime = Calendar.getInstance();
                    Calendar targetTime = (Calendar)Calendar.getInstance().clone();
                    targetTime.set(Calendar.HOUR_OF_DAY, hourInt);
                    targetTime.set(Calendar.MINUTE, minuteInt);
                    targetTime.add(Calendar.MINUTE, 2*45);

                    // LocalTime currentTime = LocalTime.now();
                    // LocalTime targetTime = LocalTime.of(hourInt[0], minuteInt[0]).plusHours(1).plusMinutes(30);

                    if(currentTime.after(targetTime)) {
                        Log.d("larss","har inte ätit efter tiden: ");
                        makeNotification(title, msg);
                        Log.d("krok", "hej:" + caretakersRef.child(patientUID).child("notified").toString());
                        //caretakersRef.child(patientUID).child("notified").setValue(true);

                    } else{

                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // private boolean isNoon(long currentTimeMillis) {
    //     Calendar calendar = Calendar.getInstance();
    //     calendar.setTimeInMillis(currentTimeMillis);
    //     return calendar.get(Calendar.HOUR_OF_DAY) == 12 && calendar.get(Calendar.MINUTE) == 0;
    // }

                                          
    public void makeNotification(String title, String msg) {

        String channelID = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                channelID);
        builder.setSmallIcon(R.drawable.ic_notifications_active);
        builder.setContentTitle(title);
        builder.setContentText(msg);
        builder.setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(context, home_caregiver.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "Some value");

        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);

        android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = notificationManager.getNotificationChannel(channelID);
            if (notificationChannel == null) {
                int importance = android.app.NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelID, "some description", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);

            }
        }

        notificationManager.notify(0, builder.build());
    }

    // public void cleanup() {
    //     // Remove the ValueEventListener and clean up resources
    //     if (databaseReference != null && valueEventListener != null) {
    //         databaseReference.removeEventListener(valueEventListener);
    //     }
    // }
}
