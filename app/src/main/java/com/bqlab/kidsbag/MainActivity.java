package com.bqlab.kidsbag;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Runnable {

    private static final String TAG = MainActivity.class.getSimpleName();

    final int ACCESS_FINE_LOCATION = 0;
    final int ACCESS_COARSE_LOCATION = 1;

    boolean isConnected = false;
    boolean isOverheated = false;
    boolean isBuzzed = false;

    Double mV = (double) 0, mV1 = (double) 0;
    Boolean buzz = false;
    Integer temp = 0;

    Button mainCommand;
    TextView mainTemperature;
    GoogleMap googleMap;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setMembers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isConnected = false;
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
        new Thread(MainActivity.this).start();
        isConnected = true;
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
                                    case "hey":
                                        databaseReference.child("test").push().setValue("hey");
                                        break;
                                    case "cel-def":
                                        databaseReference.child("temp").setValue(28);
                                        break;
                                    case "cel-oh":
                                        databaseReference.child("temp").setValue(40);
                                        break;
                                    case "bz-true":
                                        databaseReference.child("buzz").setValue(true);
                                        break;
                                    case "bz-false":
                                        databaseReference.child("buzz").setValue(false);
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
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.main_buzz)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
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
}
