package com.example.pethealth;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.pethealth.fragments.PetAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AlarmRecevier extends BroadcastReceiver {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private List<feed_data> feed_data = new ArrayList<>();
    private Long intake;
    private int i = 1;
    private String intakedate = "";
    private String intakedata = "";


    public AlarmRecevier(){ }

    NotificationManager manager;
    NotificationCompat.Builder builder;

    //오레오 이상은 반드시 채널을 설정해줘야 Notification이 작동함
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel1";
    

    @Override
    public void onReceive(Context context, Intent intent) {
        android.util.Log.i("알림 받음","onStartCommand()");
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
       // AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
/*
        builder = null;
        manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            manager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );
            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }
*/
        mReference.child("feed_data").child("status").setValue("intake");
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String getTime = dateFormat.format(date);


        final Handler delayHandler = new Handler();
        delayHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mReference.child("feed_data").child("intake").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Long value = dataSnapshot.getValue(Long.class);
                        intake = value;
                        android.util.Log.i("알림 받음", intake.toString());

                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String the_uid = user.getUid();




                        intakegroup group = new intakegroup();
                        group.setIntakedata(intake.toString());
                        group.setIntakedate(getTime);


                        mReference.child(the_uid).child("intakelist").push().setValue(group);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
                    }
                });
            }
        }, 10000);

    }
}