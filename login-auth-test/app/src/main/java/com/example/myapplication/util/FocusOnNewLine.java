package com.example.myapplication.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

// When enter or new line is entered into EditText this class
// will clear focus from the EditText.
public class FocusOnNewLine implements TextWatcher {
    public EditText view = null;
    public ViewCallback callback = null;
    public interface ViewCallback {
        void run(EditText view);
    }
    public FocusOnNewLine(EditText view, ViewCallback callback) {
        this.view = view;
        this.callback = callback;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        // i: the index where characters were added or removed
        // i1: amount of removed characters
        // i2: amount of added characters
        // System.out.println("Text: "+i+" "+i1 + " "+i2);

        // System.out.println("Hello! "+view.getText()+", "+charSequence);
        if(i2 > 0 && view.getText().toString().contains("\n")) {
            String str = "";
            for(int j=0;j<view.getText().length();j++) {
                if(view.getText().charAt(j) != '\n')
                    str += view.getText().charAt(j);
            }
            // String str = "";
            // if(i > 0) {
            //     str += charSequence.subSequence(0, i);
            // }
            // if(charSequence.length() - (i+1) > 0) {
            //     str += charSequence.subSequence(i + 1, charSequence.length() - (i + 1));
            // }
            view.setText(str);
            view.clearFocus();
            // System.out.println("DONE! "+view.getText());
            if(callback != null)
                callback.run(view);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
