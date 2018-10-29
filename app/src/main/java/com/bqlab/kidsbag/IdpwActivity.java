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

    String forWhat; //이 화면으로 이동한 이유
    String id, pw;  //id와 pw를 입력받으면 저장하는 공간

    TextView idpwTitle; //레이아웃 idpw_title
    EditText idpwInId;  //레이아웃 idpw_in_id
    EditText idpwInPw;  //레이아웃 idpw_in_pw
    Button idpwDone;    //레이아웃 idpw_done

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();      //파이어베이스 접근을 위해 선언
    DatabaseReference databaseReference = firebaseDatabase.getReference();   //파이어베이스의 DB 주소

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idpw);
        checkInternetState();
        init();
    }

    public void init() { //초기화 함수
        forWhat = getIntent().getStringExtra("forWhat"); //이 화면으로 이동하게 된 목적을 불러옵니다 (ex: login, register)

        idpwTitle = findViewById(R.id.idpw_title);
        idpwInId = findViewById(R.id.idpw_in_id);
        idpwInPw = findViewById(R.id.idpw_in_pw);
        idpwDone = findViewById(R.id.idpw_done);
        //레이아웃을 미리 만들어둔 객체에 대입합니다

        if (forWhat.equals("login")) { //만약 이 화면으로 이동하게된 목적이 login이라면
            idpwTitle.setText(getResources().getString(R.string.idpw_title_login)); //제목을 로그인으로 바꿉니다
            idpwDone.setBackground(getResources().getDrawable(R.drawable.idpw_btn_login)); //버튼 이미지도 로그인 배경화면으로 바꿉니다
            idpwDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //버튼이 눌렸을 때의 이벤트
                    id = idpwInId.getText().toString(); //id에 사용자가 입력한 아이디를 문자열로 저장합니다
                    pw = idpwInPw.getText().toString(); //마찬가지, 비밀번호를 문자열로 저장합니다

                    if (!getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals("none")
                            //만약 아이디 저장소(ids)에서 아이디(key)가 없음(none)과 같을 경우가 아닌 경우
                            && getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals(pw)
                            //만약 아이디 저장소(ids)에서 해당 아이디의 비밀번호가 pw와 같을 경우
                            && !id.equals("") && !pw.equals("")) {
                            //만약 빈칸을 비워두지 않았을 경우 이 모든 조건에서 참을 반환하는 경우
                        databaseReference.child("ids").setValue(1);
                        //서버에는 로그인했다는 신호를 보냄
                        startActivity(new Intent(IdpwActivity.this, MainActivity.class).putExtra("login", true));
                        //메인화면으로 이동, 이때 login 정보에 대해 함께 보냄(메인화면에서 로그인을 한 경우와 하지 않은 경우는 이 것으로 판별)
                    } else
                        Toast.makeText(IdpwActivity.this, "로그인에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    //위 조건에 부합하지 않았다는 것은 잘못 입력했거나 아이디가 없는 경우, 빈칸이 있을 경우입니다
                }
            });
        } else if (forWhat.equals("register")) { //이 화면으로 이동하게 된 목적이 회원가입이라면
            idpwTitle.setText(getResources().getString(R.string.idpw_title_register));  //제목은 회원가입으로 바꿈
            idpwDone.setBackground(getResources().getDrawable(R.drawable.idpw_btn_register)); //버튼 이미지 회원가입으로 바꿈
            idpwDone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //버튼이 눌렸을 때의 이벤트
                    id = idpwInId.getText().toString(); //로그인 때와 같은 목적으로 선언
                    pw = idpwInPw.getText().toString();

                    if (!(id.equals("") || pw.equals(""))) { //빈칸이 아닌경우
                        if (!getSharedPreferences("ids", MODE_PRIVATE).getString(id, "none").equals("none"))
                            //해당 아이디가 none이라는 비밀번호 값을 가지고 있지 않은 경우가 아닌 경우, 이미 아잉디가 있다는 의미
                            Toast.makeText(IdpwActivity.this, "이미 가입된 아이디입니다.", Toast.LENGTH_SHORT).show();
                        else {
                            //위 조건에서 거짓이라면 none이라는 값을 가졌다는 것, 즉 아이디가 없으니 회원가입 완료
                            getSharedPreferences("ids", MODE_PRIVATE).edit().putString(id, pw).apply();
                            Toast.makeText(IdpwActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else
                        Toast.makeText(IdpwActivity.this, "모든 빈 칸을 채워야 합니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void checkInternetState() { //인터넷이 연결되어 있지 않은 경우 앱 종료
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
