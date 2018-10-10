package com.bqlab.kidsbag;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.bqlab.kidsbag.App.CHANNEL_ID;

public class ReceiveService extends Service {

    public boolean isConnected = true;

    private boolean isBuzzed;
    private boolean isOverheated;

    public static Integer temp;
    public static Boolean buzz;
    public static Double lat, lng;

    NotificationManager notificationManager;
    NotificationChannel notificationChannel;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    public void onCreate() {
        super.onCreate();
        setDatabaseReference();
        checkAndroidVersion();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       String content = intent.getStringExtra("content");
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(p)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while(isConnected) {
                        Thread.sleep(1000);
                        checkTempAndBuzz(buzz, temp);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        startForeground(1, notification);
        return START_NOT_STICKY;
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

    public void checkTempAndBuzz(boolean buzz, int temp) {
        if (buzz && !isBuzzed) {
            isBuzzed = true;
            databaseReference.child("buzz").setValue(false);
            makeNotification("디바이스의 부저 버튼을 눌렀습니다.");
        }
        if (!buzz && isBuzzed)
            isBuzzed = false;
        if (temp >= 40 && !isOverheated) {
            isOverheated = true;
            makeNotification("디바이스가 40℃ 이상의 온도를 감지했습니다.");
        }
        if (temp < 40 && isOverheated) {
            isOverheated = false;
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
}
