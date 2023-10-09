package com.example.myapplication.Notifications;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessaging;

import org.checkerframework.checker.nullness.qual.NonNull;

public class notifications_caregiver extends AppCompatActivity {

    EditText userTB, title, message;

    Button send;

    FirebaseUser user;
    FirebaseAuth auth;

    private DatabaseReference itemRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_caregiver);
        userTB = findViewById(R.id.editID);
        title = findViewById(R.id.editTitle);
        message = findViewById(R.id.editMsg);
        send = findViewById(R.id.sendButton);

        user = auth.getCurrentUser();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        itemRef = database.getReference("users/" + user + "/eaten");


        itemRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method will be called whenever the data at the specified location changes
                // You can retrieve the updated data from the dataSnapshot object
                // dataSnapshot.getChildren() will give you the list of eaten items for the caretaker
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String eatenItem = itemSnapshot.getKey();
                    // Process the eaten item as needed
                    // You can access the eaten item's data using itemSnapshot.getValue()
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occur during the listener setup or while retrieving data
                Log.e("FirebaseListener", "Error: " + databaseError.getMessage());
            }
        });
    }
}