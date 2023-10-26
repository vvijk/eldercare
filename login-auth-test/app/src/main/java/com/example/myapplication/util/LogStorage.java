package com.example.myapplication.util;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.annotation.SuppressLint;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Filter;

import javax.annotation.Nullable;

public class LogStorage {

    FirebaseAuth mAuth;
    public DatabaseReference refLogs;
    public DatabaseReference refCaretakers;
    public DatabaseReference refCaregivers;

    public enum Category {
        PATIENT_ADD,
        MEAL_CONFIRM,
        MEAL_MISS,
        MEAL_SKIP,
        EMERGENCY,
    }
    @SuppressLint("SimpleDateFormat")
    protected static final SimpleDateFormat date_format = new SimpleDateFormat("[dd/MM-yy HH:mm:ss]");
    // this is not the item in the database
    public class DisplayedItem {
        public Category category;
        public long timestamp;
        public String caretaker;
        public String caregiver; // depends on category
        public String extraData; // depends on category, can be meal name, or something else,

        public String getFormattedTime() {
            Date date = new Date(timestamp);
            return date_format.format(date);
        }
    }
    public static class FilterOptions {
        ArrayList<String> caretakers = new ArrayList<>();
        ArrayList<Category> categories = new ArrayList<>();
        public boolean sortByAscendingTime = false; // false is descending
        public boolean filterAllCategories = true;
        public boolean filterAllCaretakers = true;

