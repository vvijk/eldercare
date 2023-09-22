package com.example.myapplication;

public class User {
    private String name;
    private String lastname;
    private String phoneNr;

    private String email;
    private String personNummer;
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }
    public User(String name, String lastname, String phoneNr, String email, String personNummer) {
        this.name = name;
        this.lastname = lastname;
        this.phoneNr = phoneNr;
        this.email = email;
        this.personNummer = personNummer;
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
}

