package com.example.myapplication;

import static com.example.myapplication.Helpers.isStringInArray;

import android.annotation.SuppressLint;

import com.example.myapplication.util.MealStorage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class LogItem {
    /*
    * This class is the relevant info in each log to be presented.
    * Each patient has a list of logs, where these LogItems are then stored.
    * Caregivers can then view logs related to each patient.
    * */

    //categories, used for identifying log type.

    public static final String PATIENT_CREATE  = "PATIENT_CREATE";
    public static final String PATIENT_ADD     = "PATIENT_ADD";
    public static final String EMERGENCY       = "EMERGENCY";
    public static final String MEAL_ADD        = "MEAL_ADD";
    public static final String MEAL_DELETE     = "MEAL_DELETE";
    public static final String MEAL_CONFIRM    = "MEAL_CONFIRM";
    public static final String MEAL_SKIP       = "MEAL_SKIP"; //notification when non-final missed meal.
    public static final String MEAL_MISS       = "MEAL_MISS"; //notification final missed meal.

    @SuppressLint("SimpleDateFormat")
    protected static final SimpleDateFormat date_format = new SimpleDateFormat("[dd/MM-yy HH:mm:ss]");

    protected CareGiver caregiver;
    protected CareTaker patient;
    protected Timestamp time;
    protected String category;
    protected MealStorage.Meal meal;

    public LogItem(CareGiver caregiver, CareTaker patient, String category, MealStorage.Meal meal){
        /* Creates a log, and adds it to the patients list of logs.
        * patient   : the patient this log concerns.
        * category  : the type of this log, important for constructing log message and filtering.
        * meal      : the meal which the patient ate/missed, important so that the message can specify correctly.
        *
        * Enter null as parameter in builder when that parameter is irrelevant to log printout.
        */
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
    protected void setMeal(MealStorage.Meal meal){ this.meal = meal; }

    protected CareGiver getCaregiver(){ return this.caregiver; }
    protected CareTaker getPatient(){ return this.patient; }
    protected Timestamp getTimestamp(){ return this.time; }
    protected String getFormattedTimestamp(){ return date_format.format(this.time); }
    protected String getCategory(){ return this.category; }
    protected MealStorage.Meal getMeal(){ return this.meal; }

}
