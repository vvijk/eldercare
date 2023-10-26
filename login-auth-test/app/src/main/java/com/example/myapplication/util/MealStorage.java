package com.example.myapplication.util;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/*
    This class provides ways of accessing meals from the database.
    There only needs to be one instance per android application.
    The class caches results from the database and has listeners which automatically update the cached results.
    You can pass a callback (Runnable) to the functions called pushRefresher which will run the callback when the cached results change.
*/
public class MealStorage {
    public class Caregiver {
        String UID;
        ArrayList<Integer> caretakers = new ArrayList<>(); // in the form of ids/indexes
        MealDay templateMeal = new MealDay();

        public void addCaretaker(int caretakerId) {
            int index = caretakers.indexOf(caretakerId);
            if(index == -1) {
                caretakers.add(caretakerId);
            }
        }
        public void removeCaretaker(int caretakerId) {
            caretakers.remove((Integer)caretakerId);
        }
        public int getCaretakerByIndex(int index) {
            if(caretakers.size() <= index || index < 0) {
                return -1;
            }
            return caretakers.get(index);
        }
        public int countOfCaretakers() {
            return caretakers.size();
        }
    }
    public class Meal {
        public Meal(String name, int hour, int minute) {
            this.name = name;
            this.hour = hour;
            this.minute = minute;
        }
        public Meal() {} // default
        public String key;
        public String name=""; // lunch, breakfast...
        public int hour=12;
        public int minute=0;
        public String desc="";
        public boolean eaten=false;
    }
    public class MealDay {
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
        public void removeMeal(String key) {
            int index = indexOf(key);
            if(index == -1)
                return;
            meals.set(index, null);
        }
        public int mealCount() {
            return meals.size();
        }
        public @Nullable Meal getMeal(int index) {
            return meals.get(index);
        }
    }
    public class Caretaker {
        String name = "";
        String UID;

        private MealDay[] days = new MealDay[7];

        public MealDay getDay(int dayIndex) {
            if(days.length <= dayIndex || dayIndex < 0)
                return null;
            if(days[dayIndex] == null) {
                days[dayIndex] = new MealDay();
            }
            return days[dayIndex];
        }
        public void setDay(int dayIndex, MealDay mealDay) {
            days[dayIndex] = mealDay;
        }
    }

    public MealStorage() {
    }

    private boolean useDatabase = true;
    private boolean initialized = false;
    public void initDBConnection() {
        if(initialized)
            return;
        initialized = true;
        // TODO(Emarioo): Should the API to the database exist in one class (dbLibrary) or multiple (dbLibrary and MealStorage)?
        //  Firebase database has some listeners and what not so perhaps we should use Firebase directly without an API.
        //  In some cases, like meal management, it is nice to have an API which handles the database stuff. It depends if you
        //  want listeners on the text views to automatically update the UI (which I think you can do?).
        db_meals = FirebaseDatabase.getInstance().getReference("meals");
        db_caregivers = FirebaseDatabase.getInstance().getReference("users/caregivers");
        db_caretakers = FirebaseDatabase.getInstance().getReference("users/caretakers");
    }

    public DatabaseReference db_meals;
    public DatabaseReference db_caregivers;
    public DatabaseReference db_caretakers;

    private ArrayList<Caregiver> caregivers = new ArrayList<>();
    private HashMap<String, Caregiver> caregivers_by_UID = new HashMap<>();
    private int addCaregiver(String UID) {
        Caregiver caregiver = new Caregiver();
        caregiver.templateMeal = new MealDay();
        caregiver.UID = UID;
        int index = caregivers.indexOf(null);
        if(index != -1) {
            caregivers.set(index, caregiver);
            caregivers_by_UID.put(UID, caregiver);
            return index;
        }
        caregivers.add(caregiver);
        caregivers_by_UID.put(UID, caregiver);
        return caregivers.size()-1;
    }
    private Caregiver getCaregiver(int caregiverId) {
        if(caregivers.size() <= caregiverId || caregiverId < 0)
            return null;
        return caregivers.get(caregiverId);
    }
    private Caregiver getCaregiver(String UID) {
        return caregivers_by_UID.get(UID);
    }
    private int addCaretaker(String UID) {
        Caretaker caretaker = new Caretaker();
        caretaker.UID = UID;
        int index = caretakers.indexOf(null);
        if(index != -1) {
            caretakers.set(index, caretaker);
            caretakers_by_UID.put(UID, caretaker);
            return index;
        }
        caretakers.add(caretaker);
        caretakers_by_UID.put(UID, caretaker);
        return caretakers.size()-1;
    }
    private Caretaker getCaretaker(int caretakerId) {
        if(caretakers.size() <= caretakerId || caretakerId < 0)
            return null;
        return caretakers.get(caretakerId);
    }
    private Caretaker getCaretaker(String UID) {
        return caretakers_by_UID.get(UID);
    }
    private int idFromCaretaker(Caretaker caretaker) {
        return caretakers.indexOf(caretaker);
    }

