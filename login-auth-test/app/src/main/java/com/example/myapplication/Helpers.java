package com.example.myapplication;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Helpers {
    private Context context;
    public Helpers(Context context) {
        this.context = context;
    }
    public static String[] segmentString(String str, int lowerLimit) {
        /* Splits string into segments of lowerLimit length + remaining characters on the current word. (so as to not cut string mid-sentance.) */
        String[] stringSegments = new String[99];
        int stepper;
        int currentSubstring;
        for (currentSubstring = 0; !str.isEmpty(); currentSubstring++) {
            for (stepper = lowerLimit; ; stepper++){
                if (stepper >= str.length()){
                    stringSegments[currentSubstring] = str;
                    str = "";
                    break;
                }
                if (Character.isWhitespace(str.charAt(stepper))) {
                    stringSegments[currentSubstring] = str.substring(0, stepper);
                    str = str.substring(stepper);
                    break;
                }
            }
        }

        //Removes trailing nulls by creating a new string array. Also removes leading whitespace after segmenting string.
        String[] out = new String[currentSubstring];
        for (stepper = 0; stepper < currentSubstring; stepper++){
            out[stepper] = shaveLeadingWhitespace(stringSegments[stepper]);
        }

        return out;
    }

    public static String shaveLeadingWhitespace(String toShave){
        //Returns the string, after removing leading whitespace.
        String out;
        for (int i = 0; i < toShave.length(); i++){
            if (!Character.isWhitespace(toShave.charAt(i))){
                out = toShave.substring(i);
                return out;
            }
        }
        return "ERROR IN Helpers.shaveLeadingWhitespace";
    }

    public static String cutoffString(String toCut, int cutoff){
        //Cuts the string toCut off at cutoff, and appending trailing dots "...".
        if (toCut.length() <= cutoff){ return toCut + "...";}
        return toCut.substring(0, cutoff) + "...";
    }

    public static String linebreakString(String str, int lowerLimit){
        //Inserts linebreaks in a string after every lowerLimit characters (end of word).
        String[] strs = segmentString(str, lowerLimit);
        String out = "";
        for (int i = 0; i < strs.length; i++){
            out = out + "\n" + strs[i];
        }
        shaveLeadingWhitespace(out);
        return out;
    }

    public static boolean isStringInArray(String str, String[] arr){
        for (String s : arr) {
            if (Objects.equals(s, str)) {
                return true;
            }
        }
        return false;
    }
    public static boolean isStringInArray(String str, ArrayList<String> arr){
        for (String s : arr) {
            if (Objects.equals(s, str)) {
                return true;
            }
        }
        return false;
    }


    public boolean isValidUserInput(String email, String password, String firstname, String lastname, String phoneNr, String personNummer) {
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Ange epostadress");
            return false;
        } else if (TextUtils.isEmpty(password) || password.length() < 6) {
            showToast("Lösenordet måste vara minst 6 siffror");
            return false;
        } else if (TextUtils.isEmpty(firstname)) {
            showToast("Ange förnamn");
            return false;
        } else if (TextUtils.isEmpty(lastname)) {
            showToast("Ange efternamn");
            return false;
        } else if (TextUtils.isEmpty(phoneNr) || !TextUtils.isDigitsOnly(phoneNr)) {
            showToast("Ange telefonnummer");
            return false;
        } else if (TextUtils.isEmpty(personNummer) || personNummer.length() < 12) {
            showToast("Ange personnummret i 12 siffror");
            return false;
        }
        return true;
    }
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    static public String FormatTime(int hour, int minute) {
        String out="";
        if(hour<10)
            out+= "0";
        out += hour;
        out += ":";
        if(minute<10)
            out+= "0";
        out += minute;
        return out;

    }
}