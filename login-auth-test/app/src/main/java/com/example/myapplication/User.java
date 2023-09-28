package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String firstName, lastName, phoneNr, email, idNumber, prefFood, PIN;
    private boolean careGiver;
    private List<String> caretakers;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public User(String name, String lastname, String phoneNr, String email, String personNummer, String prefFood, String PIN, Boolean careGiver) {
        this.firstName = name;
        this.lastName = lastname;
        this.phoneNr = phoneNr;
        this.email = email;
        this.idNumber = personNummer;
        this.careGiver = careGiver;
        this.prefFood = prefFood;
        this.PIN = PIN;
        this.caretakers = new ArrayList<>();
    }
    public String getPIN() { return PIN; }
    public String getPrefFood() { return prefFood; }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getPhoneNr() {
        return phoneNr;
    }
    public String getEmail() { return email; }
    public String getIdNumber() { return idNumber; }
    public boolean isCareGiver() { return careGiver; }
    public List<String> getCaretakers() { return caretakers; }
    public void setCaretakers(List<String> caretakers) {
        this.caretakers = caretakers;
    }

}

