package com.example.myapplication;

import static com.example.myapplication.Helpers.isStringInArray;

import android.annotation.SuppressLint;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LogItem {
    /*
    * This class is the relevant info in each log to be presented.
    * Each patient has a list of logs, where these LogItems are then stored.
    * Caregivers can then view logs related to each patient.
    * */

    //categories, used for identifying log type.
    public static final String PATIENT_ADD = "PATIENT_ADD";
    public static final String PATIENT_MODIFY = "PATIENT_MODIFY";
    public static final String PATIENT_DELETE  = "DELETE_PATIENT"; //kan gå oanvänd, loggar sparas på patient, och om den raderas kommer inte denna log kunna hämtas.
    public static final String EMERGENCY       = "EMERGENCY";
    public static final String MEAL_CONFIRM    = "MEAL_CONFIRM";
    public static final String MEAL_SKIP       = "MEAL_SKIP"; //notification when non-final missed meal.
    public static final String MEAL_MISS       = "MEAL_MISS"; //notification final missed meal.


    public static final String[] TYPE_ALL = {"PATIENT_ADD", "PATIENT_MODIFY", "DELETE_PATIENT", "EMERGENCY", "MEAL_CONFIRM", "MEAL_SKIP", "MEAL_MISS"};
    public static final String[] TYPE_GENERAL  = {"PATIENT_ADD", "PATIENT_MODIFY", "DELETE_PATIENT", "EMERGENCY"};
    public static final String[] TYPE_MEAL  = {"MEAL_CONFIRM", "MEAL_SKIP", "MEAL_MISS"};

    @SuppressLint("SimpleDateFormat")
    protected static final SimpleDateFormat date_format = new SimpleDateFormat("[dd/MM-yy HH:mm:ss]");

    protected CareGiver caregiver;
    protected CareTaker patient;
    protected Timestamp time;
    protected String category;
    protected Meal meal;

    public LogItem(CareGiver caregiver, CareTaker patient, String category){
        /* Creates a log, and adds it to the patients list of logs.
        * patient   : the patient this log concerns.
        * category  : the type of this log, important for constructing log message and filtering.
        */
        if (isStringInArray(category, TYPE_MEAL)){ System.out.print("meal parameter required for this category"); return; }
        setCaregiver(caregiver);
        setPatient(patient);
        setTimestamp();
        setCategory(category);
        setMeal(null);
    }


    public LogItem(CareGiver caregiver, CareTaker patient, String category, Meal meal){
        /* Creates a log, and adds it to the patients list of logs.
        * patient   : the patient this log concerns.
        * category  : the type of this log, important for constructing log message and filtering.
        * meal      : the meal which the patient ate/missed, important so that the message can specify correctly.
        */
        if (isStringInArray(category, TYPE_GENERAL)) { meal = null; }
        setCaregiver(caregiver);
        setPatient(patient);
        setTimestamp();
        setCategory(category);
        setMeal(meal);
    }

    protected void setCaregiver(CareGiver caregiver){ this.caregiver = caregiver; }
    protected void setPatient(CareTaker patient){ this.patient = patient; }
    protected void setTimestamp(){ this.time = new Timestamp(System.currentTimeMillis()); }
    protected void setCategory(String category){ this.category = category; }
    protected void setMeal(Meal meal){ this.meal = meal; }

    protected CareGiver getCaregiver(){ return this.caregiver; }
    protected CareTaker getPatient(){ return this.patient; }
    protected Timestamp getTimestamp(){ return this.time; }
    protected String getFormattedTimestamp(){ return date_format.format(this.time); }
    protected String getCategory(){ return this.category; }
    protected Meal getMeal(){ return this.meal; }

    /*TODO: PLACEHOLDER until meal management is merged.*/
    protected static class Meal{
        protected String name;
        public Meal(String mealName){
            setName(mealName);
        }
        protected void setName(String mealName){ this.name = mealName; }
        protected String getName(){ return this.name; }
    }


}
