package com.bqlab.kidsbag;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    private final int ACCESS_FINE_LOCATION = 0;
    private final int ACCESS_COARSE_LOCATION = 1;

    private String name;
    private int temp;
    private int gps;
    private Socket socket;
    private Thread thread;
    private BufferedReader reader;
    private boolean isConnected;

    Button mainRegister;
    TextView mainTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        setMembers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isConnected = false;
    }

    public void setMembers() {
        mainRegister = findViewById(R.id.main_register);
        mainRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setIP();
            }
        });

        mainTemperature = findViewById(R.id.main_temperature);
    }

    private void setIP() {
        final EditText e = new EditText(MainActivity.this);
        AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
        b.setMessage("IP를 입력하세요.");
        b.setView(e);
        b.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (checkSetIP(e.getText().toString())) {
                    Toast.makeText(MainActivity.this, e.getText().toString() + "에 연결합니다.", Toast.LENGTH_SHORT).show();
                    new Thread(new Connector(e.getText().toString(), 8090)).start();
                    dialogInterface.dismiss();
                } else {
                    Toast.makeText(MainActivity.this, "입력을 다시 확인하세요.", Toast.LENGTH_SHORT).show();
                    setIP();
                }
            }
        });
        b.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        b.show();
    }

    private boolean checkSetIP(String ip) {
        for (int i = 0; i < ip.length(); i++)
            if (!"0123456789.".contains(String.valueOf(ip.charAt(i)))) return false;
        return !ip.isEmpty() && ip.contains(".");
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.ACCESS_COARSE_LOCATION);
        }
    }

    public void callRequestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Log.d("PERMISSION", "Allowed permission ACCESS_FINE_LOCATION");
                else
                    Log.d("PERMISSION", "DENIED permission ACCESS_FINE_LOCATION");
            case ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Log.d("PERMISSION", "Allowed permission ACCESS_FINE_LOCATION");
                else
                    Log.d("PERMISSION", "DENIED permission ACCESS_FINE_LOCATION");

        }
    }

    private class Connector implements Runnable {
        private static final String TAG = "tcp";
        private String ip;
        private int port;

        Connector(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(ip, port);
                Connector.this.ip = socket.getRemoteSocketAddress().toString();
            } catch (UnknownHostException e) {
                Log.d(TAG, "호스트를 찾을 수 없습니다.");
            } catch (SocketTimeoutException e) {
                Log.d(TAG, "연결 시간이 초과되었습니다.");
            } catch (Exception e) {
                Log.e(TAG, ("오류가 발생했습니다."));
            }

            if (socket != null) {
                try {
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    isConnected = true;
                } catch (IOException e) {
                    Log.e(TAG, ("오류가 발생했습니다."));
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isConnected) {
                        thread = new Thread(new Receiver());
                        thread.start();
                    } else
                        Toast.makeText(MainActivity.this, "서버와 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private class Receiver implements Runnable {
        @Override
        public void run() {
            try {
                while (isConnected) {
                    if (reader == null || reader.readLine() == null) {
                        view.setBackground(getResources().getDrawable(R.color.colorGray));
                        view.setText(getString(R.string.normal, Room.this.name, 0));
                        break;
                    } else {
                        temp = Integer.parseInt(reader.readLine());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (temp <= 35) {
                                    view.setBackground(getResources().getDrawable(R.color.colorGreen));
                                    view.setText(getString(R.string.normal, Room.this.name, Room.this.temp));
                                } else if (temp <= 80) {
                                    view.setBackground(getResources().getDrawable(R.color.colorYellow));
                                    view.setText(getString(R.string.overheat, Room.this.name, Room.this.temp));
                                } else if (temp <= 105) {
                                    view.setBackground(getResources().getDrawable(R.color.colorRed));
                                    view.setText(getString(R.string.fire, Room.this.name, Room.this.temp));
                                }
                            }
                        });
                    }
                }
                reader = null;
                socket.close();
            } catch (IOException e) {
                Log.e(TAG, "오류가 발생했습니다.");
            }
        }
    }

}
