package com.example.pethealth;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MyWorker extends Worker {

    private static int count = 0;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        count++;
        Log.d("MyWorker", "worker called - " + count + " time(s)");
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
        // AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        mReference.child("feed_data").child("status").setValue("intake");

        if (isStopped() == true) {
            /* 작업이 멈추었을 때 대비한 코드 */
            Log.d("MyWorker", "worker stopped - " + count + " time(s)");
        }
        return Result.success();
    }
}