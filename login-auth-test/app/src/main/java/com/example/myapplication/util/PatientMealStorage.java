package com.example.myapplication.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.MealDayActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import com.example.myapplication.dbLibrary;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

class Caregiver {
    ArrayList<Integer> patientIds = new ArrayList<>();
}
class Meal {
    public Meal(String name, int hour, int minute) {
        this.name = name;
        this.hour = hour;
        this.minute = minute;
    }
    public Meal() {} // default
    String name="<missing-name>"; // lunch, breakfast...
    int hour=12;
    int minute=0;
    String desc="";
}
class MealDay {
    ArrayList<Meal> meals = new ArrayList<>();
}
class MealPlan {
    String name = "<missing-name>";

    ArrayList<MealDay> days = new ArrayList<>();
}
class Patient {
    String name = "<missing-name>";
    int activeMealPlanId = 0;
}
abstract class ForsakenListener implements OnSuccessListener<DataSnapshot> {
    public Object userData;
    public ForsakenListener(Object userData) {
        this.userData = userData;
    }
}
/*
    This class provides ways of accessing an artificial database.
    It's temporary until the database supports patients and meal plans.

    IMPORTANT: There are critical sections when getting and setting values in the database.
      Firebase has transactions which can deal with those critical sections. Use those.
*/
public class PatientMealStorage {

    public PatientMealStorage() {
        // add dummy values
        String[] names = {
                "Carol",
                "Onion",
                "Steven",
                "Broccoli",
                "Mr. Java",
                "Mr. Rust",
                "Mrs. C++",
                "Roger",
                "Molly",
                "Sarah",
                "Crocodile",
                "Cat",
                "Sir. Burp",    
                "Patient A",
                "Patient B",
                "Patient C",
                "Patient D",
                "Patient E",
                "Patient X",
            };
        String[] meal_names = {
                "Beef meal",
                "Vegan meal",
                "Snack meal",
                "Fruit meal",
        };
        Caregiver caregiver = new Caregiver();
        for (String str : names) {
            int patientId = patients.size() + 1;
            Patient patient = new Patient();
            patient.name = str;
            patients.put(patientId, patient);
            caregiver.patientIds.add(patientId);
        }
        int caregiverId = 1;
        caregivers.put(caregiverId, caregiver);

        for(String str : meal_names) {
            int mealId = mealPlans.size()+1;
            MealPlan mealPlan = new MealPlan();
            mealPlan.name = str;
            for (int i = 0; i < 365; i++) {
                MealDay mealDay = new MealDay();
                Meal breakfast = new Meal("Breakfast",8,10);
                Meal lunch = new Meal("Lunch",12,30);
                Meal dinner = new Meal("Dinner",19,30);
                mealDay.meals.add(breakfast);
                mealDay.meals.add(lunch);
                mealDay.meals.add(dinner);
                mealPlan.days.add(mealDay);
            }
            
            mealPlans.put(mealId, mealPlan);
            mealPlanIds.add(mealId);
        }
    }

    boolean useDatabase = false;
    boolean initialized = false;
    public void initDBConnection() {
        if(initialized)
            return;
        useDatabase = true;
        initialized = true;
        // TODO(Emarioo): Should the API to the database exist in one class (dbLibrary) or multiple (dbLibrary and PatientMealStorage)?
        //  Firebase database has some listeners and what not so perhaps we should use Firebase directly without an API.
        //  In some cases, like meal management, it is nice to have an API which handles the database stuff. It depends if you
        //  want listeners on the text views to automatically update the UI (which I think you can do?).
        db_mealPlans = FirebaseDatabase.getInstance().getReference("mealPlans");
    }

    DatabaseReference db_mealPlans;

    private HashMap<Integer, Caregiver> caregivers = new HashMap<>(); // caregiverId -> list of patientIds
    private HashMap<Integer, Patient> patients = new HashMap<>();
    private HashMap<Integer, MealPlan> mealPlans = new HashMap<>();
    private ArrayList<Integer> mealPlanIds = new ArrayList<>();

    public void refreshMealPlans(Runnable runnable) {
        if(!initialized) return;
        OnSuccessListener<DataSnapshot> listener = new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot==null) return;

                Integer count = dataSnapshot.getValue(Integer.class);
                if(count == null) count = 0;

