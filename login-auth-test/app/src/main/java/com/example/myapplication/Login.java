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
import android.content.SharedPreferences;
import android.content.Context;

public class Login extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextPIN;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textViewLogin, textViewReset;
    dbLibrary db;
    RadioGroup radioGroup;
    private static final String PREF_NAME = "MyAppPreferences";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_PIN = "pin";

    @Override
    public void onStart() {
        super.onStart();
        // Check if the user is already signed in and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Handle the case when a user is already signed in
            // You can add your logic here to navigate to the appropriate screen
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextPIN = findViewById(R.id.editTextPin);
        buttonLogin = findViewById(R.id.loginButton);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.registerNow);
        textViewReset = findViewById(R.id.forgotPasswordBtn);
        db = new dbLibrary(Login.this);
        radioGroup = findViewById(R.id.radioGroupLogin);

        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonPin) {
                    editTextPassword.setVisibility(View.GONE);
                    editTextPIN.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.radioButtonPassword) {
                    editTextPassword.setVisibility(View.VISIBLE);
                    editTextPIN.setVisibility(View.GONE);
                }
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, pin;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                pin = String.valueOf(editTextPIN.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonPin) {
                    if (TextUtils.isEmpty(pin)) {
                        Toast.makeText(Login.this, "Please enter your PIN", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    // Retrieve the saved email and password from SharedPreferences
                    SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
                    String savedEmail = sharedPref.getString(PREF_EMAIL, "");
                    String savedPassword = sharedPref.getString(PREF_PASSWORD, "");
                    String savedPin = sharedPref.getString(PREF_PIN, ""); // Retrieve the saved PIN

                    Log.d("test", "savedPin: " + savedPin);
                    Log.d("test", "savedEmail: " + savedEmail);
                    Log.d("test", "savedPassword: " + savedPassword);
                    if (pin.equals(savedPin)) {
                        // PIN is correct, proceed with login
                        mAuth.signInWithEmailAndPassword(savedEmail, savedPassword)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        progressBar.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            // Successfully logged in
                                            Intent intent = new Intent(getApplicationContext(), home_caregiver.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        // Incorrect PIN
                        Toast.makeText(Login.this, "Invalid PIN", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    // "Use Password" radio button is selected, proceed with email and password login
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Successfully logged in
                                        Intent intent = new Intent(getApplicationContext(), home_caregiver.class);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        textViewReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(intent);
            }
        });
    }
}
