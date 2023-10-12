package com.example.myapplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CareGiver extends User {
    private Map<String, Boolean> caretakers;

    public CareGiver(String name, String lastname, String phoneNr, String email, String personNummer) {
        super(name, lastname, phoneNr, email, personNummer, true); // Set careGiver to true
        this.caretakers = new HashMap<>();
    }
    public CareGiver() {
        // Default constructor required by Firebase for deserialization
    }
    public Map<String, Boolean> getCaretakers() {
        return caretakers;
    }
    public void setCaretakers(Map<String, Boolean> caretakers) {
        this.caretakers = caretakers;
    }

}

