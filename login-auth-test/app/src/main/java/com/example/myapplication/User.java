package com.example.myapplication;

public class User {
    private String firstName, lastName, phoneNr, email, personNummer, prefFood, PIN;
    private boolean careGiver;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public User(String name, String lastname, String phoneNr, String email, String personNummer, String prefFood, String PIN, Boolean careGiver) {
        this.firstName = name;
        this.lastName = lastname;
        this.phoneNr = phoneNr;
        this.email = email;
        this.personNummer = personNummer;
        this.careGiver = careGiver;
        this.prefFood = prefFood;
        this.PIN = PIN;
    }
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
    public String getPersonNummer() { return personNummer; }
    public boolean isCareGiver() { return careGiver; }
}

