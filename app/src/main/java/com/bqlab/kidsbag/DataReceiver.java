package com.bqlab.kidsbag;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataReceiver extends JobService {

    public static Integer temp;
    public static Boolean buzz;
    public static Double lat, lng;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    public boolean onStartJob(JobParameters params) {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                temp = dataSnapshot.child("temp").getValue(Integer.class);
                buzz = dataSnapshot.child("buzz").getValue(Boolean.class);
                lat = dataSnapshot.child("lat").getValue(Double.class);
                lng = dataSnapshot.child("lng").getValue(Double.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("tag", "onCancelled", databaseError.toException());
            }
        });
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    public void useCommand(String command) {
        switch (command) {
            case "ini":
                databaseReference.child("buzz").setValue(false);
                databaseReference.child("temp").setValue(0);
                databaseReference.child("lat").setValue(0);
                databaseReference.child("lng").setValue(0);
                break;
            case "cel-def":
                databaseReference.child("temp").setValue(28);
                break;
            case "cel-oh":
                databaseReference.child("temp").setValue(40);
                break;
            case "bz":
                databaseReference.child("buzz").setValue(true);
                break;
            case "map-se":
                databaseReference.child("lat").setValue(37.5);
                databaseReference.child("lng").setValue(126.9);
                break;
            case "map-ny":
                databaseReference.child("lat").setValue(40.7);
                databaseReference.child("lng").setValue(-74.2);
                break;
        }
    }
}
