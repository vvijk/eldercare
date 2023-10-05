package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    Button button;
    Button addCaretakerButton;
    TextView textView;
    FirebaseUser user;
    TextInputEditText addCaretakerInputText;
    dbLibrary db;
    
    Button btn_mealManagement = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.logout);
        textView = findViewById(R.id.user_details);
        user = auth.getCurrentUser();
        
        btn_mealManagement = findViewById(R.id.btn_goto_meal_management);
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


        /*createPatient.setOnClickListener(new View.OnClickListener(){ //PLACEHOLDER
           @Override
           public void onClick(View view) {
               Intent intent = new Intent(getApplicationContext(), CreatePatient.class);
               startActivity(intent);
            }
        });*/

        btn_mealManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MealManagementActivity.class);
                startActivity(intent);
            }
        });

        addCaretakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String caretakerFromInput = String.valueOf(addCaretakerInputText.getText());
                if (TextUtils.isEmpty(caretakerFromInput) || !android.util.Patterns.EMAIL_ADDRESS.matcher(caretakerFromInput).matches()) {
                    Toast.makeText(MainActivity.this, "Ange epost i rätt format: test@test.com", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.getCaretakerUidByEmail(caretakerFromInput, new dbLibrary.UserUidCallback() {
                    @Override
                    public void onUserUidFound(String uid) {
                        db.addCaretakerToGiver(db.getUserID(), uid, new dbLibrary.CaretakerAddCallback() {
                            @Override
                            public void onCaretakerAdded(String message) {
                                Toast.makeText(MainActivity.this, "Användare: " + caretakerFromInput + " har lagts till i din patientlista!", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onCaretakerAddError(String errorMessage) {
                                Toast.makeText(MainActivity.this, "Användare: " + caretakerFromInput + " finns redan i din patientlista!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onUserUidNotFound() {
                        // Handle the case where no user with the specified email was found
                        Toast.makeText(MainActivity.this, "Användare: " + caretakerFromInput + " hittades inte bland vårdtagare!", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onUserUidError(String errorMessage) {
                        Toast.makeText(MainActivity.this, "ERROR, kolla onUserUidError()..", Toast.LENGTH_SHORT).show();
                        // Handle the error
                    }
                });
            }
        });

    }
}