package com.example.simplelistviewdemo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Set;
public class MealAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final Set<Integer> clickedPositions;

    public MealAdapter(Context context, String[] values, Set<Integer> clickedPositions) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
        this.clickedPositions = clickedPositions;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        TextView textView = rowView.findViewById(R.id.textView);
        ImageView starImageView = rowView.findViewById(R.id.starImageView);

        textView.setText(values[position]);

        if (clickedPositions.contains(position)) {
            starImageView.setColorFilter(Color.YELLOW); // Ändra färgen på stjärnan till gul
        } else {
            starImageView.setColorFilter(Color.TRANSPARENT); // Återställ färgen på stjärnan
        }

        return rowView;
    }

}