        public FilterOptions add(Category cat) {
            categories.add(cat);
            return this;
        }
        public FilterOptions add(String caretakerUID) {
            caretakers.add(caretakerUID);
            return this;
        }
        public boolean has(Category category) {
            if(filterAllCategories)
                return true;
            for(int i=0;i<categories.size();i++) {
                if(categories.get(i) == category)
                    return true;
            }
            return false;
        }
        public boolean has(String caretakerUID) {
            if(filterAllCaretakers)
                return true;
            for(int i=0;i<caretakers.size();i++) {
                if(caretakers.get(i).equals(caretakerUID))
                    return true;
            }
            return false;
        }
    }
    // Connects to database from log storage unless already connected
    public void initDBConnection() {
        if(mAuth == null) {
            mAuth = FirebaseAuth.getInstance();
            refLogs = FirebaseDatabase.getInstance().getReference("logs");
            refCaretakers = FirebaseDatabase.getInstance().getReference("users/caretakers");
            refCaregivers = FirebaseDatabase.getInstance().getReference("users/caregivers");
        }
    }
    public void submitLog(Category category, String caretakerUID, @Nullable String caregiverUID, @Nullable String extraData) {
        initDBConnection(); // just in case you forgot to init database connection
        DatabaseReference ref = refLogs.child(caretakerUID).push();

        // TODO(Emarioo): You ensure that extraData isn't null when category is a meal type. This is not a high priority though.
        // RuntimeException err = new RuntimeException("Not suppoed to be null");
        // switch(category) {
        //     case EMERGENCY: {
        //         if(caretakerUID == null)
        //             throw err;
        //         break;
        //     }
        //     case PATIENT_ADD: {
        //         if(caretakerUID == null)
        //             throw err;
        //         break;
        //     }
        // }

        ref.child("timestamp").setValue(System.currentTimeMillis());
        ref.child("category").setValue(category);
        if(caregiverUID != null)
            ref.child("caregiver").setValue(caregiverUID);
        if(extraData != null)
            ref.child("extraData").setValue(extraData);
    }
    public interface RetrievalCallback {
        void retrievedItems(ArrayList<DisplayedItem> items);
    }
    public void retrieveLogs(@Nullable FilterOptions filterOptions, RetrievalCallback callback) {
        initDBConnection();
        class PendingSharedRetrieval {
            ArrayList<DisplayedItem> items = new ArrayList<>();
            int remainingListeners = 0;
            boolean finished = false;
            FilterOptions options;
            void sort() {
                if(options == null)
                    return;

                // TODO(Emarioo): Don't use bubble sort
                for(int i=0;i<items.size()-1;i++) {
                    boolean changed = false;
                    for(int j=0;j<items.size() - 1 - i;j++) {
                        boolean swap = false;
                        if(options.sortByAscendingTime)
                            swap = items.get(j).timestamp > items.get(j+1).timestamp;
                        else
                            swap = items.get(j).timestamp < items.get(j+1).timestamp;
                        if(swap) {
                            DisplayedItem tmp = items.get(j);
                            items.set(j,items.get(j+1));
                            items.set(j+1, tmp);
                            changed = true;
                        }
                    }
                    if(!changed)
                        break;
                }
            }
            boolean tryFinish(RetrievalCallback callback) {
                if (remainingListeners == 0 && finished) {
                    sort();
                    // retrieving = false;
                    callback.retrievedItems(items);
                    return true;
                }
                return false;
            }
        }
        abstract class PendingRetrieval implements ValueEventListener {
            PendingRetrieval(PendingSharedRetrieval sharedRetrieval) {
                this.sharedRetrieval = sharedRetrieval;
            }
            PendingSharedRetrieval sharedRetrieval;
        }

        // NOTE(Emarioo): I am not sure how firebase works but a listener like this may fetch all logs to the app
        //  even if we don't use all of them. We won't have many logs so we can take this shortcut.
        refLogs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                PendingSharedRetrieval sharedRetrieval = new PendingSharedRetrieval();
                sharedRetrieval.options = filterOptions;
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()) {
                    DataSnapshot data = iterator.next();
                    String caretakerUID = data.getKey();
                    if (!filterOptions.has(caretakerUID))
                        continue;

                    Iterator<DataSnapshot> logIterator = data.getChildren().iterator();
                    while (logIterator.hasNext()) {
                        DataSnapshot logdata = logIterator.next();

                        long timestamp = 0;
                        if(logdata.child("timestamp").getValue() != null && logdata.child("timestamp").getValue() instanceof Long)
                            timestamp = logdata.child("timestamp").getValue(Long.class);
                        Category category = logdata.child("category").getValue(Category.class);
                        String caregiverUID = logdata.child("caregiver").getValue(String.class);
                        String extraData = logdata.child("extraData").getValue(String.class);
                        if (category == null)
                            continue; // bug in the code or did the database get messed up?
                        if (!filterOptions.has(category))
                            continue;

                        DisplayedItem item = new DisplayedItem();
                        int itemIndex = sharedRetrieval.items.size();
                        sharedRetrieval.items.add(item);
                        item.extraData = extraData;
                        if (caretakerUID != null) {
                            sharedRetrieval.remainingListeners++;
                            refCaretakers.child(caretakerUID).addListenerForSingleValueEvent(new PendingRetrieval(sharedRetrieval) {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    sharedRetrieval.remainingListeners--;
                                    DisplayedItem item = sharedRetrieval.items.get(itemIndex);
                                    item.caretaker = snapshot.child("fullName").getValue(String.class);
                                    if(item.caretaker == null) {
                                        item.caretaker = snapshot.child("firstName").getValue(String.class) +" "+ snapshot.child("lastName").getValue(String.class);
                                    }
                                    sharedRetrieval.tryFinish(callback);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                        }
                        if (caregiverUID != null) {
                            sharedRetrieval.remainingListeners++;
                            refCaregivers.child(caregiverUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    sharedRetrieval.remainingListeners--;
                                    DisplayedItem item = sharedRetrieval.items.get(itemIndex);
                                    item.caregiver = snapshot.child("fullName").getValue(String.class);
                                    if(item.caregiver == null) {
                                        item.caregiver = snapshot.child("firstName").getValue(String.class) +" "+ snapshot.child("lastName").getValue(String.class);
                                    }
                                    sharedRetrieval.tryFinish(callback);
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError error) { }
                            });
                        }

                        item.category = category;
                        item.timestamp = timestamp;
                    }
                }
                sharedRetrieval.finished = true;
                sharedRetrieval.tryFinish(callback);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
