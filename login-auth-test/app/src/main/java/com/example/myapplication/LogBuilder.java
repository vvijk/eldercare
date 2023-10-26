package com.example.myapplication;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.util.MealStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.checkerframework.checker.units.qual.C;

public class LogBuilder {
    //Class that helps build specific logs
    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    CareGiver caregiver;
    CareTaker caretaker;
    MealStorage.Meal meal;

    private void init_db(){
        dbRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
    }
    public String getUserID(){
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }
    private interface CaregiverCallback{
        void setCareGiver(CareGiver caregiver);
    }
    private interface CaretakerCallback{
        void setCareTaker(CareTaker caretaker);
    }
    private interface MealCallback{
        void setMeal(MealStorage.Meal meal);
    }

    private void fetchCaregiver(CaregiverCallback cb){
        DatabaseReference uidRef = dbRef.child("users/caregivers").child(this.getUserID());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CareGiver caregiver = dataSnapshot.getValue(CareGiver.class);
                cb.setCareGiver(caregiver);
                Log.d("TAG", caregiver.getFirstName());

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", databaseError.getMessage()); //TODO: Don't ignore errors!
            }
        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }
    private void fetchCaretaker(String uid, CaretakerCallback cb){
        DatabaseReference uidRef = dbRef.child("users/caretakers").child(uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CareTaker caretaker = dataSnapshot.getValue(CareTaker.class);
                cb.setCareTaker(caretaker);
                Log.d("TAG", caretaker.getFirstName());

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", databaseError.getMessage()); //TODO: Don't ignore errors!
            }
        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }


    public void LogPatientCreated(String patientUID){
        CaretakerCallback cb = (out) -> {
            this.caretaker = out;

        };
        fetchCaretaker(patientUID, cb);
        //LogItem toAdd = new LogItem(null, );
    }

    public void LogPatientAdded(String caregiverUID, String patientUID){

    }

    public void LogEmergency(String patientUID){

    }

    public void LogMealConfirmed(String patientUID /*, meal*/){

    }
    public void LogMealMissed(String patientUID /*, meal*/){

    }

    public void LogMealAdded(String patientUID /*, meal*/){

    }

    public void LogMealDeleted(String patientUID /*, meal*/){

    }



}
