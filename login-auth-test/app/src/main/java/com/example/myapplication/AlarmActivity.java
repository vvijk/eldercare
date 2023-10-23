package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
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

public class AlarmActivity extends AppCompatActivity {


    DatabaseReference caretakersRef;
    String recipientUID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        recipientUID = getIntent().getStringExtra("recipientUID");

        TextView countDownTextView = findViewById(R.id.CountDown);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        caretakersRef = database.getReference("users/caretakers");

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

                caretakersRef.child(recipientUID).child("larm").setValue(true).addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        // TODO: Om en vårdtagare har larmat och databasen misslyckas här kan det gå riktigt illa för vårdtagaren.
                        countDownTextView.setText("Databasen är nere!");
                    }
                });
            }
        }.start();
    }

}
