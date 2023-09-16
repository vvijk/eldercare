package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.myapplication.util.PatientMealStorage;
import com.google.firebase.auth.FirebaseUser;

public class PatientMealActivity extends AppCompatActivity implements View.OnClickListener {
    LinearLayout scrolledLayout=null;
    TextView patientName=null;
    Button btn_back=null;

    PatientMealStorage mealStorage = new PatientMealStorage();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_patient);
        scrolledLayout = findViewById(R.id.week_scroll);
        patientName = findViewById(R.id.text_patient_name);
        btn_back = findViewById(R.id.manage_patient_back);
        btn_back.setOnClickListener(this);

        Intent intent = getIntent();
        int patientId = intent.getIntExtra("patientId", 0);
        if(patientId == 0) {
            // invalid patient
        } else {
            String name = mealStorage.nameOfPatient(patientId);
            if (name != null) {
                patientName.setText(name);
            } else {
                // error?
                patientName.setText("null");
            }
        }

    }

    @Override
    public void onClick(View view) {
        Button button = (Button)view;

        if(button == btn_back) {
            System.out.println("BACK");
            finish();
        }
    }
}