package com.bqlab.kidsbag;

import android.Manifest;
import android.app.FragmentManager;
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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    final int ACCESS_FINE_LOCATION = 0;
    final int ACCESS_COARSE_LOCATION = 1;

    Button mainRegister;
    TextView mainTemperature;

    double mV, mV1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        setMembers();
    }

    public void setMembers() {
        mainRegister = findViewById(R.id.main_register);
        mainRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMarkerLocation(1, 1);
            }
        });
        mainTemperature = findViewById(R.id.main_temperature);

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);
    }

    public void setMarkerLocation(double v, double v1) {
        mV = v;
        mV1 = v1;
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng m = new LatLng(mV, mV1);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(m);
        markerOptions.title("현위치");
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(m));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
    }
}
