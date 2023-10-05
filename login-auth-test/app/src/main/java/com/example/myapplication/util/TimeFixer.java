package com.example.myapplication.util;


import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

// The purpose of this class is to automatically add a colon when editing text which shows time (12:00)
public class TimeFixer implements TextWatcher {
    public EditText view = null;
    public TimeFixer(EditText view) {
        this.view = view;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // i: the index where characters were added or removed
        // i1: amount of removed characters
        // i2: amount of added characters
//        System.out.println("Text: "+i+" "+i1 + " "+i2);

        // System.out.println("Hello! "+view.getText()+", "+charSequence);
        // if(i2 > 0 && view.getText().charAt(i) == '\n') {
        //     System.out.println("Hello! "+view.getText());
        //     view.clearFocus();
        // }

        if(view.getText().length() > 1) {
            // System.out.println("Hereo! "+view.getText()+", "+charSequence);
            if(view.getText().length()>2) {
                if(view.getText().charAt(2) == ':')
                    return;
                if(view.getText().length() == 4 && view.getText().charAt(1) == ':')
                    return;
            } else {
                if (view.getText().charAt(1) == ':')
                    return;
            }
            String cleanedString = "";
            for(int j=0;j<view.getText().length();j++) {
                if(view.getText().toString().charAt(j)!=':')
                    cleanedString += view.getText().toString().charAt(j);
            }
            String str = null;
            if(cleanedString.length() == 3) {
                str = cleanedString.substring(0,1) + ":";
                str += cleanedString.substring(1);
            } else {
                str = cleanedString.substring(0, 2) + ":";
                if (cleanedString.length() > 2) {
                    str += cleanedString.substring(2);
                }
            }
            view.setText(str);
            if(i1 > 0) {
                view.setSelection(i);
            } else if (i + i2 == 2)
                view.setSelection(i + i2 + 1);
            else
                view.setSelection(i + i2);
            // System.out.println("DONE! "+view.getText());
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
