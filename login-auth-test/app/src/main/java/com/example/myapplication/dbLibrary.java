package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class dbLibrary {
    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    public dbLibrary() {
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");
    }
    public void registerUser(String email, String password, String firstname, String lastname, String phoneNr, String personNummer, boolean isCareGiver, final RegisterCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String uid = user.getUid();
                        User newUser = new User(firstname, lastname, phoneNr, email, personNummer, isCareGiver);
                        dbRef.child(uid).setValue(newUser)
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
}
