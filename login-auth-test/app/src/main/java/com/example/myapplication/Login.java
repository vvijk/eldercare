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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.SharedPreferences;
import android.content.Context;

public class Login extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextPIN;
    Button buttonLogin, settings_btn;
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
        // switchActivity(); // uncomment to use automatic login
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

        settings_btn = findViewById(R.id.settings_btn);
        settings_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Settings.class);
                intent.putExtra("previousActivityClass",Login.class.getName());
                startActivity(intent);
                // finish();
            }
        });

        // default visibility
        editTextPassword.setVisibility(View.GONE);
        editTextPIN.setVisibility(View.VISIBLE);
        radioGroup.check(R.id.radioButtonPin);

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

                // if (TextUtils.isEmpty(email)) {
                //     Toast.makeText(Login.this, getResources().getString(R.string.provide_email), Toast.LENGTH_SHORT).show();
                //     progressBar.setVisibility(View.GONE);
                //     return;
                // }

                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButtonPin) {
                    if (TextUtils.isEmpty(pin)) {
                        Toast.makeText(Login.this, getResources().getString(R.string.provide_pin), Toast.LENGTH_SHORT).show();
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
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            if(user == null) {
                                                Toast.makeText(getApplicationContext(),getString(R.string.invalid_user_login),Toast.LENGTH_LONG).show();
                                                Log.d("dbtest", "Is neither a caretake nor caregiver");
                                            }
                                            startHomeActivity();
                                        } else {
                                            Toast.makeText(Login.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        // Incorrect PIN
                        Toast.makeText(Login.this, getString(R.string.invalid_pin), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                } else {
                    if (TextUtils.isEmpty(email)) {
                        Toast.makeText(Login.this, getResources().getString(R.string.provide_email), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    if (TextUtils.isEmpty(password)) {
                        Toast.makeText(Login.this, getResources().getString(R.string.provide_password), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }
                    // "Use Password" radio button is selected, proceed with email and password login
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Successfully logged in
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        if(user == null) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_user_login),Toast.LENGTH_LONG).show();
                                            Log.d("dbtest", "Is neither a caretake nor caregiver");
                                        } else {
                                            // NOTE(Emarioo): The
                                            FirebaseDatabase db = FirebaseDatabase.getInstance();
                                            db.getReference().child("users/caretakers").child(user.getUid()).child("pin").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    System.out.println("Changed!");
                                                    Object pinString = snapshot.getValue();
                                                    if(pinString instanceof String) {
                                                        saveData(email, password, (String)pinString);
                                                    }
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {
                                                    System.out.println("Cancelled oh no");
                                                    saveData(email, password, null);
                                                }
                                            });
                                        }
                                        startHomeActivity();
                                    } else {
                                        Toast.makeText(Login.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
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
    void saveData(String email, String password, String pinCode) {
        SharedPreferences sharedPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(email != null)
            editor.putString(PREF_EMAIL, email);
        if(password != null)
            editor.putString(PREF_PASSWORD, password);
        if(pinCode != null)
            editor.putString(PREF_PIN, pinCode);
        editor.apply();
    }
    void startHomeActivity() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
            return;
        db.isCaregiver(currentUser.getUid(), new dbLibrary.CaregiverCheckCallback() {
            @Override
            public void onFound(boolean isCaregiver) {
                // Sign in success, update UI with the signed-in user's information
                Intent intent;
                if(isCaregiver){
                    // NOTE(Emarioo): I commented this out because I assume it's for debug purposes. If not, then we can't use hardcoded strings like this. It must be translated!
                    // Toast.makeText(getApplicationContext(), "Successful pre.login as: caregiver!", Toast.LENGTH_SHORT).show();
                    intent = new Intent(getApplicationContext(), home_caregiver.class);
                }else{
                    // Toast.makeText(getApplicationContext(), "Successful pre-login as: caretaker!", Toast.LENGTH_SHORT).show();
                    intent = new Intent(getApplicationContext(), RecipientHome.class);
                }
                startActivity(intent);
                finish();
            }
            @Override
            public void onNotFound() {
                Log.d("dbtest", "Is neither a caretake nor caregiver");
            }
            @Override
            public void onFoundError(String errorMessage) {
                Log.d("dbtest", "Database ERROR!!");
            }
        });   
    }
}