    private ArrayList<Caretaker> caretakers = new ArrayList<Caretaker>();
    private HashMap<String, Caretaker> caretakers_by_UID = new HashMap<>();

    class Refresher {
        Runnable runnable;
        ChildEventListener listener_child;
        ValueEventListener listener_value;
        DatabaseReference ref; // listener must be removed from ref
    }
    abstract class ValueListener implements ValueEventListener {
        public ValueListener(Object extraData) {
            this.extraData = extraData;
        }
        Object extraData;
    }
    private ArrayList<ArrayList<Refresher>> refresher_stack = new ArrayList<>();
    public void pushRefresher_caregiver(int caregiverId, Runnable runnable) {
        if (!initialized) {
            throw new RuntimeException("Call initDBConnection before pushing refresher");
        }
        Caregiver caregiver = getCaregiver(caregiverId);
        if (caregiver == null) {
            throw new RuntimeException("Caregiver was null");
        }
        ArrayList<Refresher> refreshers = new ArrayList<>();
        refresher_stack.add(refreshers);

        {
            Refresher refresher = new Refresher();
            refreshers.add(refresher);
            refresher.runnable = runnable;
            refresher.ref = db_meals.child(caregiver.UID).child("templateMeal");
            // other listeners won't execute if there are zero meals. That's why we add this one.
            refresher.ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    if(count == 0) {
                        if (refreshers.size() != 0)
                            refreshers.get(refreshers.size() - 1).runnable.run();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            refresher.listener_child = refresher.ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String mealKey = snapshot.getKey();
                    String UID = snapshot.getRef().getParent().getParent().getKey();
                    Caregiver caregiver = getCaregiver(UID);
                    if (caregiver == null) {
                        int id = addCaregiver(UID);
                        caregiver = getCaregiver(id);
                    }

                    Meal meal = caregiver.templateMeal.getMeal(mealKey);
                    if (meal == null) {
                        meal = new Meal();
                        meal.key = mealKey;
                        caregiver.templateMeal.addMeal(meal);
                    }

                    Object tmp = snapshot.child("name").getValue(String.class);
                    if (tmp != null)
                        meal.name = (String) tmp;
                    tmp = snapshot.child("hour").getValue(Integer.class);
                    if (tmp != null)
                        meal.hour = (Integer) tmp;
                    tmp = snapshot.child("minute").getValue(Integer.class);
                    if (tmp != null)
                        meal.minute = (Integer) tmp;
                    tmp = snapshot.child("desc").getValue(String.class);
                    if (tmp != null)
                        meal.desc = (String) tmp;
                    tmp = snapshot.child("eaten").getValue(Boolean.class);
                    if (tmp != null)
                        meal.eaten = (Boolean) tmp;

    //                System.out.println("[Child listener] plan added "+name);

                    if (refreshers.size() != 0) refreshers.get(refreshers.size() - 1).runnable.run();
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String mealKey = snapshot.getKey();
                    String UID = snapshot.getRef().getParent().getParent().getKey();
                    Caregiver caregiver = getCaregiver(UID);
                    if (caregiver == null) {
                        int id = addCaregiver(UID);
                        caregiver = getCaregiver(id);
                    }

                    Meal meal = caregiver.templateMeal.getMeal(mealKey);
                    if (meal == null) {
                        meal = new Meal();
                        meal.key = mealKey;
                        caregiver.templateMeal.addMeal(meal);
                    }

                    Object tmp = snapshot.child("name").getValue(String.class);
                    if (tmp != null)
                        meal.name = (String) tmp;
                    tmp = snapshot.child("hour").getValue(Integer.class);
                    if (tmp != null)
                        meal.hour = (Integer) tmp;
                    tmp = snapshot.child("minute").getValue(Integer.class);
                    if (tmp != null)
                        meal.minute = (Integer) tmp;
                    tmp = snapshot.child("desc").getValue(String.class);
                    if (tmp != null)
                        meal.desc = (String) tmp;

    //                System.out.println("[Child listener] plan added "+name);

                    if (refreshers.size() != 0) refreshers.get(refreshers.size() - 1).runnable.run();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String mealKey = snapshot.getKey();
                    String UID = snapshot.getRef().getParent().getParent().getKey();
                    Caregiver caregiver = getCaregiver(UID);
                    if (caregiver == null) {
                        int id = addCaregiver(UID);
                        caregiver = getCaregiver(id);
                    }
                    caregiver.templateMeal.removeMeal(mealKey);

    //                System.out.println("[Child listener] plan added "+name);

                    if (refreshers.size() != 0) refreshers.get(refreshers.size() - 1).runnable.run();
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
        {
            Refresher refresher = new Refresher();
            refreshers.add(refresher);
            refresher.runnable = runnable;
            refresher.ref = db_caregivers.child(caregiver.UID).child("caretakers");
            refresher.listener_child = refresher.ref.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    onChildChanged(snapshot, previousChildName);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    String caregiver_UID = snapshot.getRef().getParent().getParent().getKey();
                    if(caregiver_UID == null) return;
                    String caretaker_UID = snapshot.getKey();
                    // NOTE(Emarioo): isValid refers to the boolean in caregivers/<UID>/caretakers/<UID>:boolean
                    //  true means that the caregiver can see caretaker, false means the opposite. UID not being in
                    //  the list also means that caregiver can't see the caretaker.
                    Boolean isValid = snapshot.getValue(Boolean.class);
                    if(caretaker_UID == null) return;
                    Caregiver caregiver = getCaregiver(caregiver_UID);
                    if (caregiver == null) {
                        int id = addCaregiver(caregiver_UID);
                        caregiver = getCaregiver(id);
                    }

                    Caretaker caretaker = getCaretaker(caretaker_UID);
                    int caretaker_id = 0;
                    if (caretaker == null && isValid) {
                        caretaker_id = addCaretaker(caretaker_UID);
                        caretaker = getCaretaker(caretaker_id);
                        caregiver.addCaretaker(caretaker_id);
                        db_caretakers.child(caretaker_UID).addListenerForSingleValueEvent(new ValueListener(caretaker) {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String firstname = snapshot.child("firstName").getValue(String.class);
                                String lastname = snapshot.child("lastName").getValue(String.class);
                                Caretaker caretaker = (Caretaker)extraData;
                                caretaker.name = firstname + " "+lastname;

                                if (refreshers.size() != 0)
                                    refreshers.get(refreshers.size() - 1).runnable.run();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                if (refreshers.size() != 0)
                                    refreshers.get(refreshers.size() - 1).runnable.run();
                            }
                        });
                        // NOTE(Emarioo): As I have understood it, addListenerForSingeValueEvent does not need to be removed.
                        //   It will be called once and then deleted. If not then this is a memory leak of listeners.
                    } else {
                        caretaker_id = idFromCaretaker(caretaker);
                        if(isValid) {
                            caregiver.addCaretaker(caretaker_id);
                        } else {
                            caregiver.removeCaretaker(caretaker_id);
                        }
                        if (refreshers.size() != 0)
                            refreshers.get(refreshers.size() - 1).runnable.run();
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String caregiver_UID = snapshot.getRef().getParent().getParent().getKey();
                    if(caregiver_UID == null) return;
                    String caretaker_UID = snapshot.getKey();
                    Boolean isValid = snapshot.getValue(Boolean.class);
                    if(caretaker_UID == null) return;
                    Caregiver caregiver = getCaregiver(caregiver_UID);
                    if (caregiver == null) {
                        int id = addCaregiver(caregiver_UID);
                        caregiver = getCaregiver(id);
                    }

                    Caretaker caretaker = getCaretaker(caretaker_UID);
                    int caretaker_id = 0;
                    if (caretaker != null) {
                        caretaker_id = idFromCaretaker(caretaker);
                        caregiver.removeCaretaker(caretaker_id);
                    }

                    if (refreshers.size() != 0)
                        refreshers.get(refreshers.size() - 1).runnable.run();
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
    }
    public void pushRefresher_caretaker(int caretakerId, Runnable runnable) {
        if (!initialized) {
            System.out.println("Call initDBConnection before pushing refresher");
            return;
            // throw new RuntimeException("Call initDBConnection before pushing refresher");
        }
        Caretaker caretaker = getCaretaker(caretakerId);
        if (caretaker == null) {
            throw new RuntimeException("Caregiver was null");
        }
        ArrayList<Refresher> refreshers = new ArrayList<>();
        refresher_stack.add(refreshers);
        {
            Refresher refresher = new Refresher();
            refreshers.add(refresher);
            refresher.runnable = runnable;
            refresher.ref = db_meals.child(caretaker.UID);
            refresher.ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long count = snapshot.getChildrenCount();
                    if(count == 0) {
                        if (refreshers.size() != 0)
                            refreshers.get(refreshers.size() - 1).runnable.run();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            refresher.listener_value = refresher.ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String caretaker_UID = snapshot.getKey();
                    Caretaker caretaker = getCaretaker(caretaker_UID);
                    if (caretaker == null) {
                        throw new RuntimeException("Caretaker was null, should be impossible.");
                        // pushRefresher takes caretakerId as argument which means that the caretaker exists and getCaretaker shouldn't return null.
                    }

                    boolean[] days_to_remove = new boolean[7]; // Seven days in a week
                    for(int i=0;i<days_to_remove.length;i++)
                        days_to_remove[i] = true;

                    Iterator<DataSnapshot> day_iterator = snapshot.getChildren().iterator();
                    while(day_iterator.hasNext()) {
                        DataSnapshot daySnapshot = day_iterator.next();
                        int weekDayIndex = refday(daySnapshot.getKey());

                        if(weekDayIndex == -1)
                            continue;
                        MealDay mealDay = caretaker.getDay(weekDayIndex);
                        if(mealDay == null) {
                            throw new RuntimeException("Meal day was null, day index: "+weekDayIndex);
                        } else {
                            days_to_remove[weekDayIndex] = false;
                        }

                        // TODO: Optimize, don't clone meals
                        ArrayList<Meal> meals_to_remove = (ArrayList<Meal>) mealDay.meals.clone();

                        Iterator<DataSnapshot> iterator = daySnapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            DataSnapshot childSnapshot = iterator.next();
                            String mealKey = childSnapshot.getKey();

                            Meal meal = mealDay.getMeal(mealKey);
                            if (meal == null) {
                                meal = new Meal();
                                meal.key = mealKey;
                                mealDay.addMeal(meal);
                            } else {
                                meals_to_remove.remove(meal);
                            }
                            Object tmp = childSnapshot.child("name").getValue(String.class);
                            if (tmp != null)
                                meal.name = (String) tmp;
                            tmp = childSnapshot.child("desc").getValue(String.class);
                            if (tmp != null)
                                meal.desc = (String) tmp;
                            tmp = childSnapshot.child("hour").getValue(Integer.class);
                            if (tmp != null)
                                meal.hour = (Integer) tmp;
                            tmp = childSnapshot.child("minute").getValue(Integer.class);
                            if (tmp != null)
                                meal.minute = (Integer) tmp;
                            tmp = childSnapshot.child("eaten").getValue(Boolean.class);
                            if (tmp != null)
                                meal.eaten = (Boolean) tmp;
                        }

                        for(Meal meal : meals_to_remove) {
                            if(meal != null)
                                mealDay.removeMeal(meal.key);
                        }
                    }
                    for(int i=0;i<days_to_remove.length;i++) {
                        if(days_to_remove[i]) {
                            caretaker.setDay(i,null);
                        }
                    }

                    if (refreshers.size() != 0)
                        refreshers.get(refreshers.size() - 1).runnable.run();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    //                System.out.println("[child listener] CANCELLED!");
                }
            });
        }
    }
    public void popRefresher() {
        if(!initialized) {
            throw new RuntimeException("Call initDBConnection before pushing refresher");
        }
        ArrayList<Refresher> refreshers = refresher_stack.get(refresher_stack.size()-1);
        for(Refresher refresher : refreshers) {
            if(refresher.listener_value!=null)
                refresher.ref.removeEventListener(refresher.listener_value);
            if(refresher.listener_child!=null)
                refresher.ref.removeEventListener(refresher.listener_child);
        }
        refresher_stack.remove(refresher_stack.size()-1);
    }
    // adds a new caregiver if UID isn't cached
    public int idFromCaregiverUID(String UID) {
        Caregiver caregiver = caregivers_by_UID.get(UID);
        if(caregiver == null) {
            return addCaregiver(UID);
        } else {
            return caregivers.indexOf(caregiver);
        }
    }
    public int idFromCaretakerUID(String UID) {
        Caretaker caretaker = caretakers_by_UID.get(UID);
        if(caretaker == null) {
            return addCaretaker(UID);
        } else {
            return caretakers.indexOf(caretaker);
        }
    }
    public int caretakerCountOfCaregiver(int caregiverId) {
        Caregiver caregiver = caregivers.get(caregiverId);
        if(caregiver == null)
            return 0;
        return caregiver.countOfCaretakers();
    }
    // returns 0 if something failed
    public int caretakerIdFromIndex(int caregiverId, int caretakerIndex) {
        Caregiver caregiver = caregivers.get(caregiverId);
        if(caregiver == null)
            return 0;
        return caregiver.getCaretakerByIndex(caretakerIndex);
    }
    public String UIDOfCaretaker(int caretakerId) {
        Caretaker caretaker = caretakers.get(caretakerId);
        if(caretaker == null)
            return "";
        return caretaker.UID;
    }
    public String UIDOfCaregiver(int caregiverId) {
        Caregiver caregiver = caregivers.get(caregiverId);
        if(caregiver == null)
            return "";
        return caregiver.UID;
    }
    // returns null if recipientId was invalid
    public String nameOfCaretaker(int caretakerId) {
        Caretaker caretaker = caretakers.get(caretakerId);
        if(caretaker == null)
            return "";
        return caretaker.name;
    }
    public int caregiver_template_countOfMeals(int caregiverId) {
        Caregiver caregiver = getCaregiver(caregiverId);
        if(caregiver == null)
            return 0;
        if(caregiver.templateMeal == null)
            return 0;
        return caregiver.templateMeal.meals.size();
    }
    public boolean caregiver_template_isMealIndexValid(int caregiverId, int mealIndex) {
        Caregiver caregiver = getCaregiver(caregiverId);
        if(caregiver == null)
            return false;
        MealDay day = caregiver.templateMeal;
        if(day == null)
            return false;
        if(day.meals.get(mealIndex) == null)
            return false;
        return true;
    }
    public String caregiver_template_nameOfMeal(int caregiverId, int mealIndex) {
        Caregiver caregiver = getCaregiver(caregiverId);
        if(caregiver == null)
            return "";
        Meal meal = caregiver.templateMeal.getMeal(mealIndex);
        if(meal == null)
            return "";
        return meal.name;
    }
    public int caregiver_template_hourOfMeal(int caregiverId, int mealIndex) {
        Caregiver caregiver = getCaregiver(caregiverId);
        if(caregiver == null)
            return 12;
        Meal meal = caregiver.templateMeal.getMeal(mealIndex);
        if(meal == null)
            return 12;
        return meal.hour;
    }
    public int caregiver_template_minuteOfMeal(int caregiverId, int mealIndex) {
        Caregiver caregiver = getCaregiver(caregiverId);
        if(caregiver == null)
            return 0;
        Meal meal = caregiver.templateMeal.getMeal(mealIndex);
        if(meal == null)
            return 0;
        return meal.minute;
    }
    public String caregiver_template_descriptionOfMeal(int caregiverId, int mealIndex) {
        Caregiver caregiver = getCaregiver(caregiverId);
        if(caregiver == null)
            return "";
        Meal meal = caregiver.templateMeal.getMeal(mealIndex);
        if(meal == null)
            return "";
        return meal.desc;
    }
    public void caregiver_template_addMeal(int caregiverId, String name) {
        if(useDatabase){
            Caregiver caregiver = getCaregiver(caregiverId);
            if(caregiver == null)
                return;
            DatabaseReference mealReference = db_meals.child(caregiver.UID).child("templateMeal").push();
            mealReference.child("name").setValue(name);
            mealReference.child("hour").setValue(12);
            mealReference.child("minute").setValue(0);
            mealReference.child("desc").setValue("");
        }
    }
    public void caregiver_template_deleteMeal(int caregiverId, int mealIndex){
        if(useDatabase){
            Caregiver caregiver = getCaregiver(caregiverId);
            if(caregiver == null)
                return;
            MealDay day = caregiver.templateMeal;
            if(day == null)
                return;
            Meal meal = caregiver.templateMeal.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caregiver.UID).child("templateMeal").child(meal.key).removeValue();
        }
    }
    public int todaysDayIndex() {
        Calendar calendar = Calendar.getInstance(Locale.UK);
        int sunday_first_weekDayIndex = calendar.get(Calendar.DAY_OF_WEEK)-1;
        int weekDayIndex =  (7 + sunday_first_weekDayIndex - 1) % 7;
        return weekDayIndex;
    }
    public void caretaker_replaceMealsWithTemplate(int caretakerId, int dayIndex, int caregiverId) {
        Caregiver caregiver = getCaregiver(caregiverId);
        Caretaker caretaker = getCaretaker(caretakerId);

        // db_meals.child(caretaker.UID).child(dayref(dayIndex)).removeValue();
        db_meals.child(caregiver.UID).child("templateMeal").addListenerForSingleValueEvent(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                db_meals.child(caretaker.UID).child(dayref(dayIndex)).setValue(snapshot.getValue());
                for(DataSnapshot snap : snapshot.getChildren()) {
                    db_meals.child(caretaker.UID).child(dayref(dayIndex)).child(snap.getKey()).child("eaten").setValue(false);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void caregiver_template_setNameOfMeal(int caregiverId, int mealIndex, String name) {
        if(useDatabase) {
            Caregiver caregiver = getCaregiver(caregiverId);
            if(caregiver == null)
                return;
            Meal meal = caregiver.templateMeal.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caregiver.UID).child("templateMeal").child(meal.key).child("name").setValue(name);
        }
    }
    public void caregiver_template_setHourOfMeal(int caregiverId, int mealIndex, int hour) {
        if(useDatabase) {
            Caregiver caregiver = getCaregiver(caregiverId);
            if(caregiver == null)
                return;
            Meal meal = caregiver.templateMeal.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caregiver.UID).child("templateMeal").child(meal.key).child("hour").setValue(hour);
        }
    }
    public void caregiver_template_setMinuteOfMeal(int caregiverId, int mealIndex, int minute){
        if(useDatabase) {
            Caregiver caregiver = getCaregiver(caregiverId);
            if(caregiver == null)
                return;
            Meal meal = caregiver.templateMeal.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caregiver.UID).child("templateMeal").child(meal.key).child("minute").setValue(minute);
        }
    }
    public void caregiver_template_setDescriptionOfMeal(int caregiverId, int mealIndex, String description){
        if(useDatabase) {
            Caregiver caregiver = getCaregiver(caregiverId);
            if(caregiver == null)
                return;
            Meal meal = caregiver.templateMeal.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caregiver.UID).child("templateMeal").child(meal.key).child("desc").setValue(description);
        }
    }
    public int caretaker_countOfMeals(int caretakerId, int weekDay) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return 0;
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return 0;
        return day.meals.size();
    }
    private int[] getSortedMealIndexList(MealDay day) {
        int mealCount = day.meals.size();

        int[] sortedMeals_index = new int[mealCount];
        int[] sortedMeals_time = new int[mealCount];
        int usedCount = 0;

        for(int mealIndex=0;mealIndex<mealCount;mealIndex++) {
            Meal meal = day.getMeal(mealIndex);
            if(meal == null)
                continue;
            int hour = meal.hour;
            int minute = meal.minute;
            sortedMeals_time[usedCount] = hour*100+minute;
            sortedMeals_index[usedCount] = mealIndex;
            usedCount++;
        }
        if(usedCount == 0)
            return new int[0];

        // TODO(Emarioo): Don't use bubble sort, you are better than this
        for(int i=0;i<usedCount;i++) {
            boolean swapped = false;
            for(int j=0;j<usedCount - 1 - i;j++) {
                if (sortedMeals_time[j+1] < sortedMeals_time[j]) {
                    int tmp = sortedMeals_time[j];
                    sortedMeals_time[j] = sortedMeals_time[j+1];
                    sortedMeals_time[j+1] = tmp;
                    tmp = sortedMeals_index[j];
                    sortedMeals_index[j] = sortedMeals_index[j+1];
                    sortedMeals_index[j+1] = tmp;
                    swapped = true;
                }
            }
            if(!swapped)
                break;
        }
        int[] out = new int[usedCount];
        System.arraycopy(sortedMeals_index, 0, out, 0, usedCount);
        return out;
    }
    public int[] caregiver_template_sortedMealIndices(int caregiverId) {
        Caregiver caregiver = getCaregiver(caregiverId);
        if(caregiver == null)
            return new int[0];
        MealDay day = caregiver.templateMeal;
        if(day == null)
            return new int[0];
        return getSortedMealIndexList(day);
    }
    public int[] caretaker_sortedMealIndices(int caretakerId, int weekDay) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return new int[0];
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return new int[0];
        return getSortedMealIndexList(day);
    }
    public Meal caretaker_getMeal(int caretakerId, int weekDay, int mealIndex) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return null;
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return null;
        return day.meals.get(mealIndex);
    }
    public boolean caretaker_isMealIndexValid(int caretakerId, int weekDay, int mealIndex) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return false;
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return false;
        if(day.meals.get(mealIndex) == null)
            return false;
        return true;
    }
    public String caretaker_nameOfMeal(int caretakerId, int weekDay, int mealIndex) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return "";
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return "";
        Meal meal = day.getMeal(mealIndex);
        if(meal == null)
            return "";
        return meal.name;
    }
    public int caretaker_hourOfMeal(int caretakerId, int weekDay, int mealIndex) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return 12;
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return 12;
        Meal meal = day.getMeal(mealIndex);
        if(meal == null)
            return 12;
        return meal.hour;
    }
    public int caretaker_minuteOfMeal(int caretakerId, int weekDay, int mealIndex) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return 0;
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return 0;
        Meal meal = day.getMeal(mealIndex);
        if(meal == null)
            return 0;
        return meal.minute;
    }
    public boolean caretaker_eatenOfMeal(int caretakerId, int weekDay, int mealIndex) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return false;
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return false;
        Meal meal = day.getMeal(mealIndex);
        if(meal == null)
            return false;
        return meal.eaten;
    }
    public String caretaker_descriptionOfMeal(int caretakerId, int weekDay, int mealIndex) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return "";
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return "";
        Meal meal = day.getMeal(mealIndex);
        if(meal == null)
            return "";
        return meal.desc;
    }
    public String caretaker_keyOfMeal(int caretakerId, int weekDay, int mealIndex) {
        Caretaker caretaker = getCaretaker(caretakerId);
        if(caretaker == null)
            return "";
        MealDay day = caretaker.days[weekDay];
        if(day == null)
            return "";
        Meal meal = day.getMeal(mealIndex);
        if(meal == null)
            return "";
        return meal.key;
    }
    private String weekDays[] = { "mon", "tue", "wed", "thu", "fri", "sat", "sun" };
    public String dayref(int index) {
        // return ""+index;
        return weekDays[index];
    }
    public int refday(String key) {
        for(int i=0;i<weekDays.length;i++) {
            if(weekDays[i].equals(key))
                return i;
        }
        return -1;
        // return Integer.parseInt(key);
    }
    public void caretaker_addMeal(int caretakerId, int weekDay, String name) {
        if(useDatabase){
            Caretaker caretaker = getCaretaker(caretakerId);
            if(caretaker == null)
                return;
            DatabaseReference mealReference = db_meals.child(caretaker.UID).child(dayref(weekDay)).push();
            mealReference.child("name").setValue(name);
            mealReference.child("hour").setValue(12);
            mealReference.child("minute").setValue(0);
            mealReference.child("desc").setValue("");
            mealReference.child("eaten").setValue(false);
        }
    }
    public void caretaker_deleteMeal(int caretakerId, int weekDay, int mealIndex){
        if(useDatabase){
            Caretaker caretaker = getCaretaker(caretakerId);
            if(caretaker == null)
                return;
            MealDay day = caretaker.days[weekDay];
            if(day == null)
                return;
            Meal meal = day.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caretaker.UID).child(dayref(weekDay)).child(meal.key).removeValue();
        }
    }
    public void caretaker_setNameOfMeal(int caretakerId, int weekDay, int mealIndex, String name) {
        if(useDatabase) {
            Caretaker caretaker = getCaretaker(caretakerId);
            if(caretaker == null)
                return;
            MealDay day = caretaker.days[weekDay];
            if(day == null)
                return;
            Meal meal = day.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caretaker.UID).child(dayref(weekDay)).child(meal.key).child("name").setValue(name);
        }
    }
    public void caretaker_setEatenOfMeal(int caretakerId, int weekDay, int mealIndex, boolean eaten) {
        if(useDatabase) {
            Caretaker caretaker = getCaretaker(caretakerId);
            if(caretaker == null)
                return;
            MealDay day = caretaker.days[weekDay];
            if(day == null)
                return;
            Meal meal = day.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caretaker.UID).child(dayref(weekDay)).child(meal.key).child("eaten").setValue(eaten);
        }
    }
    public void caretaker_setEatenOfMeal(int caretakerId, int weekDay, String mealKey, boolean eaten) {
        if(useDatabase) {
            Caretaker caretaker = getCaretaker(caretakerId);
            if(caretaker == null)
                return;
            MealDay day = caretaker.days[weekDay];
            if(day == null)
                return;
            Meal meal = day.getMeal(mealKey);
            if(meal == null)
                return;
            db_meals.child(caretaker.UID).child(dayref(weekDay)).child(meal.key).child("eaten").setValue(eaten);
        }
    }
    public void caretaker_setHourOfMeal(int caretakerId, int weekDay, int mealIndex, int hour) {
        if(useDatabase) {
            Caretaker caretaker = getCaretaker(caretakerId);
            if(caretaker == null)
                return;
            MealDay day = caretaker.days[weekDay];
            if(day == null)
                return;
            Meal meal = day.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caretaker.UID).child(dayref(weekDay)).child(meal.key).child("hour").setValue(hour);
        }
    }
    public void caretaker_setMinuteOfMeal(int caretakerId, int weekDay, int mealIndex, int minute){
        if(useDatabase) {
            Caretaker caretaker = getCaretaker(caretakerId);
            if(caretaker == null)
                return;
            MealDay day = caretaker.days[weekDay];
            if(day == null)
                return;
            Meal meal = day.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caretaker.UID).child(dayref(weekDay)).child(meal.key).child("minute").setValue(minute);
        }
    }
    public void caretaker_setDescriptionOfMeal(int caretakerId, int weekDay, int mealIndex, String description){
        if(useDatabase) {
            Caretaker caretaker = getCaretaker(caretakerId);
            if(caretaker == null)
                return;
            MealDay day = caretaker.days[weekDay];
            if(day == null)
                return;
            Meal meal = day.getMeal(mealIndex);
            if(meal == null)
                return;
            db_meals.child(caretaker.UID).child(dayref(weekDay)).child(meal.key).child("desc").setValue(description);
        }
    }
}