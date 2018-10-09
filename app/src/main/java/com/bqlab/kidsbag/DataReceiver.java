package com.bqlab.kidsbag;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DataReceiver extends JobService {

    private static final String TAG = "DataReceiver";
    private boolean jobCancelled = false;

    public static Integer temp;
    public static Boolean buzz;
    public static Double lat, lng;

    NotificationManager notificationManager;
    NotificationChannel notificationChannel;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        backgroundWork(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled");
        jobCancelled = true;
        return true;
    }

    private void backgroundWork(JobParameters params) {
        setDatabaseReference();
        checkAndroidVersion();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!jobCancelled) {
                    try {
                        Thread.sleep(1000);
                        makeNotification("존나 하기 싫다");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        if (jobCancelled)
            return;

        Log.d(TAG, "Job finishied");
        jobFinished(params, false);
    }

    public void setDatabaseReference() {
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

    public void makeNotification(String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify(0, new NotificationCompat.Builder(this, "em")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(content)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build());
        } else {
            notificationManager.notify(0, new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(content)
                    .setWhen(System.currentTimeMillis())
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .build());
        }
    }

    public void checkAndroidVersion() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("em", "긴급알림", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("긴급한 상황을 알립니다.");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
