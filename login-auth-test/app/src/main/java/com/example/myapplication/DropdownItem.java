package com.example.myapplication;

public class DropdownItem {
    private String text;
    private boolean selected;
    public void setText(String text) {
        this.text = text;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public String getText() {
        return this.text;
    }
    public boolean isSelected() {
        return this.selected;
    }
}
