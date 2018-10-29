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

public class TempActivity extends AppCompatActivity implements Runnable { //스레드를 따로 사용하는 인터페이스

    boolean isConnected;
    TextView tempText;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);
        init();
    }

    @Override
    public void run() { //스레드 따로 사용
        while (isConnected) {
            try {
                Thread.sleep(500); //1000은 1초, 즉 0.5초마다 검사
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //메인스레드에서 사용할 함수들
                        checkInternetState(); //인터넷 상태 체크
                        setTemperature(); //온도 설정하기
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void init() {
        isConnected = getIntent().getBooleanExtra("login", false);
        new Thread(TempActivity.this).start(); //위에 스레드 시작하는 메소드
        tempText = findViewById(R.id.temp_text);
    }

    public void setTemperature() {
        String s = ReceiveService.temp+"°C";
        tempText.setText(s); //온도표시
    }

    public void checkInternetState() { //인터넷 체크
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
