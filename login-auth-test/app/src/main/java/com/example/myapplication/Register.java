package com.example.myapplication;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.ArchTaskExecutor;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword, editTextName, editTextLastname, editTextPhoneNr,editTextPassword2;
    Button registerBtn;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView loginBtn;
    //FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference;


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
        editTextName = findViewById(R.id.fornamn);
        editTextLastname = findViewById(R.id.efternamn);
        editTextPhoneNr = findViewById(R.id.tlfnummer);

        registerBtn = findViewById(R.id.registerButton);
        mAuth = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.progressBar);
        loginBtn = findViewById(R.id.loginNow);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email, password, password2, name, lastname, phoneNr;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());
                password2 = String.valueOf(editTextPassword2.getText());
                name = String.valueOf(editTextName.getText());
                lastname = String.valueOf(editTextLastname.getText());
                phoneNr = String.valueOf(editTextPhoneNr.getText());

                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Ange epostadress", Toast.LENGTH_SHORT).show();
                    return;
                } else if(TextUtils.isEmpty(password)){
                    Toast.makeText(Register.this, "Ange lösenord", Toast.LENGTH_SHORT).show();
                    return;
                } else if(!password.equals(password2)){
                    Toast.makeText(Register.this, "Lösenorden matchar inte!", Toast.LENGTH_SHORT).show();
                    return;
                } else if(TextUtils.isEmpty(name)){
                    Toast.makeText(Register.this, "Ange förnamn", Toast.LENGTH_SHORT).show();
                    return;
                } else if(TextUtils.isEmpty(lastname)){
                    Toast.makeText(Register.this, "Ange efternamn", Toast.LENGTH_SHORT).show();
                    return;
                } else if(TextUtils.isEmpty(phoneNr) || !TextUtils.isDigitsOnly(phoneNr)){
                    Toast.makeText(Register.this, "Ange telefonnummer", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    // User registration successful
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    // Create a User object to store additional information
                                    User newUser = new User(name, lastname, phoneNr, email);
                                    // Store user information in the Firebase Realtime Database
                                    databaseReference.child(user.getUid()).setValue(newUser);
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(Register.this, "Konto skapat!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(Register.this, "Autentiseringen misslyckades.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

    }
}