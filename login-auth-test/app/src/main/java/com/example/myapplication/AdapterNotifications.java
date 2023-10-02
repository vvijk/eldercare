package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;



public class AdapterNotifications extends RecyclerView.Adapter<AdapterNotifications.HolderNotifications>{

    private Context context;
    private ArrayList<ModelNotifications> notificationsList;

    public AdapterNotifications(Context context, ArrayList<ModelNotifications> notificationsList) {
        this.context = context;
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public HolderNotifications onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_notifications, parent, false);
        return new HolderNotifications(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderNotifications holder, int position) {

        ModelNotifications model = notificationsList.get(position);
        String name = model.getsName();
        String notification = model.getNotification();
        String timestamp = model.getTimestamp();
        //tar ut tiden
        String pTime = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());

        holder.nameTv.setText(name);
        holder.notificationTv.setText(notification);
        holder.timeTv.setText(pTime);
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    class HolderNotifications extends RecyclerView.ViewHolder{

        TextView nameTv, notificationTv, timeTv;



        public HolderNotifications(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.nameTv);
            notificationTv = itemView.findViewById(R.id.notificationTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }
}
