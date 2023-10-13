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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonLogin;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textViewLogin, textViewReset;
    dbLibrary db;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            db.isCaregiver(currentUser.getUid(), new dbLibrary.CaregiverCheckCallback() {
                @Override
                public void onFound(boolean isCaregiver) {
                    // Sign in success, update UI with the signed-in user's information
                    Intent intent;
                    if(isCaregiver){
                        Toast.makeText(getApplicationContext(), "Successful pre.login as: caregiver!", Toast.LENGTH_SHORT).show();
                        intent = new Intent(getApplicationContext(), home_caregiver.class);
                    }else{
                        Toast.makeText(getApplicationContext(), "Successful pre-login as: caretaker!", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Set the content view to the login layout
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonLogin = findViewById(R.id.loginButton);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        textViewLogin = findViewById(R.id.registerNow);
        textViewReset = findViewById(R.id.forgotPasswordBtn);
        db = new dbLibrary(Login.this);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Register.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                FirebaseUser user = mAuth.getCurrentUser();
                                if(user == null) {
                                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.invalid_user_login),Toast.LENGTH_LONG).show();
                                    Log.d("dbtest", "Is neither a caretake nor caregiver");
                                } else {
                                    db.isCaregiver(user.getUid(), new dbLibrary.CaregiverCheckCallback() {
                                        @Override
                                        public void onFound(boolean isCaregiver) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Intent intent;
                                                if (isCaregiver) {
                                                    Toast.makeText(getApplicationContext(), "Successful login as: caregiver!", Toast.LENGTH_SHORT).show();
                                                    intent = new Intent(getApplicationContext(), home_caregiver.class);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Successful login as: caretaker!", Toast.LENGTH_SHORT).show();
                                                    intent = new Intent(getApplicationContext(), RecipientHome.class);
                                                }
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                // If sign-in fails, display a message to the user.
                                                Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onNotFound() {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show();
                                            Log.d("dbtest", "Is neither a caretake nor caregiver");
                                        }

                                        @Override
                                        public void onFoundError(String errorMessage) {
                                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show();
                                            Log.d("dbtest", "Database ERROR!!");
                                        }
                                    });
                                }
                            }
                        });
            }
        });

        textViewReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
                startActivity(intent);
                finish();
            }
        });
    }

}