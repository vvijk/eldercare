package com.example.myapplication;

import static com.example.myapplication.Helpers.isStringInArray;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


/* TODO:
*   Tie everything in with the database
*       Add a fetchLogs(int amount){} function
*           Fetches X amount of logs from each patient that the signed in caregiver has.
*       Add a "log" field to each patient
*         - Timestamp
*         - Caregiver
*         - Patient
*         - Category
*         - Meal
*   Implement patient spinner, and filtering by patient.
*   Polish checkbox behavior
*       When all is checked, check all other checkboxes.
*       When another checkbox is unchecked, uncheck all.
*       Auto-refresh log when checkbox updates (Probably impossible to do without comprehensive refactor.)
*   Add more log messages
*       Consult with group for suggestions on these.
*   Possibly add a sort by timestamp
*
*
*   TODO: Thoroughly test when all is implemented.
* */


public class LogHistory extends AppCompatActivity {
    //Page for viewing Logs.
    Context context;
    Spinner categorySpinner, patientSpinner;
    Button refreshLog;
    LinearLayout logTextBox;
    ArrayList<DropdownItem> categoryDDIs, patientDDIs;
    ArrayList<LogItem> logs;

    DropdownAdapter viewCategory;
    DropdownAdapter viewPatients;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();

        setContentView(R.layout.activity_log_history);
        categorySpinner = findViewById(R.id.logCategorySpinner);

        patientSpinner = findViewById(R.id.logPatientSpinner);
        refreshLog = findViewById(R.id.refreshButton);

        logs = generateDummyLogItems();
        logTextBox = findViewById(R.id.logItemBox);

        populateCategorySpinner(categorySpinner);
        //populatePatientSpinner(patientSpinner);

        setLogTextBox(logs);

        refreshLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLogTextBox(logs);
            }
        });
    }

    /*TODO: PLACEHOLDER START
       Denna borde bytas ut mot en funktion som hämtar X antal loggar från databasen. "generateLogItems()"
       Här måste även filter implementeras
    */
    private ArrayList<LogItem> generateDummyLogItems(){
        ArrayList<LogItem> dummies = new ArrayList<>();

        CareGiver dummyGiverA = new CareGiver("Filip", "Andersson", "0718434687", "filip@andersson.se", "7878787878");
        CareGiver dummyGiverB = new CareGiver("Ebba", "Lindgren", "04387642396", "ebba@lindgren.se", "8989898989");
        CareTaker dummyTakerA = new CareTaker("Agda", "Eriksson", "0701234567", "agda@eriksson.se", "0123456789", "", "1234", "");
        CareTaker dummyTakerB = new CareTaker("Gunnar", "Petersson", "0731894564", "gunnar@petersson.se", "1123", "", "1324", "");


        LogItem temp;
        temp = new LogItem(dummyGiverA, dummyTakerA, LogItem.PATIENT_ADD); dummies.add(temp);
        temp = new LogItem(dummyGiverB, dummyTakerB, LogItem.PATIENT_ADD); dummies.add(temp);
        temp = new LogItem(dummyGiverA, dummyTakerB, LogItem.MEAL_CONFIRM, new LogItem.Meal("Spaghetti och köttfärssås")); dummies.add(temp);
        temp = new LogItem(dummyGiverA, dummyTakerB, LogItem.PATIENT_MODIFY); dummies.add(temp);
        temp = new LogItem(dummyGiverA, dummyTakerA, LogItem.EMERGENCY); dummies.add(temp);
        temp = new LogItem(dummyGiverA, dummyTakerA, LogItem.MEAL_CONFIRM, new LogItem.Meal("Schnitzel med klyftpotatis")); dummies.add(temp);
        temp = new LogItem(dummyGiverA, dummyTakerB, LogItem.MEAL_MISS, new LogItem.Meal("Spenatsoppa")); dummies.add(temp);
        return dummies;
    }
    //PLACEHOLDER END

    private void populateCategorySpinner(Spinner toPopulate){

        final String[] spinner_texts = getResources().getStringArray(R.array.log_category_array);

        categoryDDIs = new ArrayList<>(); //overwrites the old dropdown items if called multiple times.

        for (int i = 0; i < spinner_texts.length; i++){
            DropdownItem DDI = new DropdownItem();
            DDI.setText(spinner_texts[i]);
            DDI.setSelected(true);
            categoryDDIs.add(DDI);
        }
        DropdownAdapter viewCategory = new DropdownAdapter(getApplicationContext(), 0, categoryDDIs);
        toPopulate.setAdapter(viewCategory);
    }

    private void populatePatientSpinner(Spinner toPopulate){
        ArrayList<String> patient_texts = new ArrayList<>();
        patient_texts.add(getString(R.string.log_patient_array_title));
        patient_texts.add(getString(R.string.all));
        ArrayList<String> fetched = fetchPatients();
        patient_texts.addAll(fetched);
    }

    private ArrayList<String> fetchPatients(){
        //fetches all patients that the signed in caregiver has. used for populating the patient spinner.
        return null;
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
            case LogItem.PATIENT_MODIFY:{
                out += logItem.caregiver.getFullName() + " ";
                out += getString(R.string.LOG_MODIFY_PATIENT_S1) + " ";
                out += logItem.patient.getFullName();
                out += getString(R.string.LOG_MODIFY_PATIENT_S2);
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
                out += logItem.getMeal().getName() + " ";
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
                case 4: out.add("PATIENT_MODIFY"); break;
                case 5: out.add("MEAL_CONFIRM"); break;
                case 6: out.add("MEAL_MISS"); out.add("MEAL_SKIP"); break;
                case 7: out.add("EMERGENCY"); break;
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
            case "PATIENT_MODIFY":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return getColor(R.color.PATIENT_MODIFY);
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
