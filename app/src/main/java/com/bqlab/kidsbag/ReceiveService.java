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

    private boolean isBuzzed = false;
    private boolean isOverheated = false;
    private boolean isFreezed = false;

    public static Integer temp = 0;
    public static Boolean buzz = false;
    public static Double lat = (double) 0, lng = (double) 0;

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
        temp = 0;
        lat = 0d;
        lng = 0d;
        isConnected = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { //이 서비스가 시작했을 때 호출되는 함수
        String content = intent.getStringExtra("content");
        Intent i = new Intent(this, MainActivity.class);
        PendingIntent p = PendingIntent.getActivity(this, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentText(content)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(p)
                .build();
        //알림서비스를 이용하기 위한 기본세팅

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (isConnected) {
                        Thread.sleep(1000); //1초에 한번씩 반복하게 만듦
                        checkTempAndBuzz(buzz, temp);// 온도와 부저를 확인하는 함수(응급상황을 알려야 하는 두 데이터)
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
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) { //서버의 데이터가 변경되었을 때 호출되는 함수
                temp = dataSnapshot.child("temp").getValue(Integer.class);
                buzz = dataSnapshot.child("buzz").getValue(Boolean.class);
                lat = dataSnapshot.child("lat").getValue(Double.class);
                lng = dataSnapshot.child("lng").getValue(Double.class);
                //temp=온도 buzz=부저 lat=위도 lng=경도
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("tag", "onCancelled", databaseError.toException());
            }
        });
    }

    public void setIsConnected(boolean b) {
        this.isConnected = b;
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
        }//안드로이드 8.0 이상일 경우에는 이렇게 노티피케이션 채널을 만들어야 함
    }

    public void checkTempAndBuzz(boolean buzz, int temp) {
        if (buzz && !isBuzzed) {
            isBuzzed = true;
            databaseReference.child("buzz").setValue(false); //파이어베이스의 buzz의 값을 false로
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

        if (temp <= -10 && !isFreezed) {
            isFreezed = true;
            makeNotification("디바이스가 영하 10℃ 이하의 온도를 감지했습니다.");
        }
        if (temp > -10 && isFreezed) {
            isFreezed = false;
        }
    }

    public void makeNotification(String content) { //알림을 만드는 함수
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
