package com.example.myapplication.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.MealDayActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
    String key;
    String name="<missing-name>"; // lunch, breakfast...
    int hour=12;
    int minute=0;
    String desc="";
}
class MealDay {
    ArrayList<Meal> meals = new ArrayList<>();

    // returns -1 none of meals have key
    public int indexOf(String key) {
        // NOTE: meals is not a hashmap because a list is usually faster with fewer items. 3-5 meals is to few for a hashmap.
        for(int i=0;i<meals.size();i++) {
            if(meals.get(i) == null)
                continue;
            if(meals.get(i).key.equals(key))
                return i;
        }
        return -1;
    }
    public Meal getMeal(String key) {
        int index = indexOf(key);
        if(index == -1)
            return null;
        return meals.get(indexOf(key));
    }
    public int addMeal(Meal meal) {
        int index = meals.indexOf(null);
        if(index != -1) {
            meals.set(index, meal);
            return index;
        }
        meals.add(meal);
        return meals.size()-1;
    }
}
class MealPlan {
    String key;
    String name = "<missing-name>";

    // TODO: Year list
    private ArrayList<MealDay> days = new ArrayList<>();

    public MealDay getDay(int dayIndex) {
        if(days.size() <= dayIndex)
            return null;
        return days.get(dayIndex);
    }
    public void setDay(int dayIndex, MealDay mealDay) {
        for(int i=days.size();i<dayIndex+1;i++) {
            days.add(null);
        }
        days.set(dayIndex,mealDay);
    }
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

