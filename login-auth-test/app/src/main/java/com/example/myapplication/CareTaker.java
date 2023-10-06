package com.example.myapplication;

public class CareTaker extends User {
    private String prefFood;
    private String PIN;
    private String handler;

    public CareTaker(String name, String lastname, String phoneNr, String email, String personNummer, String prefFood, String PIN, String handler) {
        super(name, lastname, phoneNr, email, personNummer, false); // Set careGiver to false
        this.prefFood = prefFood;
        this.PIN = PIN;
        this.handler = handler;
    }
    public void setPrefFood(String prefFood) {
        this.prefFood = prefFood;
    }
    public void setPIN(String PIN) {
        this.PIN = PIN;
    }
    public String getPrefFood() {
        return prefFood;
    }
    public String getPIN() {
        return PIN;
    }
    public String getHandler() {
        return handler;
    }
}
