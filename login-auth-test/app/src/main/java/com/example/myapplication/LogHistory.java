package com.example.myapplication;

import static com.example.myapplication.Helpers.isStringInArray;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


/* TODO:
 *   Add logs to patients at different parts of the app.
 *   .
 *   Implement patient spinner, and filtering by patient.
 *   Possibly add a sort by timestamp
 *   .
 *   Thoroughly test when all is implemented.
 * */


public class LogHistory extends AppCompatActivity {
    //Page for viewing Logs.

    DatabaseReference dbRef;
    FirebaseAuth mAuth;


    Context context;
    Spinner categorySpinner, patientSpinner;
    Button refreshLog;
    LinearLayout logTextBox;
    ArrayList<DropdownItem> categoryDDIs, patientDDIs;
    ArrayList<LogItem> logs;

    CareGiver caregiver;
    ArrayList<CareTaker> patients;

    DropdownAdapter viewCategory;
    DropdownAdapter viewPatients;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("users");


        setContentView(R.layout.activity_log_history);
        categorySpinner = findViewById(R.id.logCategorySpinner);

        patientSpinner = findViewById(R.id.logPatientSpinner);
        refreshLog = findViewById(R.id.refreshButton);

        logTextBox = findViewById(R.id.logItemBox);

        initData();
        initUI();

        populateCategorySpinner(categorySpinner);




        setLogTextBox(logs);

        refreshLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { setLogTextBox(logs); Log.d("LOGHISTORY", patients.get(0).getFullName()); }
        });
    }

    private void initData(){
        //https://stackoverflow.com/questions/4685563/how-to-pass-a-function-as-a-parameter-in-java
        CaregiverCallback cgcb = (cb1) -> {
            this.caregiver = cb1;
            fetchPatients();
        };
        fetchCaregiver(cgcb);
        //fetchLogs();
    }
    private void initUI(){

    }

    private interface CaregiverCallback{
        void setCareGiver(CareGiver caregiver);
    }
    private interface CaretakerCallback{
        void setCareTaker(CareTaker caretaker);
    }
    public String getUserID(){
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }
    public void fetchCaregiver(CaregiverCallback cb){
        DatabaseReference uidRef = dbRef.child("caregivers").child(this.getUserID());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CareGiver caregiver = dataSnapshot.getValue(CareGiver.class);
                cb.setCareGiver(caregiver);
                Log.d("TAG", caregiver.getFirstName());

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", databaseError.getMessage()); //TODO: Don't ignore errors!
            }
        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }

    public void fetchCaretaker(String uid, CaretakerCallback cb){
        DatabaseReference uidRef = dbRef.child("caretakers").child(uid);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CareTaker caretaker = dataSnapshot.getValue(CareTaker.class);
                cb.setCareTaker(caretaker);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("TAG", databaseError.getMessage()); //TODO: Don't ignore errors!
            }
        };
        uidRef.addListenerForSingleValueEvent(valueEventListener);
    }
    private void populateCategorySpinner(Spinner toPopulate){

        final String[] spinner_texts = getResources().getStringArray(R.array.log_category_array);

        categoryDDIs = new ArrayList<>(); //overwrites the old dropdown items if called multiple times.

        for (int i = 0; i < spinner_texts.length; i++){
            DropdownItem DDI = new DropdownItem();
            DDI.setText(spinner_texts[i]);
            DDI.setSelected(true);
            categoryDDIs.add(DDI);
        }
        DropdownAdapter viewCategory = new DropdownAdapter(context, 0, categoryDDIs);
        toPopulate.setAdapter(viewCategory);
    }

    private void populatePatientSpinner(Spinner toPopulate){
        ArrayList<String> patient_texts = new ArrayList<>();
        patient_texts.add(getString(R.string.log_patient_array_title));
        for (int i = 0; i < patients.size(); i++){
            patient_texts.add(patients.get(i).getFullName());
        }
        patientDDIs = new ArrayList<>();
        for (int i = 0; i < patient_texts.size(); i++){
            DropdownItem DDI = new DropdownItem();
            DDI.setText(patient_texts.get(i));
            DDI.setSelected(true);
            patientDDIs.add(DDI);
        }
        DropdownAdapter viewPatients = new DropdownAdapter(context,0, patientDDIs);
        toPopulate.setAdapter(viewPatients);
    }

    private void fetchPatients(){
        //fetches all patients that the signed in caregiver has. used for populating the patient spinner and fetching logs.
        List<String> caretakerIDs = new ArrayList<String>(this.caregiver.getCaretakers().keySet());
        this.patients = new ArrayList<CareTaker>();
        if (!caretakerIDs.isEmpty()) {
            for (int i = 0; i < caretakerIDs.size(); i++){
                CaretakerCallback ctcb = (cb2) -> {
                    this.patients.add(cb2);
                    populatePatientSpinner(patientSpinner);
                };
                fetchCaretaker(caretakerIDs.get(i), ctcb);
            }
        }
    }

    private void fetchLogs(){
        this.logs = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++){
            ArrayList<LogItem> patientLogs = patients.get(i).getLogs();
            this.logs.addAll(patientLogs);
        }
    }
    private void sortLogsByTimestamp(){
        //TODO: implementera
    }

    private String generateLogMessage(LogItem logItem) {
        String out = logItem.getFormattedTimestamp() + " ";
        switch (logItem.getCategory()){
            case LogItem.PATIENT_ADD:{
                out += logItem.caregiver.getFullName() + " ";
                out += getString(R.string.LOG_ADD_PATIENT_S1) + " ";
                out += logItem.patient.getFullName();
                break;
            }
            case LogItem.EMERGENCY:{
                out += logItem.caregiver.getFullName() + " ";
                out += getString(R.string.LOG_EMERGENCY_S1);
                break;
            }
            case LogItem.MEAL_CONFIRM:{
                out += logItem.patient.getFullName() + " ";
                out += getString(R.string.LOG_MEAL_CONFIRM_S1) + " ";
                out += logItem.getMeal().name + " ";
                out += getString(R.string.LOG_MEAL_CONFIRM_S2);
                break;
            }
            case LogItem.MEAL_SKIP:{
                //TODO: [TimeStamp] [CareTaker] missade att bekräfta [första/andra] gången.
                //TODO: väv in vilken måltid som missades
                break;
            }
            case LogItem.MEAL_MISS:{
                //TODO: väv in vilken måltid som missades
                out += logItem.patient.getFullName() + " ";
                out += getString(R.string.LOG_MEAL_MISS_S1);
                break;
            }
        }
        out += ".";
        return out;
    }

    private ArrayList<LogItem> filterLogs(ArrayList<LogItem> logs){

        ArrayList<String> checkedCategories = mapCategoryCheckboxes();
        ArrayList<LogItem> out = new ArrayList<>();
        for (int i = 0; i < logs.size(); i++){
            if (isStringInArray(logs.get(i).getCategory(), checkedCategories)){ out.add(logs.get(i)); }
        }
        return out;
    }

    private ArrayList<String> mapCategoryCheckboxes(){
        //TODO: tacky implementation, refer to log_history.xml when updating switch statement.
        //Returns the constant codes of checked checkboxes.
        ArrayList<String> out = new ArrayList<>();

        for (int i = 0; i < categoryDDIs.size(); i++){
            if (!categoryDDIs.get(i).isSelected()){ continue; }
            switch (i+1){
                case 1: break;
                case 2: out.add("ALL"); break;
                case 3: out.add("PATIENT_ADD"); break;
                case 4: out.add("MEAL_CONFIRM"); break;
                case 5: out.add("MEAL_MISS"); out.add("MEAL_SKIP"); break;
                case 6: out.add("EMERGENCY"); break;
            }
        }
        return out;
    }



    private void setLogTextBox(ArrayList<LogItem> logItems){
        TextView log;
        logItems = filterLogs(logItems);
        clearTextField();
        for (int i = 0; i < logItems.size(); i++){
            log = createTextView(logItems.get(i));
            logTextBox.addView(log);
        }
    }
    private TextView createTextView(LogItem log){
        TextView text = new TextView(context);
        text.setText(generateLogMessage(log));
        text.setTextColor(getLogColor(log));
        text.setPadding(10, 0, 10, 0);
        return text;
    }

    private void clearTextField(){
        logTextBox.removeAllViews();
    }

    private int getLogColor(LogItem log){
        //maps log color to resource file.
        switch (log.getCategory()){
            case "PATIENT_ADD":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return getColor(R.color.PATIENT_ADD);
                }
            case "EMERGENCY":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return getColor(R.color.EMERGENCY);
                }
                break;
            case "MEAL_CONFIRM":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return getColor(R.color.MEAL_CONFIRM);
                }
            case "MEAL_SKIP":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return getColor(R.color.MEAL_SKIP);
                }
            case "MEAL_MISS":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return getColor(R.color.MEAL_MISS);
                }
        }
        return 0;
    }

}
