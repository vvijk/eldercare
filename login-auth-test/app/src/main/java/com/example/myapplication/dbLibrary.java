package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
    public void getCaretakerUidByEmail(String email, final UserUidCallback callback) {
        DatabaseReference caregiversRef = dbRef.child("caretakers");

        caregiversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userEmail = userSnapshot.child("email").getValue(String.class);
                    //Log.d("dbtest", "User email from database: " + userEmail);
                    if (userEmail != null && userEmail.equals(email)) {
                        String uid = userSnapshot.getKey(); // This will give you the UID
                        //Log.d("dbtest", "Matching UID found: " + uid + " For email: " + email);
                        callback.onUserUidFound(uid);
                        return;
                    }
                }
                //Log.d("dbtest", "No matching email found.");
                callback.onUserUidNotFound();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any database errors
                callback.onUserUidError(databaseError.getMessage());
            }
        });
    }
    public interface UserUidCallback {
        void onUserUidFound(String uid);

        void onUserUidNotFound();

        void onUserUidError(String errorMessage);
    }
    public void addCaretakerToGiver(String caregiverUID, String caretakerUID, final CaretakerAddCallback callback) {
        DatabaseReference usersRef = dbRef.child("caregivers").child(caregiverUID);
        Log.d("dbtest", "userRef:" + usersRef.toString());
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the caregiver user's data
                    User caregiver = dataSnapshot.getValue(User.class);
                    if (caregiver != null) {
                        Log.d("dbtest", "Caregiver data retrieved: " + caregiver.toString());

                        if (caregiver.getCaretakers() == null) {
                            caregiver.setCaretakers(new ArrayList<>());
                        } else {
                            // Check if the caretakerUID is already in the list
                            if (caregiver.getCaretakers().contains(caretakerUID)) {
                                Log.d("dbtest", "Caretaker with UID: " + caretakerUID + " already exists.");
                                callback.onCaretakerAddError("Caretaker with UID: " + caretakerUID + " already exists.");
                                return;
                            }
                        }

                        Log.d("dbtest", "Adding caretaker with UID: " + caretakerUID + " to caregiver.");
                        caregiver.getCaretakers().add(caretakerUID);

                        // Update the caregiver user's data with the new "tab"
                        usersRef.setValue(caregiver)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("dbtest", "Caretaker added successfully.");
                                        callback.onCaretakerAdded("Caretaker added successfully.");
                                    } else {
                                        Log.d("dbtest", "Failed to add caretaker.");
                                        callback.onCaretakerAddError("Failed to add caretaker.");
                                    }
                                });
                    } else {
                        Log.d("dbtest", "Caregiver data is null.");
                        callback.onCaretakerAddError("Caregiver data is null.");
                    }
                } else {
                    Log.d("dbtest", "Caregiver with UID " + caregiverUID + " not found.");
                    callback.onCaretakerAddError("Caregiver with UID " + caregiverUID + " not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any database errors
                Log.d("dbtest", "Database error: " + databaseError.getMessage());
                callback.onCaretakerAddError("Database error: " + databaseError.getMessage());
            }
        });
    }

    public interface CaretakerAddCallback {
        void onCaretakerAdded(String message);

        void onCaretakerAddError(String errorMessage);
    }

}
