package com.example.myapplication.util;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Filter;

import javax.annotation.Nullable;

public class LogStorage {

    FirebaseAuth mAuth;
    DatabaseReference refLogs;
    // DatabaseReference refCaretakers;

    public enum Category {
        PATIENT_ADD,
        MEAL_CONFIRM,
        MEAL_MISS,
        MEAL_SKIP,
        EMERGENCY,
    }
    // this is not the item in the database
    public class DisplayedItem {
        public Category category;
        public String timestamp; // should probably not be a string
        public String caretaker;
        public String caregiver; // depends on category
        public String extraData; // depends on category, can be meal name, or something else,
    }
    public static class FilterOptions {
        ArrayList<String> caretakers = new ArrayList<>();
        ArrayList<Category> categories = new ArrayList<>();
        boolean sortByAscendingTime = true; // false would be descending
        boolean filterAllCaretakers = true;
        boolean filterAllCategories = true;

        public FilterOptions add(Category cat) {
            categories.add(cat);
            return this;
        }
        public FilterOptions add(String caretakerUID) {
            caretakers.add(caretakerUID);
            return this;
        }
        // public FilterOptions allCategories() {
        //     categories.clear();
        //     categories.add(Category.EMERGENCY);
        //     categories.add(Category.PATIENT_ADD);
        //     categories.add(Category.MEAL_CONFIRM);
        //     categories.add(Category.MEAL_MISS);
        //     categories.add(Category.MEAL_SKIP);
        //     return this;
        // }
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
        }
    }
    public void submitLog(Category category, String caretakerUID, @Nullable String caregiverUID, @Nullable String extraData) {
        initDBConnection(); // just in case you forgot to init database connection
        DatabaseReference ref = refLogs.child(caretakerUID).push();

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
        // NOTE(Emarioo): I am not sure how firebase works but a listener like this may fetch all logs to the app
        //  even if we don't use all of them. We won't have many logs so we can take this shortcut.
        refLogs.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<DisplayedItem> items = new ArrayList<>();
                Iterator<DataSnapshot> iterator = snapshot.getChildren().iterator();
                while(iterator.hasNext()) {
                    DataSnapshot data = iterator.next();
                    String caretakerUID = data.getKey();
                    if (!filterOptions.has(caretakerUID))
                        continue;

                    Iterator<DataSnapshot> logIterator = data.getChildren().iterator();
                    while (logIterator.hasNext()) {
                        DataSnapshot logdata = logIterator.next();
                        String timestamp = logdata.getKey();
                        Category category = logdata.child("category").getValue(Category.class);
                        String caregiverUID = logdata.child("caregiver").getValue(String.class);
                        String extraData = logdata.child("extraData").getValue(String.class);
                        if (category == null)
                            continue; // bug in the code or did the database get messed up?
                        if (!filterOptions.has(category))
                            continue;

                        DisplayedItem item = new DisplayedItem();
                        item.extraData = extraData;
                        item.caretaker = caretakerUID;
                        item.caregiver = caregiverUID;
                        item.category = category;
                        item.timestamp = timestamp;
                        items.add(item);
                    }
                }
                callback.retrievedItems(items);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //public ArrayList<DisplayedItem> retrieveCachedLogs(@Nullable FilterOptions filterOptions) { ... }
}
