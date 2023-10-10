package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Set;


public class MealAdapter extends ArrayAdapter<MealEntry> {
    private final Context context;
    private final ArrayList<MealEntry> values;

    public MealAdapter(Context context, ArrayList<MealEntry> values) {
        super(context, R.layout.list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item, parent, false);
        ImageView starImageView = rowView.findViewById(R.id.starImageView);

        TextView view_name = rowView.findViewById(R.id.item_name);
        TextView view_time = rowView.findViewById(R.id.item_time);
        TextView view_desc = rowView.findViewById(R.id.item_desc);

        view_name.setText(values.get(position).name);
        view_time.setText(Helpers.FormatTime(values.get(position).hour,values.get(position).minute));
        view_desc.setText(values.get(position).desc);

        if(values.get(position).eaten) {
            starImageView.setColorFilter(Color.YELLOW); // Ändra färgen på stjärnan till gul
        } else {
            starImageView.setColorFilter(Color.TRANSPARENT); // Återställ färgen på stjärnan
        }
        return rowView;
    }

}
