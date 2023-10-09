package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;


public class PatientProfile extends AppCompatActivity {
    TextView txt_PatientName;
    TextView txt_PatientAddress;
    TextView txt_PatientPreferences;
    TextView txt_PatientAlerts;
    Button btn_BackToMealPlan;
    ImageView img_PatientAvatar;
    FirebaseAuth firebase;
    DatabaseReference dbRef;

    String caregiverUUID = "";
    String caretakerUUID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        txt_PatientName = findViewById(R.id.TextBoxPatientName);
        txt_PatientAddress     = findViewById(R.id.TextBoxPatientAdress);
        txt_PatientPreferences = findViewById(R.id.TextBoxPatientPreferences);
        txt_PatientAlerts      = findViewById(R.id.TextBoxPatientAlerts);
        btn_BackToMealPlan     = findViewById(R.id.ButtonBackToMealPlan);
        img_PatientAvatar      = findViewById(R.id.ImageViewPatientAvatar);

        /*Button to mealplan setup*/
        btn_BackToMealPlan.setOnClickListener((View view) -> finish());
        btn_BackToMealPlan.setText(getString(R.string.str_BackToMealPlan));

        firebase = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users/caretakers");

        Intent intent = getIntent();
        caregiverUUID = intent.getStringExtra("caregiverUUID");
        caretakerUUID = intent.getStringExtra("caretakerUUID");

        //Set patient preferences by grabbing from DB via UUID
        if (caretakerUUID != null) {
            dbRef.child(caretakerUUID).child("prefFood").get().addOnCompleteListener(task ->
                    txt_PatientPreferences.setText(Objects.requireNonNull(task.getResult().getValue()).toString())
            );
        }

        // TODO(Emarioo): Now we have the uuid for the caretaker. Next thing would be to ask the
        //  database for some information about the caretaker (patient).
    }
}