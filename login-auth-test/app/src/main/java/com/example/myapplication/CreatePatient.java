package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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

    private void showErrorBox(String errorMessage){

    }

    private void createPatient(){
        //TODO Send data from all fields to database, when schema is implemented

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void validate(){
        boolean error = false;
        String errorMessage = "@string/createPatientErrorHeader";
        if (!validateForename()){
            errorMessage = errorMessage.concat("@string/createPatientErrorForename");
            error = true;
        } if (!validateSurname()){
            errorMessage = errorMessage.concat("@string/createPatientErrorSurname");
            error = true;
        } if (!validatePhone()){
            errorMessage = errorMessage.concat("@string/createPatientErrorPhone");
            error = true;
        } if (!validateAddress()){
            errorMessage = errorMessage.concat("@string/createPatientErrorAddress");
            error = true;
        } if (!validateCity()){
            errorMessage = errorMessage.concat("@string/createPatientErrorCity");
            error = true;
        } if (!validateCounty()){
            errorMessage = errorMessage.concat("@string/createPatientErrorCounty");
            error = true;
        } if (validateCounty()){
            entryPreferences.setText("Inga preferenser.");
        } if (validateCounty()){
            entryPreferences.setText("Inga allergier.");
        } if (!validatePIN()){
            errorMessage = errorMessage.concat("@string/createPatientErrorPIN");
            error = true;
        } if (!validateConfirmPIN()){
            errorMessage = errorMessage.concat("@string/createPatientErrorConfirmPIN");
            error = true;
        }

        if (error) { showErrorBox(errorMessage); }
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
        return entryPIN.getText() == entryConfirmPIN.getText();
    }

}
