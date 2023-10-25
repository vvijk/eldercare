package com.example.myapplication;

import static com.example.myapplication.Helpers.isStringInArray;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.util.LogStorage;
import com.example.myapplication.util.LogStorage.FilterOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
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
    ArrayList<DropdownItem> categoryDDIs = new ArrayList<>(), patientDDIs = new ArrayList<>();
    ArrayList<LogItem> logs;

    CareGiver caregiver;
    ArrayList<CareTaker> patients;

    DropdownAdapter viewCategory;
    DropdownAdapter viewPatients;

    ArrayList<StateVO> categoryBoxes =  new ArrayList<>();
    ArrayList<StateVO> recipientBoxes = new ArrayList<>();
    ArrayList<String> recipientUIDs =  new ArrayList<>();

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

        // initData();
        // initUI();
        // populateCategorySpinner(categorySpinner);


        // setLogTextBox(logs);
        refreshLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLogStorage().retrieveLogs(getFilterOptions(), (items) -> {
                    refreshLogs(items);
                });
                // setLogTextBox(logs);
                if(patients != null && patients.size() > 0)
                    Log.d("LOGHISTORY", patients.get(0).getFullName());
            }
        });

        getLogStorage().initDBConnection();
        fixCategorySpinner();
        fixRecipientSpinner();
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
                    // fetchLogs(); // NOTE(Emarioo): I added this here, maybe bad idea.
                };
                fetchCaretaker(caretakerIDs.get(i), ctcb);
            }
        }
    }

    private void fetchLogs(){
        this.logs = new ArrayList<>();
        for (int i = 0; i < patients.size(); i++){
            ArrayList<LogItem> patientLogs = patients.get(i).getLogs();
            if(patientLogs != null)
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
        if(logs != null) {
            for (int i = 0; i < logs.size(); i++) {
                if (isStringInArray(logs.get(i).getCategory(), checkedCategories)) {
                    out.add(logs.get(i));
                }
            }
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
                case 5: out.add("MEAL_MISS"); break;
                case 6: out.add("MEAL_SKIP"); break;
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


    private String generateLogMessage(LogStorage.DisplayedItem item) {
        // TODO(Emarioo): This function is incomplete. Text is not formatted properly.
        String out = item.getFormattedTime() + " ";
        switch (item.category){
            case PATIENT_ADD:{
                out += item.caregiver + " ";
                out += getString(R.string.LOG_ADD_PATIENT_S1) + " ";
                out += item.caretaker;
                break;
            }
            case EMERGENCY:{
                out += item.caretaker + " ";
                out += getString(R.string.LOG_EMERGENCY_S1);
                break;
            }
            case MEAL_CONFIRM:{
                out += item.caretaker + " ";
                out += getString(R.string.LOG_MEAL_CONFIRM_S1) + " ";
                out += item.extraData + " ";
                out += getString(R.string.LOG_MEAL_CONFIRM_S2);
                break;
            }
            case MEAL_SKIP:{
                //TODO: [TimeStamp] [CareTaker] missade att bekräfta [första/andra] gången.
                //TODO: väv in vilken måltid som missades
                break;
            }
            case MEAL_MISS:{
                //TODO: väv in vilken måltid som missades
                out += item.caretaker + " ";
                out += getString(R.string.LOG_MEAL_MISS_S1);
                break;
            }
        }
        out += ".";
        return out;
    }
    public void fixCategorySpinner() {
        final String[] anArray = getResources().getStringArray(R.array.log_category_array);
        categorySpinner = (Spinner) findViewById(R.id.logCategorySpinner);

        categoryBoxes = new ArrayList<>();

        for (int i = 0; i < anArray.length; i++) {
            StateVO stateVO = new StateVO();
            stateVO.setTitle(anArray[i]);
            stateVO.setSelected(false);
            categoryBoxes.add(stateVO);
        }
        categoryBoxes.get(1).setSelected(true); // ALL category

        CheckDropDownAdapter myAdapter = new CheckDropDownAdapter(LogHistory.this, 0, categoryBoxes);
        categorySpinner.setAdapter(myAdapter);
        myAdapter.setChangedCheckListener(new CheckDropDownAdapter.ChangedCheckListener() {
            @Override
            public void onChange(CheckDropDownAdapter adapter, int itemIndex) {
                getLogStorage().retrieveLogs(getFilterOptions(), (items) -> {
                    refreshLogs(items);
                });
            }
        });
    }
    public void fixRecipientSpinner() {
        patientSpinner = (Spinner) findViewById(R.id.logPatientSpinner);
        recipientBoxes = new ArrayList<>();
        recipientBoxes.add(new StateVO(getString(R.string.log_patient_array_title)));

        recipientUIDs.clear();

        CheckDropDownAdapter myAdapter = new CheckDropDownAdapter(LogHistory.this, 0, recipientBoxes);
        patientSpinner.setAdapter(myAdapter);
        myAdapter.setChangedCheckListener(new CheckDropDownAdapter.ChangedCheckListener() {
            @Override
            public void onChange(CheckDropDownAdapter adapter, int itemIndex) {
                getLogStorage().retrieveLogs(getFilterOptions(), (items) -> {
                    refreshLogs(items);
                });
            }
        });

        getLogStorage().refCaregivers.child(getUserID()).child("caretakers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()) {
                    DataSnapshot data = iterator.next();

                    String recipientUID = data.getKey();
                    recipientUIDs.add(recipientUID);

                    getLogStorage().refCaretakers.child(recipientUID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String name = snapshot.child("fullName").getValue(String.class);
                            if(name == null)
                                name = snapshot.child("firstName").getValue(String.class) + " " + snapshot.child("lastName").getValue(String.class);
                            recipientBoxes.add(new StateVO(name, true));

                            if(recipientUIDs.size() == recipientBoxes.size() - 1) {
                                // update logs when last recipient in recipient spinner has been added
                                getLogStorage().retrieveLogs(getFilterOptions(), (items) -> {
                                    refreshLogs(items);
                                });
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

    }
    public LogStorage.FilterOptions getFilterOptions() {
        LogStorage.FilterOptions options = new LogStorage.FilterOptions();

        options.filterAllCategories = false;
        for(int i=0;i<categoryBoxes.size();i++) {
            StateVO v0 = categoryBoxes.get(i);
            if (!v0.isSelected()) {
                continue;
            }
            switch (i){
                case 1: {
                    options.filterAllCategories = true;
                    break;
                }
                case 2: {
                    options.add(LogStorage.Category.PATIENT_ADD);
                    break;
                }
                case 3: {
                    options.add(LogStorage.Category.MEAL_CONFIRM);
                    break;
                }
                case 4: {
                    options.add(LogStorage.Category.MEAL_SKIP);
                    break;
                }
                case 5: {
                    options.add(LogStorage.Category.MEAL_MISS);
                    break;
                }
                case 6: {
                    options.add(LogStorage.Category.EMERGENCY);
                    break;
                }
            }
        }
        options.filterAllCaretakers = false;
        for(int i=0;i<recipientBoxes.size();i++) {
            StateVO v0 = recipientBoxes.get(i);
            if (!v0.isSelected()) {
                continue;
            }
            int uidIndex = i-1;
            options.add(recipientUIDs.get(uidIndex));
        }
        return options;
    }
    private void refreshLogs(ArrayList<LogStorage.DisplayedItem> items) {
        clearTextField();
        for (int i = 0; i < items.size(); i++){
            LogStorage.DisplayedItem item = items.get(i);
            TextView text = new TextView(context);
            text.setText(generateLogMessage(item));
            text.setTextColor(getLogColor(item));
            text.setPadding(10, 0, 10, 0);

            logTextBox.addView(text);
        }
    }
    private int getLogColor(LogStorage.DisplayedItem item){
        //maps log color to resource file.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            switch (item.category) {
                case PATIENT_ADD:
                    return getColor(R.color.PATIENT_ADD);
                case EMERGENCY:
                    return getColor(R.color.EMERGENCY);
                case MEAL_CONFIRM:
                    return getColor(R.color.MEAL_CONFIRM);
                case MEAL_SKIP:
                    return getColor(R.color.MEAL_SKIP);
                case MEAL_MISS:
                    return getColor(R.color.MEAL_MISS);
            }
        }
        return 0;
    }
    LogStorage getLogStorage() {
        return ((MainApp)getApplicationContext()).logStorage;
    }
}
