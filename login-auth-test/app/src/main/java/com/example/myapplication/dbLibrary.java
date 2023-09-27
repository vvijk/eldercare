package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class dbLibrary {
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private Context context;
    private Helpers help;

    //Example of how to create a new instance:
    //dbLibrary db;
    //db = new dbLibrary(Register.this);
    public dbLibrary(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        help = new Helpers(context);
    }
    public void registerUser(String email,
                             String password,
                             String firstname,
                             String lastname,
                             String phoneNr,
                             String personNummer,
                             String prefFood,
                             String PIN,
                             boolean isCareGiver,
                             final RegisterCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();
                        User newUser = new User(firstname, lastname, phoneNr, email, personNummer, prefFood, PIN, isCareGiver);

                        Log.d("db123", "newuser.prefFood: " + newUser.getPrefFood());
                        Log.d("db123", "newuser.PIN: " + newUser.getPIN());

                        DatabaseReference userRef = isCareGiver ? dbRef.child("caregivers") : dbRef.child("caretakers");

                        userRef.child(uid).setValue(newUser)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        callback.onSuccess("Konto skapat!");
                                    } else {
                                        callback.onError("Fel vid skapande av användarprofil");
                                    }
                                });
                    } else {
                        callback.onError("Autentiseringen misslyckades: " + task.getException().getMessage());
                    }
                });
    }
    public interface RegisterCallback {
        void onSuccess(String message);

        void onError(String errorMessage);
    }
    public String getUserID(){
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }

}
