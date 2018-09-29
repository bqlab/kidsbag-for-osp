package com.bqlab.kidsbag;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Runnable {
    final int ACCESS_FINE_LOCATION = 0;
    final int ACCESS_COARSE_LOCATION = 1;

    boolean isConnected = false;
    double mV = 0, mV1 = 0;

    Button mainRegister;
    TextView mainTemperature;
    GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        setMembers();
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
                        setMapMarker(mV, mV1);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setMembers() {
        mainRegister = findViewById(R.id.main_register);
        mainRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText e = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("개발자용 커맨드")
                        .setView(e)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (e.getText().toString()) {
                                    case "co-true":
                                        new Thread(MainActivity.this).start();
                                        isConnected = true;
                                        break;
                                    case "co-false":
                                        isConnected = false;
                                        break;
                                    case "co-state":
                                        if (isConnected)
                                            Toast.makeText(MainActivity.this, "its ok", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(MainActivity.this, "its not ok", Toast.LENGTH_SHORT).show();
                                        break;
                                    case "map-se":
                                        mV = 37.5;
                                        mV1 = 126.9;
                                        break;
                                    case "map-ny":
                                        mV = 40.7;
                                        mV1 = -74.2;
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
    }

    public void setMapMarker(double v, double v1) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(v, v1)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(v, v1))
                .title("현위치"));
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.ACCESS_COARSE_LOCATION);
        }
    }
}
