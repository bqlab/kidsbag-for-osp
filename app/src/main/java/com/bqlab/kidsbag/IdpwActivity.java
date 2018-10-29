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

public class IdpwActivity extends AppCompatActivity {

    String forWhat;
    String id, pw;

    TextView idpwTitle;
    EditText idpwInId;
    EditText idpwInPw;
    Button idpwDone;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference databaseReference = firebaseDatabase.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkInternetState();
        init();
    }

    public void init() {
        forWhat = getIntent().getStringExtra("forWhat");

        idpwTitle = findViewById(R.id.idpw_title);
        idpwInId = findViewById(R.id.idpw_in_id);
        idpwInPw = findViewById(R.id.idpw_in_pw);
        idpwDone = findViewById(R.id.idpw_done);

        if (forWhat.equals("login")) {
            idpwTitle.setText(R.string.idpw_title_login);
            idpwDone.setBackground(getResources().getDrawable(R.drawable.idpw_btn_login));
            idpwDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    id = idpwInId.getText().toString();
                    pw = idpwInPw.getText().toString();

                    if (!getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals("none")
                            && getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals(pw)) {
                        databaseReference.child("ids").setValue(1);
                        startActivity(new Intent(IdpwActivity.this, MainActivity.class).putExtra("login", true));
                    } else
                        Toast.makeText(IdpwActivity.this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (forWhat.equals("register")) {
            idpwTitle.setText(R.string.idpw_title_register);
            idpwDone.setBackground(getResources().getDrawable(R.drawable.idpw_btn_register));
            idpwDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    id = idpwInId.getText().toString();
                    pw = idpwInPw.getText().toString();

                    if (!getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals("none"))
                        Toast.makeText(IdpwActivity.this, "이미 가입된 아이디입니다.", Toast.LENGTH_SHORT).show();
                    else {
                        getSharedPreferences("ids", MODE_PRIVATE).edit().putString(id, pw).apply();
                        Toast.makeText(IdpwActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
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
