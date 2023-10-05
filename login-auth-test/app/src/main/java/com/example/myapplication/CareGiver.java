package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class CareGiver extends User {
    private List<String> caretakers;

    public CareGiver(String name, String lastname, String phoneNr, String email, String personNummer) {
        super(name, lastname, phoneNr, email, personNummer, true); // Set careGiver to true
        this.caretakers = new ArrayList<>();
    }
    public CareGiver() {
        // Default constructor required by Firebase for deserialization
    }
    public List<String> getCaretakers() {
        return caretakers;
    }
    public void setCaretakers(List<String> caretakers) {
        this.caretakers = caretakers;
    }

}

