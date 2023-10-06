package com.example.simplelistviewdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

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
            }
        }.start();
    }

}
