package com.example.myapplication.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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
/*
    This class provides ways of accessing an artificial database.
    It's temporary until the database supports patients and meal plans.
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

    private HashMap<Integer, Caregiver> caregivers = new HashMap<>(); // caregiverId -> list of patientIds
    private HashMap<Integer, Patient> patients = new HashMap<>();
    private HashMap<Integer, MealPlan> mealPlans = new HashMap<>();
    private ArrayList<Integer> mealPlanIds = new ArrayList<>();

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

    public int countOfMealPlans(){
        return mealPlanIds.size();
    }
    public int mealPlanIdFromIndex(int mealPlanIndex) {
        return mealPlanIds.get(mealPlanIndex);
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
}