                OnSuccessListener<DataSnapshot> mealListener = new ForsakenListener(count) {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.getValue(String.class);
                        if (name == null) name = "";

                        int id = 1 + Integer.parseInt(dataSnapshot.getRef().getParent().getKey());

                        MealPlan plan = mealPlans.get(id);
                        if(plan == null) {
                            plan = new MealPlan();
                            mealPlans.put(id, plan);
                        }
                        plan.name = name;

                        // Race condition if listener is multithreaded.
                        int count = (Integer)userData;
                        count--;
                        userData = count;
                        if(count==0) {
                            if(runnable != null)
                                runnable.run();
                        }
                    }
                };
                mealPlanIds.clear();
                for(int i=0;i<count;i++) {
                    mealPlanIds.add(i+1);
                    db_mealPlans.child("" + i).child("name").get().addOnSuccessListener(mealListener);
                }
            }
        };
        db_mealPlans.child("count").get().addOnSuccessListener(listener);
    }
    public void refreshMealDays(int mealPlanId, Runnable runnable) {
        if(!initialized) return;
        if(mealPlanId == 0) return;
        OnSuccessListener<DataSnapshot> listener = new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot == null) return;

                Integer count = dataSnapshot.getValue(Integer.class);
                if(count == null) count = 0;

                MealPlan plan = mealPlans.get(mealPlanId);
                // TODO: Save to database before clearing days?
                plan.days.clear();
                for(int i=0;i<count;i++) {
                    plan.days.add(new MealDay());
                }

                if(runnable != null)
                    runnable.run();
            }
        };
        db_mealPlans.child(""+(mealPlanId-1)).child("count").get().addOnSuccessListener(listener);
    }
    public void refreshMeals(int mealPlanId, int dayIndex, Runnable runnable) {
        if(!initialized) return;
        if(mealPlanId == 0) return;
        OnSuccessListener<DataSnapshot> listener = new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if(dataSnapshot==null) return;
                Integer count = dataSnapshot.getValue(Integer.class);
                if(count == null) count = 0;

                OnSuccessListener<DataSnapshot> mealListener = new ForsakenListener(count) {
                    @Override
                    public void onSuccess(DataSnapshot dataSnapshot) {
                        
                        int mealIndex = Integer.parseInt(dataSnapshot.getRef().getParent().getKey());
                        
                        Meal meal = mealPlans.get(mealPlanId).days.get(dayIndex).meals.get(mealIndex);
                        if (dataSnapshot.getRef().getKey().equals("name")) {
                            String name = dataSnapshot.getValue(String.class);
                            if (name == null) name = "no name ):";
                            meal.name = name;
                        } else if (dataSnapshot.getRef().getKey().equals("hour")) {
                            Integer num = dataSnapshot.getValue(Integer.class);
                            if (num == null) num = 12;
                            meal.hour = num;
                        } else if (dataSnapshot.getRef().getKey().equals("minute")) {
                            Integer num = dataSnapshot.getValue(Integer.class);
                            if (num == null) num = 0;
                            meal.minute = num;
                        } else if (dataSnapshot.getRef().getKey().equals("desc")) {
                            String str = dataSnapshot.getValue(String.class);
                            if (str == null) str = "";
                            meal.desc = str;
                        }

                        // Race condition if listener is multithreaded.
                        int count = (Integer)userData;
                        count--;
                        userData = count;
                        
                        if(runnable != null && count == 0)
                            runnable.run();
                    }
                };

                MealPlan plan = mealPlans.get(mealPlanId);
                MealDay day = plan.days.get(dayIndex);
                // TODO: Save to database before clearing days?
                day.meals.clear();
                for(int i=0;i<count;i++) {
                    day.meals.add(new Meal());
                    db_mealPlans.child(""+(mealPlanId-1)).child(""+dayIndex).child(""+i).child("name").get().addOnSuccessListener(mealListener);
                    db_mealPlans.child(""+(mealPlanId-1)).child(""+dayIndex).child(""+i).child("hour").get().addOnSuccessListener(mealListener);
                    db_mealPlans.child(""+(mealPlanId-1)).child(""+dayIndex).child(""+i).child("minute").get().addOnSuccessListener(mealListener);
                    db_mealPlans.child(""+(mealPlanId-1)).child(""+dayIndex).child(""+i).child("desc").get().addOnSuccessListener(mealListener);
                }
            }
        };
        db_mealPlans.child(""+(mealPlanId-1)).child(""+dayIndex).child("count").get().addOnSuccessListener(listener);
    }

    public int patientCountOfCaregiver(int caregiverId) {
        Caregiver caregiver = caregivers.get(caregiverId);
        if(caregiver == null || caregiver.patientIds == null)
            return 0;
        return caregiver.patientIds.size();
    }
    // returns 0 if something failed
    public int patientIdFromIndex(int caregiverId, int patientIndex) {
        Caregiver caregiver = caregivers.get(caregiverId);
        if(caregiver == null || caregiver.patientIds == null)
            return 0;
        return caregiver.patientIds.get(patientIndex);
    }
    // returns null if patientId was invalid
    public String nameOfPatient(int patientId) {
        Patient patient = patients.get(patientId);
        if(patient == null)
            return "<invalid-patient>";
        return patient.name;
    }
    public int mealPlanIdOfPatient(int patientId) {
        Patient patient = patients.get(patientId);
        if(patient == null)
            return 0;
        return patient.activeMealPlanId;
    }
    public void setMealPlanIdOfPatient(int patientId, int mealPlanId) {
        Patient patient = patients.get(patientId);
        if(patient == null)
            return;
        patient.activeMealPlanId = mealPlanId;
    }
    public void setNameOfMealPlan(int mealPlanId, String name) {
        if(useDatabase) {
            db_mealPlans.child(""+(mealPlanId-1)).child("name").setValue(name);
        }
        MealPlan plan = mealPlans.get(mealPlanId);
        if(plan == null) return;
        plan.name = name;
    }

    public int countOfMealPlans(){
        return mealPlanIds.size();
    }
    public int mealPlanIdFromIndex(int mealPlanIndex) {
        if(useDatabase)
            return mealPlanIndex + 1;
        return mealPlanIds.get(mealPlanIndex);
    }
    public void addMealPlan(String name) {
        if(useDatabase) {
            db_mealPlans.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    Integer count = currentData.child("count").getValue(Integer.class);
                    if(count == null) count = 0;
                    currentData.child("count").setValue(count + 1);
                    int index = count;
                    currentData.child(""+index).child("name").setValue(name);

                    return Transaction.success(currentData);
                }
                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                }
            });
        }
        int id = mealPlanIds.size() + 1;
        mealPlanIds.add(id);
        MealPlan plan = new MealPlan();
        plan.name = name;
        mealPlans.put(id, plan);
    }
    public String nameOfMealPlan(int mealPlanId) {
        return mealPlans.get(mealPlanId).name;
    }
    public int countOfMealDays(int mealPlanId) {
        return mealPlans.get(mealPlanId).days.size();
    }
    public int countOfMeals(int mealPlanId, int mealDayIndex) {
        return mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.size();
    }
    public String nameOfMeal(int mealPlanId, int mealDayIndex, int mealIndex) {
        return mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.get(mealIndex).name;
    }
    public int hourOfMeal(int mealPlanId, int mealDayIndex, int mealIndex) {
        return mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.get(mealIndex).hour;
    }
    public int minuteOfMeal(int mealPlanId, int mealDayIndex, int mealIndex) {
        return mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.get(mealIndex).minute;
    }
    public String descriptionOfMeal(int mealPlanId, int mealDayIndex, int mealIndex) {
        return mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.get(mealIndex).desc;
    }
    public void addMeal(int mealPlanId, int mealDayIndex, String name) {
        if(useDatabase){
            db_mealPlans.child(""+(mealPlanId-1)).child(""+(mealDayIndex)).runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                    // TODO(Emarioo): Prevent race condition (not only here)
                    Integer count = currentData.child("count").getValue(Integer.class);
                    if(count == null) count = 0;
//                    System.out.println("Transact "+count);
                    currentData.child("count").setValue(count + 1);
                    int index = count;
                    currentData.child(""+index).child("name").setValue(name);
                    currentData.child(""+index).child("hour").setValue(12);
                    currentData.child(""+index).child("minute").setValue(0);
                    currentData.child(""+index).child("desc").setValue("");
//                    System.out.println("Done "+count);

                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {

                }
            });
        }
        Meal meal = new Meal();
        meal.desc = "";
        meal.name = name;
        mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.add(meal);
    }
    public void deleteMeal(int mealPlanId, int mealDayIndex, int mealIndex){
        if(useDatabase){
//             db_mealPlans.child(""+(mealPlanId-1)).child(""+(mealDayIndex)).child(""+mealIndex).removeValue();
            throw new RuntimeException("Delete meal not implemented");
        }
        mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.remove(mealIndex);
    }
    public void setNameOfMeal(int mealPlanId, int mealDayIndex, int mealIndex, String name) {
        if(useDatabase) {
            db_mealPlans.child(""+(mealPlanId-1)).child(""+(mealDayIndex)).child(""+mealIndex).child("name").setValue(name);
        }
        mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.get(mealIndex).name = name;
    }
    public void setHourOfMeal(int mealPlanId, int mealDayIndex, int mealIndex, int hour) {
        if(useDatabase) {
            db_mealPlans.child(""+(mealPlanId-1)).child(""+(mealDayIndex)).child(""+mealIndex).child("hour").setValue(hour);
        }
        mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.get(mealIndex).hour = hour;
    }
    public void setMinuteOfMeal(int mealPlanId, int mealDayIndex, int mealIndex, int minute){
        if(useDatabase) {
            db_mealPlans.child(""+(mealPlanId-1)).child(""+(mealDayIndex)).child(""+mealIndex).child("minute").setValue(minute);
        }
        mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.get(mealIndex).minute = minute;
    }
    public void setDescriptionOfMeal(int mealPlanId, int mealDayIndex, int mealIndex, String description){
        if(useDatabase) {
            db_mealPlans.child(""+(mealPlanId-1)).child(""+(mealDayIndex)).child(""+mealIndex).child("desc").setValue(description);
        }
        mealPlans.get(mealPlanId).days.get(mealDayIndex).meals.get(mealIndex).desc = description;
    }
}