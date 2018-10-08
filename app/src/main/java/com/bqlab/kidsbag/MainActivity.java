package com.bqlab.kidsbag;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
<<<<<<< HEAD
<<<<<<< HEAD
import android.app.job.JobInfo;
<<<<<<< HEAD
import android.app.job.JobScheduler;
=======
import android.app.job.JobParameters;
>>>>>>> b96ff4baab3424396bf0a5483dfb9fb65f8c8f01
import android.app.job.JobService;
import android.content.ComponentName;
=======
>>>>>>> parent of 67d4d6a... 내일하자 시발
=======
>>>>>>> parent of 67d4d6a... 내일하자 시발
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    boolean isConnected = false;
    boolean isOverheated = false;
    boolean isBuzzed = false;

    Double mV = (double) 0, mV1 = (double) 0;
    Boolean buzz = false;
    Integer temp = 0;

    Button mainCommand;
    TextView mainTemperature;
    GoogleMap googleMap;

    NotificationManager notificationManager;
    NotificationChannel notificationChannel;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setMembers();
        checkAndroidVersion();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0, 0)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .title("현위치"));
    }

    @Override
    public void run() {
        while (isConnected) {
            try {
                Thread.sleep(1000);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkInternetState();
                        setMapMarker(mV, mV1);
                        setTemperature(temp);
                        setBuzz(buzz);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMembers() {
        mainCommand = findViewById(R.id.main_command);
        mainCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText e = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this)
                        .setView(e)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (e.getText().toString()) {
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
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
        mainTemperature = findViewById(R.id.main_temperature);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                MainActivity.this.temp = dataSnapshot.child("temp").getValue(Integer.class);
                MainActivity.this.buzz = dataSnapshot.child("buzz").getValue(Boolean.class);
                MainActivity.this.mV = dataSnapshot.child("v").getValue(Double.class);
                MainActivity.this.mV1 = dataSnapshot.child("v1").getValue(Double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });
    }

    public void setMapMarker(double v, double v1) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(v, v1)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(v, v1))
                .title("현위치"));
    }

<<<<<<< HEAD
    public void setTemperature(int temp) {
        if (temp >= 40 && !isOverheated) {
            isOverheated = true;
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.main_temperature_oh)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        if (temp < 40 && isOverheated)
            isOverheated = false;
        String s = getResources().getString(R.string.main_temperature) + temp + (getResources().getString(R.string.main_temperature_cel));
        mainTemperature.setText(s);
    }

    public void setBuzz(boolean buzz) {
        if (buzz && !isBuzzed) {
            isBuzzed = true;
            databaseReference.child("buzz").setValue(false);
            makeNotification("디바이스의 부저 버튼을 눌렀습니다.");
        }
        if (!buzz && isBuzzed)
            isBuzzed = false;
    }

    public void checkInternetState() {
        ConnectivityManager mCM = (ConnectivityManager) this.getSystemService(Service.CONNECTIVITY_SERVICE);
        if (mCM != null) {
            NetworkInfo networkInfo = mCM.getActiveNetworkInfo();
            if ((networkInfo != null) && (networkInfo.getState() == NetworkInfo.State.CONNECTED)) {
                return;
            }
        }
        Toast.makeText(this, "인터넷이 연결되어 있지 않습니다.", Toast.LENGTH_LONG).show();
        finishAffinity();
    }

=======
>>>>>>> b96ff4baab3424396bf0a5483dfb9fb65f8c8f01
    public void checkAndroidVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("em", "긴급알림", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("긴급한 상황을 알립니다.");
            notificationChannel.enableLights(true);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            notificationManager.createNotificationChannel(notificationChannel);
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
