package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class Settings extends AppCompatActivity {

    Button englishBtn, swedishBtn;

    String previousActivityClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        previousActivityClass = getIntent().getStringExtra("previousActivityClass");

        englishBtn = findViewById(R.id.engBtn);
        swedishBtn = findViewById(R.id.sweBtn);

        swedishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("sv");

            }
        });

        englishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                setLocale("en");

            }
        });

    }

    private void setLocale(String languageCode) {
        // Change the app's language based on the language code
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration();
        configuration.locale = locale;
        Resources resources = getResources();
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        // Restart the activity to apply the language change
        recreate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = null;
                try {
                    intent = new Intent(getApplicationContext(), Class.forName(previousActivityClass));
                } catch(ClassNotFoundException e) {

                }
                if(intent == null) {
                    intent = new Intent(getApplicationContext(), home_caregiver.class);
                }
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}