        if(!useDatabase) {
            for (String str : meal_names) {
                MealPlan mealPlan = new MealPlan();
                mealPlan.name = str;
                for (int i = 0; i < 365; i++) {
                    MealDay mealDay = new MealDay();
                    Meal breakfast = new Meal("Breakfast", 8, 10);
                    Meal lunch = new Meal("Lunch", 12, 30);
                    Meal dinner = new Meal("Dinner", 19, 30);
                    mealDay.meals.add(breakfast);
                    mealDay.meals.add(lunch);
                    mealDay.meals.add(dinner);
                    mealPlan.setDay(i, mealDay);
                }

                addMealPlan(mealPlan);
            }
        }
    }

    private boolean useDatabase = true;
    private boolean initialized = false;
    public void initDBConnection() {
        if(initialized)
            return;
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

    private HashMap<String, MealPlan> mealPlans_by_key = new HashMap<>();
    private ArrayList<MealPlan> _mealPlans = new ArrayList<>();

    public int addMealPlan(MealPlan plan) {
        mealPlans_by_key.put(plan.key, plan);
        int index = _mealPlans.indexOf(null);
        if (index != -1) {
            _mealPlans.set(index, plan);
            return index;
        }
        _mealPlans.add(plan);
        return _mealPlans.size() - 1;
    }
    public MealPlan getMealPlan(int mealPlanId) {
        if(_mealPlans.size() > mealPlanId - 1 && mealPlanId > 0) {
            return _mealPlans.get(mealPlanId-1);
        }
        return null;
    }
    public MealPlan getMealPlan(String key) {
        return mealPlans_by_key.get(key);
    }
    public boolean removeMealPlan(int mealPlanId) {
        if(_mealPlans.size() <= mealPlanId-1)
            return false;
        _mealPlans.set(mealPlanId-1, null);
        return true;
    }
    public boolean removeMealPlan(MealPlan plan) {
        int index = _mealPlans.indexOf(plan);
        if(index!=-1) {
            _mealPlans.set(index, null);
            return true;
        }
        return false;
    }
//    public int indexOfMealPlan(MealPlan plan) {
//        return _mealPlans.indexOf(plan);
//    }
    
    class Refresher {
        Runnable runnable;
        ChildEventListener listener;
        DatabaseReference ref; // listener must be removed from ref
    }
    private ArrayList<Refresher> refreshers = new ArrayList<>();
    public void pushRefresher(Runnable runnable) {
        if(!initialized) {
            throw new RuntimeException("Call initDBConnection before pushing refresher");
        }
        Refresher refresher = new Refresher();
        refreshers.add(refresher);
        refresher.runnable = runnable;
        refresher.ref = db_mealPlans;
        refresher.listener = refresher.ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String planKey = snapshot.getKey();
                MealPlan plan = mealPlans_by_key.get(planKey);
                String name = snapshot.child("name").getValue(String.class);
                if(plan == null) {
                    plan = new MealPlan();
                    plan.key = planKey;
                    addMealPlan(plan);
                    mealPlans_by_key.put(planKey, plan);
                }
                if(name == null) {
                    name = "unnamed plan";
                }
                plan.name = name;
//                System.out.println("[Child listener] plan added "+name);

                if(refreshers.size()!=0) refreshers.get(refreshers.size()-1).runnable.run();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String planKey = snapshot.getKey();
                MealPlan plan = mealPlans_by_key.get(planKey);
                String name = snapshot.child("name").getValue(String.class);
                if(plan == null) {
                    plan = new MealPlan();
                    plan.key = planKey;
                    addMealPlan(plan);
                    mealPlans_by_key.put(planKey, plan);
                }
                if(name == null) {
                    name = "unnamed plan";
                }
                plan.name = name;
//                System.out.println("[Child listener] plan changed "+name);

                // TODO: Don't run refresher if the new name is equal to the old name.
                //   Additionally we can set the name on mealPlans directly in setNameOfMealPlan which would be more responsive
                //   since we won't wait for the change to be sent to the database and then sent back to this listener.
                //   This should be done for all refreshers.
                if(refreshers.size()!=0)
                    refreshers.get(refreshers.size()-1).runnable.run();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String planKey = snapshot.getKey();
                MealPlan plan = mealPlans_by_key.get(planKey);
                mealPlans_by_key.remove(planKey);

                if(removeMealPlan(plan)) {
//                    System.out.println("[Child listener] plan removed " + plan.name);
                }

                if(refreshers.size()!=0)
                    refreshers.get(refreshers.size()-1).runnable.run();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                System.out.println("[child listener] MOVED!");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                System.out.println("[child listener] CANCELLED!");
            }
        });
    }
    public void pushRefresher(int mealPlanId, int dayIndex, Runnable runnable) {
        if(!initialized) {
            throw new RuntimeException("Call initDBConnection before pushing refresher");
        }
        Refresher refresher = new Refresher();
        refreshers.add(refresher);
        refresher.runnable = runnable;
        MealPlan plan = getMealPlan(mealPlanId);
        if (plan == null)
            return;
        int year = 2023;
        refresher.ref = db_mealPlans.child(plan.key).child("years").child(""+year).child(""+dayIndex);
        refresher.listener = refresher.ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String mealKey = snapshot.getKey();
                int dayIndex = Integer.parseInt(snapshot.getRef().getParent().getKey());
                int year = Integer.parseInt(snapshot.getRef().getParent().getParent().getKey());
                String planKey = snapshot.getRef().getParent().getParent().getParent().getParent().getKey();

                MealPlan plan = mealPlans_by_key.get(planKey);
                if(plan == null) {
                    plan = new MealPlan();
                    plan.key = planKey;
                    plan.name = "unnamed plan";
                    addMealPlan(plan); // TODO: Meal plan should be a
                    mealPlans_by_key.put(planKey, plan);
                }
                MealDay mealDay = plan.getDay(dayIndex);
                if(mealDay == null) {
                    mealDay = new MealDay();
                    plan.setDay(dayIndex, mealDay);
                }
                Meal meal = mealDay.getMeal(mealKey);
                if(meal == null) {
                    meal = new Meal();
                    meal.key = mealKey;
                    mealDay.meals.add(meal);
                }
                Object tmp = snapshot.child("name").getValue(String.class);
                if(tmp != null)
                    meal.name = (String)tmp;
                tmp = snapshot.child("hour").getValue(Integer.class);
                if(tmp != null)
                    meal.hour = (Integer)tmp;
                tmp = snapshot.child("minute").getValue(Integer.class);
                if(tmp != null)
                    meal.minute = (Integer)tmp;
                tmp =  snapshot.child("desc").getValue(String.class);
                if(tmp != null)
                    meal.desc = (String)tmp;

//                System.out.println("[Child listener] meal added "+meal.name);
                if(refreshers.size()!=0)
                    refreshers.get(refreshers.size()-1).runnable.run();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String mealKey = snapshot.getKey();
                int dayIndex = Integer.parseInt(snapshot.getRef().getParent().getKey());
                int year = Integer.parseInt(snapshot.getRef().getParent().getParent().getKey());
                String planKey = snapshot.getRef().getParent().getParent().getParent().getParent().getKey();

                MealPlan plan = mealPlans_by_key.get(planKey);
                if(plan == null) {
                    plan = new MealPlan();
                    plan.key = planKey;
                    plan.name = "unnamed plan";
                    addMealPlan(plan); // TODO: Meal plan should be a
                    mealPlans_by_key.put(planKey, plan);
                }
                MealDay mealDay = plan.getDay(dayIndex);
                if(mealDay == null) {
                    mealDay = new MealDay();
                    plan.setDay(dayIndex, mealDay);
                }

                Meal meal = mealDay.getMeal(mealKey);
                if(meal == null) {
                    meal = new Meal();
                    meal.key = mealKey;
                    mealDay.meals.add(meal);
                }
                Object tmp = snapshot.child("name").getValue(String.class);
                if(tmp != null)
                    meal.name = (String)tmp;
                tmp = snapshot.child("hour").getValue(Integer.class);
                if(tmp != null)
                    meal.hour = (Integer)tmp;
                tmp = snapshot.child("minute").getValue(Integer.class);
                if(tmp != null)
                    meal.minute = (Integer)tmp;
                tmp =  snapshot.child("desc").getValue(String.class);
                if(tmp != null)
                    meal.desc = (String)tmp;

//                System.out.println("[Child listener] meal changed "+meal.name);
                if(refreshers.size()!=0)
                    refreshers.get(refreshers.size()-1).runnable.run();
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                String mealKey = snapshot.getKey();
                int dayIndex = Integer.parseInt(snapshot.getRef().getParent().getKey());
                int year = Integer.parseInt(snapshot.getRef().getParent().getParent().getKey());
                String planKey = snapshot.getRef().getParent().getParent().getParent().getParent().getKey();

                String name = "none?";

                MealPlan plan = mealPlans_by_key.get(planKey);
                if(plan != null) {
                    MealDay mealDay = plan.getDay(dayIndex);
                    if (mealDay != null) {
                        int index = mealDay.indexOf(mealKey);
                        if (index != -1) {
                            name = mealDay.meals.get(index).name;
                            mealDay.meals.set(index, null);
                        }
                    }
                }

//                System.out.println("[Child listener] meal removed "+name);
                if(refreshers.size()!=0)
                    refreshers.get(refreshers.size()-1).runnable.run();
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
//                System.out.println("[child listener] MOVED!");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                System.out.println("[child listener] CANCELLED!");
            }
        });
    }
    public void popRefresher() {
        if(!initialized) {
            throw new RuntimeException("Call initDBConnection before pushing refresher");
        }
        Refresher refresher = refreshers.get(refreshers.size()-1);
        refresher.ref.removeEventListener(refresher.listener);
        refreshers.remove(refreshers.size()-1);
    }

    public void refreshMealDays(int mealPlanId, Runnable runnable) {
        if(!initialized) return;
        if(mealPlanId == 0) return;
        int year = 2023; // TODO: Don't hardcode
        int days = 365; // TODO: Days should be calculated based on the year

        MealPlan plan = getMealPlan(mealPlanId);
        // TODO: Save to database before clearing days?
//        plan.days.clear();
//        for(int i=0;i<days;i++) {
//            plan.days.add(new MealDay());
//        }
        runnable.run();
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
            MealPlan plan = getMealPlan(mealPlanId);
            db_mealPlans.child(plan.key).child("name").setValue(name);
        }
