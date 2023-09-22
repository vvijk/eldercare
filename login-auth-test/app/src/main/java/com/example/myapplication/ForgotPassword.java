package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    TextView textViewForgotPassword, textViewLoginNow;
    TextInputEditText editTextEmail;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        textViewForgotPassword = findViewById(R.id.forgotPasswordBtn);
        editTextEmail = findViewById(R.id.email);
        textViewLoginNow = findViewById(R.id.loginNow);
        mAuth = FirebaseAuth.getInstance();

        textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = String.valueOf(editTextEmail.getText());
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(ForgotPassword.this, "Ange epostadress", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "Återställninged lyckades!", Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }else{
                                Toast.makeText(getApplicationContext(), "Återställninged lyckades EJ!!!!!!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    });
                }

            }
        });


        textViewLoginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

    }


}
