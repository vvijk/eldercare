package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.util.GlobalApp;
import com.example.myapplication.util.PatientMealStorage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;


public class PatientProfile extends AppCompatActivity {
    TextView txt_PatientName;
    TextView txt_PatientAddress;
    TextView txt_PatientPreferences;
    TextView txt_PatientAlerts;
    Button btn_BackToMealPlan;
    ImageView img_PatientAvatar;
    String caregiverUUID = "";
    FirebaseAuth firebase;
    DatabaseReference dbRef;
    int currentCaregiverId = 1; // TODO(Emarioo): Should come from somewhere else.
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
        btn_BackToMealPlan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MealManagementActivity.class);
                //intent.putExtra("caretakerId", patientId);
                //intent.putExtra("caregiverId", currentCaregiverId); //TODO(Johan): Think this is needed for meal management to work correctly
                startActivity(intent);
            }
        });
        btn_BackToMealPlan.setText(getString(R.string.str_BackToMealPlan));

        firebase = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");

        FirebaseFirestore store = FirebaseFirestore.getInstance();



    }
}