//        MealPlan plan = mealPlans.get(mealPlanId - 1);
//        if(plan == null) return;
//        plan.name = name;
    }

    public int countOfMealPlans(){
        return _mealPlans.size();
    }
    // returns 0 if the slot at index doesn't have a meal plan
    public int mealPlanIdFromIndex(int mealPlanIndex) {
        if(getMealPlan(mealPlanIndex+1) == null)
            return 0;
        return mealPlanIndex + 1;
    }
    public void addMealPlan(String name) {
        if(useDatabase) {
            DatabaseReference planReference = db_mealPlans.push();
//            String key = db_mealPlans.push().getKey();
            planReference.child("name").setValue(name);
        }
//        System.out.println("Add meal plan");
//        int id = mealPlanIds.size() + 1;
//        mealPlanIds.add(id);
//        MealPlan plan = new MealPlan();
//        plan.name = name;
//        mealPlans.put(id, plan);
    }
    public String nameOfMealPlan(int mealPlanId) {
        MealPlan plan = getMealPlan(mealPlanId);
        if(plan == null) {
            return "";
        }
        return plan.name;
    }
//    public int countOfMealDays(int mealPlanId) {
//        return mealPlans.get(mealPlanId-1).days.size();
//    }
    public int countOfMeals(int mealPlanId, int dayIndex) {
        MealPlan plan = getMealPlan(mealPlanId);
        if(plan == null)
            return 0;
        MealDay day = plan.getDay(dayIndex);
        if(day == null)
            return 0;
        return day.meals.size();
    }
    public boolean isMealIndexValid(int mealPlanId, int dayIndex, int mealIndex) {
        MealPlan plan = getMealPlan(mealPlanId);
        if(plan == null)
            return false;
        MealDay day = plan.getDay(dayIndex);
        if(day == null)
            return false;
        if(day.meals.get(mealIndex) == null)
            return false;
        return true;
    }
    public String nameOfMeal(int mealPlanId, int dayIndex, int mealIndex) {
//        int year = 2023; // TODO: Don't hardcode
//        MealPlan plan = getMealPlan(mealPlanId);
//        if(plan == null)
//            return;
//        MealDay day = plan.getDay(dayIndex);
//        if(day == null)
//            return;
//        Meal meal = day.meals.get(mealIndex);
//        if(meal == null)
//            return;
        return getMealPlan(mealPlanId).getDay(dayIndex).meals.get(mealIndex).name;
    }
    public int hourOfMeal(int mealPlanId, int dayIndex, int mealIndex) {
//        int year = 2023; // TODO: Don't hardcode
//        MealPlan plan = getMealPlan(mealPlanId);
//        if(plan == null)
//            return 12;
//        MealDay day = plan.getDay(dayIndex);
//        if(day == null)
//            return 12;
//        Meal meal = day.meals.get(mealIndex);
//        if(meal == null)
//            return 12;
        return getMealPlan(mealPlanId).getDay(dayIndex).meals.get(mealIndex).hour;
    }
    public int minuteOfMeal(int mealPlanId, int dayIndex, int mealIndex) {
        return getMealPlan(mealPlanId).getDay(dayIndex).meals.get(mealIndex).minute;
    }
    public String descriptionOfMeal(int mealPlanId, int dayIndex, int mealIndex) {
        return getMealPlan(mealPlanId).getDay(dayIndex).meals.get(mealIndex).desc;
    }
    public void addMeal(int mealPlanId, int mealDayIndex, String name) {
        if(useDatabase){
            int year = 2023;
            MealPlan plan = getMealPlan(mealPlanId);
            if(plan == null)
                return;
            DatabaseReference mealReference = db_mealPlans.child(plan.key).child("years").child(""+year).child(""+(mealDayIndex)).push();
            mealReference.child("name").setValue(name);
            mealReference.child("hour").setValue(12);
            mealReference.child("minute").setValue(0);
            mealReference.child("desc").setValue("");
        }
//        Meal meal = new Meal();
//        meal.desc = "";
//        meal.name = name;
//        mealPlans.get(mealPlanId-1).days.get(mealDayIndex).meals.add(meal);
    }
    public void deleteMeal(int mealPlanId, int dayIndex, int mealIndex){
        if(useDatabase){
            int year = 2023; // TODO: Don't hardcode
            MealPlan plan = getMealPlan(mealPlanId);
            if(plan == null)
                return;
            MealDay day = plan.getDay(dayIndex);
            if(day == null)
                return;
            Meal meal = day.meals.get(mealIndex);
            if(meal == null)
                return;
            db_mealPlans.child(plan.key).child("years").child("" + year).child("" + (dayIndex)).child(meal.key).removeValue();
        }
//        mealPlans.get(mealPlanId-1).days.get(mealDayIndex).meals.remove(mealIndex);
    }
    public void deleteMealPlan(int mealPlanId){
        if(useDatabase){
            MealPlan plan = getMealPlan(mealPlanId);
            if(plan != null) {
                db_mealPlans.child(plan.key).removeValue();
            }
        }
//        mealPlans.remove(mealPlanId-1);
    }
    public void setNameOfMeal(int mealPlanId, int dayIndex, int mealIndex, String name) {
        if(useDatabase) {
            int year = 2023; // TODO: Don't hardcode
            MealPlan plan = getMealPlan(mealPlanId);
            if(plan == null)
                return;
            MealDay day = plan.getDay(dayIndex);
            if(day == null)
                return;
            Meal meal = day.meals.get(mealIndex);
            if(meal == null)
                return;
            db_mealPlans.child(plan.key).child("years").child(""+year).child(""+(dayIndex)).child(meal.key).child("name").setValue(name);
        }
//        mealPlans.get(mealPlanId-1).days.get(mealDayIndex).meals.get(mealIndex).name = name;
    }
    public void setHourOfMeal(int mealPlanId, int dayIndex, int mealIndex, int hour) {
        if(useDatabase) {
            int year = 2023; // TODO: Don't hardcode
            MealPlan plan = getMealPlan(mealPlanId);
            if(plan == null)
                return;
            MealDay day = plan.getDay(dayIndex);
            if(day == null)
                return;
            Meal meal = day.meals.get(mealIndex);
            if(meal == null)
                return;
            db_mealPlans.child(plan.key).child("years").child(""+year).child(""+(dayIndex)).child(meal.key).child("hour").setValue(hour);
        }
//        mealPlans.get(mealPlanId-1).days.get(mealDayIndex).meals.get(mealIndex).hour = hour;
    }
    public void setMinuteOfMeal(int mealPlanId, int dayIndex, int mealIndex, int minute){
        if(useDatabase) {
            int year = 2023; // TODO: Don't hardcode
            MealPlan plan = getMealPlan(mealPlanId);
            if(plan == null)
                return;
            MealDay day = plan.getDay(dayIndex);
            if(day == null)
                return;
            Meal meal = day.meals.get(mealIndex);
            if(meal == null)
                return;
            db_mealPlans.child(plan.key).child("years").child(""+year).child(""+(dayIndex)).child(meal.key).child("minute").setValue(minute);
        }
//        mealPlans.get(mealPlanId-1).days.get(mealDayIndex).meals.get(mealIndex).minute = minute;
    }
    public void setDescriptionOfMeal(int mealPlanId, int dayIndex, int mealIndex, String description){
        if(useDatabase) {
            int year = 2023; // TODO: Don't hardcode
            MealPlan plan = getMealPlan(mealPlanId);
            if(plan == null)
                return;
            MealDay day = plan.getDay(dayIndex);
            if(day == null)
                return;
            Meal meal = day.meals.get(mealIndex);
            if(meal == null)
                return;
            db_mealPlans.child(plan.key).child("years").child(""+year).child(""+(dayIndex)).child(meal.key).child("desc").setValue(description);
        }
//        mealPlans.get(mealPlanId-1).days.get(mealDayIndex).meals.get(mealIndex).desc = description;
    }
}