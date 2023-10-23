package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlarmActivity extends AppCompatActivity {

    DatabaseReference larmRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUser = auth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        larmRef = database.getReference("users/caretakers/" + currentUser + "/larm");
        TextView countDownTextView = findViewById(R.id.CountDown);

        Button buttonGoBack = findViewById(R.id.ButtonGoBack);
        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        startCountDownTimer(countDownTextView);
    }

    private void startCountDownTimer(TextView countDownTextView) {
        TextView TextViewTimeLeft = findViewById(R.id.TextViewTimeLeft);
        Button ButtonGoBack = findViewById(R.id.ButtonGoBack);

        CountDownTimer countDownTimer = new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long l) {
                countDownTextView.setText(String.valueOf(l / 1000));
            }

            @Override
            public void onFinish() {

                countDownTextView.setText("Larmet har aktiverats");
                TextViewTimeLeft.setText("");
                ButtonGoBack.setText("Tillbaka");
                larmRef.setValue(true);
            }
        }.start();
    }

}
