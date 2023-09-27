package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    Button createPatient, addCaretakerButton; //PLACEHOLDER
    TextView textView;
    FirebaseUser user;
    TextInputEditText addCaretakerInputText;
    dbLibrary db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        createPatient = findViewById(R.id.createpatient); //PLACEHOLDER
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        addCaretakerButton = findViewById(R.id.addCaretakerToGiver);
        addCaretakerInputText = findViewById(R.id.addCaretakerTextView);
        db = new dbLibrary(MainActivity.this);

        if(user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }else{
            textView.setText(user.getEmail());
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        createPatient.setOnClickListener(new View.OnClickListener(){ //PLACEHOLDER
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(getApplicationContext(), CreatePatient.class);
               startActivity(intent);
            }
        });
        addCaretakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String msg = String.valueOf(addCaretakerInputText.getText());
                //Log.d("MainActivity", msg);

            }
        });

    }
}