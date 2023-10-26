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

public class LogHistory extends AppCompatActivity {
    //Page for viewing Logs.

    FirebaseAuth mAuth;

    Context context;
    Spinner categorySpinner, patientSpinner;
    Button refreshLog;
    LinearLayout logTextBox;

    ArrayList<StateVO> categoryBoxes =  new ArrayList<>();
    ArrayList<StateVO> recipientBoxes = new ArrayList<>();
    ArrayList<String> recipientUIDs =  new ArrayList<>();

    ValueEventListener logRefreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        mAuth = FirebaseAuth.getInstance();
        getLogStorage().initDBConnection();

        setContentView(R.layout.activity_log_history);
        categorySpinner = findViewById(R.id.logCategorySpinner);

        patientSpinner = findViewById(R.id.logPatientSpinner);
        refreshLog = findViewById(R.id.refreshButton);

        logTextBox = findViewById(R.id.logItemBox);

        refreshLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLogStorage().retrieveLogs(getFilterOptions(), (items) -> {
                    refreshLogs(items);
                });
            }
        });

        getLogStorage().refLogs.addValueEventListener(logRefreshListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getLogStorage().retrieveLogs(getFilterOptions(), (items) -> {
                    refreshLogs(items);
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

        fixCategorySpinner();
        fixRecipientSpinner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLogStorage().refLogs.removeEventListener(logRefreshListener);
    }

    public String getUserID(){
        FirebaseUser user = mAuth.getCurrentUser();
        return (user != null) ? user.getUid() : null;
    }
    private void clearTextField(){
        logTextBox.removeAllViews();
    }
    private String generateLogMessage(LogStorage.DisplayedItem item) {
        String out = item.getFormattedTime() + " ";
        switch (item.category){
            case PATIENT_ADD:{
                out += getString(R.string.LOG_ADD_PATIENT, item.caregiver, item.caretaker);
                break;
            }
            case EMERGENCY:{
                out += getString(R.string.LOG_EMERGENCY, item.caretaker);
                break;
            }
            case MEAL_CONFIRM:{
                out += getString(R.string.LOG_MEAL_CONFIRM, item.caretaker, item.extraData);
                break;
            }
            case MEAL_SKIP:{
                out += getString(R.string.LOG_MEAL_SKIP, item.caretaker, item.extraData);
                break;
            }
            case MEAL_MISS:{
                out += getString(R.string.LOG_MEAL_MISS, item.caretaker, item.extraData);
                break;
            }
            default: {
                out += getString(R.string.LOG_UNKNOWN);
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
