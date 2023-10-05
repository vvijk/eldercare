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

                        if (isCareGiver) {
                            // Register as a CareGiver
                            CareGiver newCareGiver = new CareGiver(firstname, lastname, phoneNr, email, personNummer);

                            DatabaseReference careGiverRef = dbRef.child("caregivers");
                            careGiverRef.child(uid).setValue(newCareGiver)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            callback.onSuccess("CareGiver account created!");
                                        } else {
                                            callback.onError("Error creating CareGiver profile");
                                        }
                                    });
                        } else {
                            // Register as a CareTaker
                            // Newly registerd CareTakers have Handler set to "null"
                            CareTaker newCareTaker = new CareTaker(firstname, lastname, phoneNr, email, personNummer, prefFood, PIN, null);
                            newCareTaker.setPrefFood(prefFood);
                            newCareTaker.setPIN(PIN);

                            DatabaseReference careTakerRef = dbRef.child("caretakers");
                            careTakerRef.child(uid).setValue(newCareTaker)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            callback.onSuccess("CareTaker account created!");
                                        } else {
                                            callback.onError("Error creating CareTaker profile");
                                        }
                                    });
                        }
                    } else {
                        callback.onError("Authentication failed: " + task.getException().getMessage());
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
        //Log.d("dbtest", "caregiverUID: " + caregiverUID);
        //Log.d("dbtest", "caretakerUID: " + caretakerUID);
        DatabaseReference caregiversRef = dbRef.child("caregivers").child(caregiverUID);
        DatabaseReference caretakersRef = dbRef.child("caretakers").child(caretakerUID);

        caregiversRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the caregiver's data
                    CareGiver caregiver = dataSnapshot.getValue(CareGiver.class);
                    if (caregiver != null) {
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
                        // Add the caretakerUID to the caregiver's list of caretakers
                        caregiver.getCaretakers().add(caretakerUID);

                        // Update the caregiver's data in the database
                        caregiversRef.setValue(caregiver)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("dbtest", "Caretaker added successfully to caregiver.");

                                        // Now, add the caregiver's UID to the caretaker's data as the handler
                                        caretakersRef.child("handler").setValue(caregiverUID)
                                                .addOnCompleteListener(handlerTask -> {
                                                    if (handlerTask.isSuccessful()) {
                                                        Log.d("dbtest", "Handler set successfully for caretaker.");
                                                        callback.onCaretakerAdded("Caretaker added successfully.");
                                                    } else {
                                                        Log.d("dbtest", "Failed to set handler for caretaker.");
                                                        callback.onCaretakerAddError("Failed to set handler for caretaker.");
                                                    }
                                                });
                                    } else {
                                        Log.d("dbtest", "Failed to add caretaker to caregiver.");
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
