package com.bqlab.kidsbag;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataReceiver extends JobService {

    public static Integer temp;
    public static Boolean buzz;
    public static Double v, v1;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    public boolean onStartJob(JobParameters params) {
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
