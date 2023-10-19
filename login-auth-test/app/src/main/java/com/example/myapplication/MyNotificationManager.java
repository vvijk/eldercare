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
    private ValueEventListener valueEventListener;
    private DatabaseReference databaseReference;

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
        DatabaseReference caretakersRef = database.getReference("users/caretakers");

        ArrayList<String> patients = new ArrayList<>();
        ArrayList<String> patientsInMeal;

        patients = getPatientFromGiver(careGiverTakerReference);
       // checkPatientMealPatient(mealsRef, patients);
        checkLarm(caretakersRef, patients);


    }

    public void checkLarm(DatabaseReference caretakersRef, ArrayList<String>caretakerUIDs){

        caretakersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String caretakerUID = snapshot.getKey();  // Retrieve the caretaker UID
                    for(int i=0; i < caretakerUIDs.size(); i++){
                        if(snapshot.getKey().equals(caretakerUIDs.get(i))){
                            DatabaseReference finalCaretakersRef = caretakersRef.child(snapshot.getKey());
                            finalCaretakersRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for(DataSnapshot larmSnapshot : snapshot.getChildren()) {
                                        Log.d("lars", "hitta caretaker: " + larmSnapshot.getValue());
                                        if (larmSnapshot.getKey().equals("larm") && larmSnapshot.getValue(boolean.class)){

                                                String msg =  finalCaretakersRef.child("name").toString();
                                                String title = "Larm";
                                                makeNotification(title, msg);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
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

    private void checkDay(String patientSnapshot, DatabaseReference mealsRef){
        Date currentDate = new Date();

        // Format the date to display the first three letters of the day (e.g., "Mon")
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        String todaysDay = sdf.format(currentDate);
        String day = ("fri");

        DatabaseReference finalMealsRef = mealsRef.child(patientSnapshot).child(day);

        finalMealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    for(DataSnapshot mealSnapshot : dataSnapshot.getChildren()){   //går igenom alla Mål, frukost, lunch .....
                       // Log.d("larss", "här har vi: "+mealSnapshot.getKey());
                        checkTime(finalMealsRef, mealSnapshot.getKey());

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

    private void checkTime(DatabaseReference mealRef, String mealType){
        final int[] hourInt = new int[1];
        final int[] minuteInt = new int[1];
        final boolean[] checkEaten = new boolean[1];
        DatabaseReference mealTypeRef = mealRef.child(mealType);
        String title ="Har inte ätit";
        String msg = "lasse har inte ätit";

        mealTypeRef.addValueEventListener(new ValueEventListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot mealSnapshot : snapshot.getChildren()){   //går igenom desc, hour, minute, eaten
                    if(mealSnapshot.getKey().equals("hour")){
                        String hour = mealSnapshot.getValue().toString();
                        Log.d("larss", "hour är:" + hour);
                        hourInt[0] = Integer.parseInt(hour);
                    }
                    if(mealSnapshot.getKey().equals("minute")){
                        String minute = mealSnapshot.getValue().toString();
                        Log.d("larss", "min är:" + minute);
                        minuteInt[0] = Integer.parseInt(minute);
                    }

                   if(mealSnapshot.getKey().equals("eaten")){
                       checkEaten[0] = mealSnapshot.getValue(Boolean.class);
                   }
                }
                //om den inte har ätit
                if(!checkEaten[0]){                                                                                       LocalTime currentTime = LocalTime.now();
                    LocalTime targetTime = LocalTime.of(hourInt[0], minuteInt[0]).plusHours(1).plusMinutes(30);
                    LocalTime currTime = LocalTime.now();
                    if(currentTime.isAfter(targetTime)){ //om klockan är efter tiden
                        makeNotification(title, msg);
                    } else{

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private boolean isNoon(long currentTimeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        return calendar.get(Calendar.HOUR_OF_DAY) == 12 && calendar.get(Calendar.MINUTE) == 0;
    }

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

    public void cleanup() {
        // Remove the ValueEventListener and clean up resources
        if (databaseReference != null && valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}
