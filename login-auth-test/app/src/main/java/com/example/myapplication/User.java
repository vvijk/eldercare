package com.example.myapplication;

import java.util.List;

public class User {
    private String name;
    private String lastname;
    private String phoneNr;
    private String email;
    private String personNummer;
    private boolean careGiver;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public User(String name, String lastname, String phoneNr, String email, String personNummer, Boolean careGiver) {
        this.name = name;
        this.lastname = lastname;
        this.phoneNr = phoneNr;
        this.email = email;
        this.personNummer = personNummer;
        this.careGiver = careGiver;
    }
    public String getName() {
        return name;
    }
    public String getLastname() {
        return lastname;
    }
    public String getPhoneNr() {
        return phoneNr;
    }
    public String getEmail() { return email; }
    public String getPersonNummer() { return personNummer; }
    public boolean isCareGiver() { return careGiver; }
}

