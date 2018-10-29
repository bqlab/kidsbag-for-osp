package com.bqlab.kidsbag;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, Runnable {

    boolean isConnected = false;

    GoogleMap googleMap;
    Thread mapThread;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        init();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) { //구글 지도가 준비되었을 때(최초 한번만 호출됨, 중요하지 않음)
        this.googleMap = googleMap;
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(0, 0))); //카메라 초점은 위도 경도 0, 0에 맞추기
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10)); //줌은 10으로 맞추기
        googleMap.addMarker(new MarkerOptions() //마커 추가
                .position(new LatLng(0, 0))
                .title("현위치")); //마커 제목설정
    }

    @Override
    public void run() {
        while (isConnected) {
            try {
                Thread.sleep(500); //0.5초마다 검사
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkInternetState();
                        setMapMarker();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void init() {
        isConnected = getIntent().getBooleanExtra("login", false);
        new Thread(this).start();
        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    public void setMapMarker() {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(ReceiveService.lat, ReceiveService.lng))); //ReceiveService에서 실시간으로 받은 데이터를 기준으로 함
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(ReceiveService.lat, ReceiveService.lng))//ReceiveService에서 실시간으로 받은 데이터로 마커 찍기
                .title("현위치"));
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
