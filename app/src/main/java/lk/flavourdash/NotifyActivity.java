package lk.flavourdash;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import lk.flavourdash.utils.NotificationUtils;

public class NotifyActivity extends AppCompatActivity {

    public static final String TAG=MainActivity.class.getName();
    private NotificationManager notificationManager;
    private final String channelId="info";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);

        notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

            NotificationChannel channel=new NotificationChannel(channelId,"INFO",NotificationManager.IMPORTANCE_DEFAULT);
            channel.setShowBadge(true);
            channel.setDescription("This is information notification");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.setVibrationPattern(new long[]{0,1000,1000,1000});
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);

        }

//        findViewById(R.id.).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
////                Notification notification=new NotificationCompat.Builder(getApplicationContext(),channelId)
////                        .setSmallIcon(R.drawable.baseline_home_24)
////                        .setColor(Color.RED)
////                        .setContentTitle("My Notification")
////                        .setContentText("This is sample notification content")
////                        .build();
////
////                notificationManager.notify(2,notification);
//
//                NotificationUtils.sendNotification(NotifyActivity.this,"Welcome","Welcome to the flavoudash food app");
//            }
//        });


    }
}