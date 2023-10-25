package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.protobuf.Value;

import java.util.Objects;


public class PatientProfile extends AppCompatActivity {
    TextView txt_PatientName;
    TextView txt_PatientEmail;
    TextView txt_PatientAddress;
    TextView txt_PatientPreferences;
    TextView txt_PatientAlerts;
    Button btn_BackToMealPlan;
    ImageView img_PatientAvatar;
    FirebaseAuth firebase;
    DatabaseReference dbRef;

    String caregiverUID = "";
    String recipientUID = "";

    CareTaker patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        txt_PatientName = findViewById(R.id.TextBoxPatientName);
        txt_PatientEmail     = findViewById(R.id.TextBoxPatientEmail);
        txt_PatientAddress     = findViewById(R.id.TextBoxPatientAdress);
        txt_PatientPreferences = findViewById(R.id.TextBoxPatientPreferences);
        txt_PatientAlerts      = findViewById(R.id.TextBoxPatientAlerts);
        btn_BackToMealPlan     = findViewById(R.id.ButtonBackToMealPlan);
        img_PatientAvatar      = findViewById(R.id.ImageViewPatientAvatar);

        /* Button to mealplan setup */
        btn_BackToMealPlan.setOnClickListener((View view) -> finish());
        btn_BackToMealPlan.setText(getString(R.string.str_BackToMealPlan));

        firebase = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users/caretakers");

        Intent intent = getIntent();
        caregiverUID = intent.getStringExtra("caregiverUID");
        recipientUID = intent.getStringExtra("recipientUID");

        dbRef.child(recipientUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                patient = snapshot.getValue(CareTaker.class);
                txt_PatientPreferences.setText(patient.getPrefFood());
                txt_PatientEmail.setText(patient.getEmail());
                txt_PatientAddress.setText(patient.getPhoneNr());
                txt_PatientName.setText(patient.getFullName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}