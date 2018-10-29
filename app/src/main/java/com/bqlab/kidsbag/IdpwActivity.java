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

public class IdpwActivity extends AppCompatActivity implements OnMapReadyCallback, Runnable {

    private static final String TAG = IdpwActivity.class.getSimpleName();

    boolean isConnected = false;

    Integer temp = 0;
    Boolean buzz = false;
    Double lat = (double) 0, lng = (double) 0;
    String id = null;

    Button mainLogin;
    Button mainRegister;
    Button mainCommand;

    TextView mainUser;
    TextView mainTemperature;

    GoogleMap googleMap;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
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
                Thread.sleep(500);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkInternetState();
                        syncData();
                        setMapMarker(lat, lng);
                        setTemperature(temp);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void syncData() {
        buzz = ReceiveService.buzz;
        temp = ReceiveService.temp;
        lat = ReceiveService.lat;
        lng = ReceiveService.lng;
    }

    public void init() {
        databaseReference.child("ids").setValue(0);
        mainUser = findViewById(R.id.main_user);
        mainLogin = findViewById(R.id.main_login);
        mainLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeLoginDialog();
            }
        });
        mainRegister = findViewById(R.id.main_register);
        mainRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeRegisterDialog();
            }
        });
        mainCommand = findViewById(R.id.main_command);
        mainCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText e = new EditText(IdpwActivity.this);
                new AlertDialog.Builder(IdpwActivity.this)
                        .setView(e)
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (e.getText().toString()) {
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

    public void setMapMarker(double lat, double lng) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(10));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lng))
                .title("현위치"));
    }

    public void setTemperature(int temp) {
        String s = getResources().getString(R.string.main_temperature) + temp + (getResources().getString(R.string.main_temperature_cel));
        mainTemperature.setText(s);
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

    public void makeLoginDialog() {
        if (!isConnected) {
            final EditText e = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("로그인")
                    .setMessage("ID를 입력하세요.")
                    .setView(e)
                    .setPositiveButton("다음", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String id = e.getText().toString();
                            if (getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals("none"))
                                Toast.makeText(IdpwActivity.this, "아이디를 찾을 수 없습니다.", Toast.LENGTH_LONG).show();
                            else {
                                final EditText e = new EditText(IdpwActivity.this);
                                new AlertDialog.Builder(IdpwActivity.this)
                                        .setTitle("로그인")
                                        .setMessage("비밀번호를 입력하세요.")
                                        .setView(e)
                                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                final String pw = e.getText().toString();
                                                if (getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals(pw)) {
                                                    Toast.makeText(IdpwActivity.this, "로그인되었습니다.", Toast.LENGTH_LONG).show();
                                                    IdpwActivity.this.id = id;
                                                    databaseReference.child("ids").setValue(1);
                                                    mainLogin.setText(R.string.main_logout);
                                                    String s = id + "님, 환영합니다!";
                                                    mainUser.setText(s);
                                                    isConnected = true;
                                                    startService();
                                                    new Thread(IdpwActivity.this).start();
                                                    dialog.dismiss();
                                                } else
                                                    Toast.makeText(IdpwActivity.this, "비밀번호가 틀렸습니다.", Toast.LENGTH_LONG).show();
                                            }
                                        }).
                                        setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("로그아웃")
                    .setMessage("현재 아이디를 로그아웃 할까요?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(IdpwActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_LONG).show();
                            databaseReference.child("ids").setValue(0);
                            mainLogin.setText(R.string.main_login);
                            mainUser.setText(R.string.main_user_out);
                            stopService();
                            isConnected = false;
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
        }
    }

    public void makeRegisterDialog() {
        final EditText e = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("회원가입")
                .setMessage("ID를 입력하세요.")
                .setView(e)
                .setPositiveButton("다음", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String id = e.getText().toString();
                        if (!getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals("none"))
                            Toast.makeText(IdpwActivity.this, "이미 가입된 아이디입니다.", Toast.LENGTH_LONG).show();
                        else {
                            final EditText e = new EditText(IdpwActivity.this);
                            new AlertDialog.Builder(IdpwActivity.this)
                                    .setTitle("회원가입")
                                    .setMessage("비밀번호를 입력하세요.")
                                    .setView(e)
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final String pw = e.getText().toString();
                                            getSharedPreferences("ids", MODE_PRIVATE).edit().putString(id, pw).apply();
                                            Toast.makeText(IdpwActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    }).show();
                        }
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }
}
