package com.bqlab.kidsbag;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Objects;

public class App extends Application {
    public static final String CHANNEL_ID = "긴급알림";
    //위급한 상황에서 알림이 발생할 때 알림의 ID를 설정합니다.

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //안드로이드 버전이 8.0 이상일 경우
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, "긴급알림", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(notificationManager).createNotificationChannel(notificationChannel);
            //안드로이드 8.0 이상부터는 백그라운드에서 알림이 오게 처리하기 위해서는 이렇게 알림 채널을 반드시 만들어야 합니다. (정책상 어길 수 없음)
      }
    }
}
