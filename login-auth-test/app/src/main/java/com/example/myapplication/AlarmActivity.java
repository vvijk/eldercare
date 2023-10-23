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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.util.MealStorage;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AlarmActivity extends AppCompatActivity {


    DatabaseReference caretakersRef;
    String recipientUID;

    boolean isAlarmActive;
    
    static final int SECONDS_UNTIL_ALARM=10;

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        
        FirebaseAuth auth = FirebaseAuth.getInstance();
        recipientUID = auth.getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        caretakersRef = database.getReference("users/caretakers");
        TextView countDownTextView = findViewById(R.id.CountDown);

        caretakersRef.child(recipientUID).child("larm").addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isActive = false;
                if(snapshot.getValue() != null)
                    isActive = (Boolean)snapshot.getValue();
                isAlarmActive = isActive;
                refreshUI(isActive);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Button buttonGoBack = findViewById(R.id.ButtonGoBack);
        buttonGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                caretakersRef.child(recipientUID).child("larm").setValue(false);

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        startCountDownTimer(countDownTextView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void startCountDownTimer(TextView countDownTextView) {
        TextView TextViewTimeLeft = findViewById(R.id.TextViewTimeLeft);
        Button ButtonGoBack = findViewById(R.id.ButtonGoBack);

        if(countDownTimer != null)
            countDownTimer.cancel();
        countDownTimer = new CountDownTimer(SECONDS_UNTIL_ALARM, 1000) {
            @Override
            public void onTick(long l) {
                if(!isAlarmActive) {
                    countDownTextView.setText(String.valueOf(l / 1000));
                }
            }

            @Override
            public void onFinish() {
                refreshUI(true);

                caretakersRef.child(recipientUID).child("larm").setValue(true).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        // TODO: Om en vårdtagare har larmat och databasen misslyckas här kan det gå riktigt illa för vårdtagaren.
                        countDownTextView.setText(getResources().getString(R.string.database_down));
                    }
                });
            }
        }.start();
    }
    void refreshUI(boolean alarmIsActive) {
        TextView TextViewTimeLeft = findViewById(R.id.TextViewTimeLeft);
        Button ButtonGoBack = findViewById(R.id.ButtonGoBack);
        TextView countDownTextView = findViewById(R.id.CountDown);

        if(alarmIsActive) {
            countDownTextView.setText(getResources().getString(R.string.alarm_is_active));
            TextViewTimeLeft.setText("");
            ButtonGoBack.setText(getResources().getString(R.string.alarm_turn_off_go_back));
        } else {
            countDownTextView.setText("");
            TextViewTimeLeft.setText(getResources().getString(R.string.alarm_time_until_active));
            ButtonGoBack.setText(getResources().getString(R.string.alarm_regret_go_back));
        }
    }
}
