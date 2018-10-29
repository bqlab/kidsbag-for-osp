package com.bqlab.kidsbag;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
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

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    boolean loginState = false;
    Button mainBtn1, mainBtn2;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkInternetState();
        checkLoggedIn();
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loginState = false;
    }

    public void init() {
        databaseReference.child("ids").setValue(0);

        mainBtn1 = findViewById(R.id.main_btn1);
        mainBtn2 = findViewById(R.id.main_btn2);

        if (loginState) {
            startService();
            databaseReference.child("ids").setValue(1);
            Toast.makeText(this, "로그인에 성공하였습니다.", Toast.LENGTH_SHORT).show();
            mainBtn1.setBackground(getResources().getDrawable(R.drawable.main_btn_temp));
            mainBtn2.setBackground(getResources().getDrawable(R.drawable.main_btn_map));
            mainBtn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, TempActivity.class)
                            .putExtra("login", true));
                    databaseReference.child("ids").setValue(1);
                }
            });
            mainBtn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, MapActivity.class)
                            .putExtra("login", true));
                }
            });
        } else {
            databaseReference.child("ids").setValue(0);
            mainBtn1.setBackground(getResources().getDrawable(R.drawable.main_btn_login));
            mainBtn2.setBackground(getResources().getDrawable(R.drawable.main_btn_register));
            mainBtn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, IdpwActivity.class)
                            .putExtra("forWhat", "login"));
                }
            });
            mainBtn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, IdpwActivity.class)
                            .putExtra("forWhat", "register"));
                }
            });
        }
    }

    public void checkLoggedIn() {
        Intent i = getIntent();
        loginState = i.getBooleanExtra("login", false);
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

    public void startService() {
        Intent i = new Intent(this, ReceiveService.class);
        i.putExtra("content", "디바이스와 실시간으로 데이터를 동기화하고 있습니다.");
        startService(i);
    }

    public void stopService() {
        stopService(new Intent(this, ReceiveService.class));
    }
}
