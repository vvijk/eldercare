package com.example.myapplication;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class dbLibrary {
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private Context context;

    //Example of how to create a new instance:
    //dbLibrary db;
    //db = new dbLibrary(Register.this);
    public dbLibrary(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
    }
    public void registerUser(String email,
                             String password,
                             String firstname,
                             String lastname,
                             String phoneNr,
                             String personNummer,
                             boolean isCareGiver,
                             final RegisterCallback callback) {

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context ,"Ange epostadress", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(password) || password.length() < 6) {
            Toast.makeText(context, "Lösenordet måste vara minst 6 siffror", Toast.LENGTH_SHORT).show();
            return;
        } else if(TextUtils.isEmpty(firstname)){
            Toast.makeText(context, "Ange förnamn", Toast.LENGTH_SHORT).show();
            return;
        } else if(TextUtils.isEmpty(lastname)){
            Toast.makeText(context, "Ange efternamn", Toast.LENGTH_SHORT).show();
            return;
        } else if(TextUtils.isEmpty(phoneNr) || !TextUtils.isDigitsOnly(phoneNr)){
            Toast.makeText(context, "Ange telefonnummer", Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(personNummer) || personNummer.length() < 12) {
            Toast.makeText(context, "Ange personnummret i 12 siffror", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();
                        User newUser = new User(firstname, lastname, phoneNr, email, personNummer, isCareGiver);

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
