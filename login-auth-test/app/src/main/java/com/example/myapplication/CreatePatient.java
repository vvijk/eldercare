package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
public class CreatePatient extends AppCompatActivity {

    TextInputEditText entryForename, entrySurname, entryDOB, entryPhone, entryAddress, entryCity, entryCounty, entryPreferences, entryRestrictions, entryPIN, entryConfirmPIN;
    Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_patient);

        entryForename = findViewById(R.id.createPatient_firstName);
        entrySurname = findViewById(R.id.createPatient_surname);
        //entryDOB = findViewById(R.id.createPatient_DOB);   TODO: implement later, when gui works for it.
        entryPhone = findViewById(R.id.createPatient_telephoneNumber);
        entryAddress = findViewById(R.id.createPatient_address);
        entryCity = findViewById(R.id.createPatient_city);
        entryCounty = findViewById(R.id.createPatient_county);
        entryPreferences = findViewById(R.id.createPatient_preferences);
        entryRestrictions = findViewById(R.id.createPatient_restrictions);
        entryPIN = findViewById(R.id.createPatient_pin);
        entryConfirmPIN = findViewById(R.id.createPatient_confirmPin);
        createButton = findViewById(R.id.createPatient_button);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
    }

    private void createPatient(){
        //TODO Send data from all fields to database, when schema is implemented

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        Toast.makeText(this, getString(R.string.createPatientSuccess), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void validate(){
        boolean error = false;
        int errorMessage = R.string.createPatientErrorDefault;
        if (!validateForename()){
            errorMessage = R.string.createPatientErrorForename;
            error = true;
        } else if (!validateSurname()){
            errorMessage = R.string.createPatientErrorSurname;
            error = true;
        } else if (!validatePhone()){
            errorMessage = R.string.createPatientErrorPhone;
            error = true;
        } else if (!validateAddress()){
            errorMessage = R.string.createPatientErrorAddress;
            error = true;
        } else if (!validateCity()){
            errorMessage = R.string.createPatientErrorCity;
            error = true;
        } else if (!validateCounty()){
            errorMessage = R.string.createPatientErrorCounty;
            error = true;
        } else if (!validatePIN()){
            errorMessage = R.string.createPatientErrorPIN;
            error = true;
        } else if (!validateConfirmPIN()){
            errorMessage = R.string.createPatientErrorConfirmPIN;
            error = true;
        }

        if (error) {
            Toast.makeText(this, getString(errorMessage), Toast.LENGTH_SHORT).show();
        }
        else { createPatient(); }
    }

    //many of these function similarly, but we now have structure in place if we need to modify these validations later.
    private boolean validateForename(){
        return !String.valueOf(entryForename.getText()).isEmpty();
    }
    private boolean validateSurname(){
        return !String.valueOf(entrySurname.getText()).isEmpty();
    }

/*TODO: add validation for DOB, also add DOB to gui.
    private boolean validateDOB(String str){
        return true;
    }
*/
    private boolean validatePhone(){
        String str = String.valueOf(entryPhone.getText());
        return str.matches("[0-9]+") && str.length() == 10;
    }
    private boolean validateAddress(){
        return !String.valueOf(entryAddress.getText()).isEmpty();
    }
    private boolean validateCity(){
        return !String.valueOf(entryCity.getText()).isEmpty();
    }
    private boolean validateCounty(){
        return !String.valueOf(entryCounty.getText()).isEmpty();
    }
    private boolean validatePreferences(){
        return !String.valueOf(entryPreferences.getText()).isEmpty();
    }
    private boolean validateRestrictions(){
        return !String.valueOf(entryRestrictions.getText()).isEmpty();
    }
    private boolean validatePIN(){
        String str = String.valueOf(entryPIN.getText());
        return str.matches("[0-9]+") && str.length() == 4;

    }
    private boolean validateConfirmPIN(){
        System.out.print(entryPIN.getText());
        System.out.print(entryConfirmPIN.getText());
        if (!validatePIN()){ return true; } //if PIN already fails to validate, we don't need to send error for confirm pin.
        else { return (String.valueOf(entryPIN.getText()).equals(String.valueOf(entryConfirmPIN.getText()))); }
    }
}
