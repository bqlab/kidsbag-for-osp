package com.bqlab.kidsbag;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataReceiver extends JobService {

    private int temp;
    private double v, v1;
    private boolean buzz, jobCancelled;

    public FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    public DatabaseReference databaseReference = firebaseDatabase.getReference();

    static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        setDatabaseReference();
        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        jobCancelled = true;
        return true;
    }

    public int getTemp() {
        return temp;
    }

    public double getV() {
        return v;
    }

    public double getV1() {
        return v1;
    }

    public boolean getBuzz() {
        return buzz;
    }

    public void setDatabaseReference() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                temp = dataSnapshot.child("temp").getValue(Integer.class);
                buzz = dataSnapshot.child("buzz").getValue(Boolean.class);
                v = dataSnapshot.child("v").getValue(Double.class);
                v1 = dataSnapshot.child("v1").getValue(Double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void useCommand(String command) {
        switch (command) {
            case "ini":
                databaseReference.child("temp").setValue(0);
                databaseReference.child("v").setValue(0);
                databaseReference.child("v1").setValue(0);
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
                databaseReference.child("v").setValue(37.5);
                databaseReference.child("v1").setValue(126.9);
                break;
            case "map-ny":
                databaseReference.child("v").setValue(40.7);
                databaseReference.child("v1").setValue(-74.2);
                break;
        }
    }
}
