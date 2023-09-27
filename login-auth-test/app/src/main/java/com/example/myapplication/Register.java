package com.example.myapplication;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail,
            editTextPassword,
            editTextName,
            editTextLastname,
            editTextPhoneNr,
            editTextPassword2,
            editTextPersonNummer,
            test,
            pin;
    Button registerBtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView loginBtn;
    RadioGroup radioGroup;
    boolean isCareGiver;
    int checkedRadioButtonId;
    dbLibrary db;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent =  new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextPassword2 = findViewById(R.id.password2);
        editTextName = findViewById(R.id.firstName);
        editTextLastname = findViewById(R.id.lastName);
        editTextPhoneNr = findViewById(R.id.phoneNumber);
        editTextPersonNummer = findViewById(R.id.idNumber);

        test = findViewById(R.id.test);
        pin = findViewById(R.id.pin);

        registerBtn = findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        loginBtn = findViewById(R.id.loginNow);
        radioGroup = findViewById(R.id.radioGroup);
        db = new dbLibrary(Register.this);
        checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        isCareGiver = checkedRadioButtonId == R.id.careGiverID;

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.careGiverID) {
                    test.setVisibility(View.VISIBLE);
                    pin.setVisibility(View.VISIBLE);
                } else {
                    test.setVisibility(View.GONE);
                    pin.setVisibility(View.GONE);
                }
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, password2, name, lastname, phoneNr, personNummer;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                password2 = String.valueOf(editTextPassword2.getText());
                name = String.valueOf(editTextName.getText());
                lastname = String.valueOf(editTextLastname.getText());
                phoneNr = String.valueOf(editTextPhoneNr.getText());
                personNummer = String.valueOf(editTextPersonNummer.getText());

                if (checkedRadioButtonId == -1) {
                    Toast.makeText(Register.this, "Du måste välja vårdtagare eller vårdgivare!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!password.equals(password2)) {
                    Toast.makeText(Register.this, "Lösenorden matchar inte!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                db.registerUser(email, password, name, lastname, phoneNr, personNummer, isCareGiver, new dbLibrary.RegisterCallback() {
                    @Override
                    public void onSuccess(String message) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();
                        // Skicka användaren till MainAcitivity sidan.
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    @Override
                    public void onError(String errorMessage) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Register.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

    }
}