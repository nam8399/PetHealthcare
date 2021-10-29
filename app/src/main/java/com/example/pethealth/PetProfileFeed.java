package com.example.pethealth;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pethealth.fragments.PetAccount;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PetProfileFeed extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ChildEventListener mChild;
    private TextView tv_name2, tv_birthday2, tv_species2, tv_gender2;

    private RecyclerView recyclerView;
    private List<PetAccount> petAccountList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    ProgressDialog progressDialog;

    private EditText et_weight2, et_kcal, manual_feed;
    private TextView message;
    private RadioGroup rd_ask, rd_feed;
    private RadioButton et_auto, et_manual, ask_1, ask_16, ask_18, ask_2;
    private Button btn_feed;
    private double flt_result;
    private String str_feed;
     List<Object> Array = new ArrayList<Object>();

    SharedPreferences pref;          // 프리퍼런스
    SharedPreferences.Editor editor; // 에디터
    String myStr;
    String myStr2;
    Long manualkcal;                   // 문자 변수
    int mk;
    private PendingIntent pendingIntent;

    private AlarmManager alarmManager;
    private GregorianCalendar mCalender;

    private NotificationManager notificationManager;
    NotificationCompat.Builder builder;
    private Intent intent2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petprofilefeed);

        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
        recyclerView = findViewById(R.id.recyclerview2);


        btn_feed = findViewById(R.id.btn_feed);
        et_weight2 = findViewById(R.id.et_weight2);
        et_kcal = findViewById(R.id.et_kcal);
        et_auto = findViewById(R.id.et_auto);
        et_manual = findViewById(R.id.et_manual);
        rd_ask = findViewById(R.id.rd_ask);
        rd_feed = findViewById(R.id.rd_feed);
        ask_1 = findViewById(R.id.ask_1);
        ask_2 = findViewById(R.id.ask_2);
        ask_16 = findViewById(R.id.ask_16);
        ask_18 = findViewById(R.id.ask_18);
        message = findViewById(R.id.message);
        manual_feed = findViewById(R.id.manual_feed);

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        myStr = pref.getString("MyStr", "");
        myStr2 = pref.getString("MyStr2", "");

        et_weight2.setText(myStr);
        et_kcal.setText(myStr2);


        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        mCalender = new GregorianCalendar();

        Log.v("HelloAlarmActivity", mCalender.getTime().toString());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);



        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recyclerView.scrollToPosition(position);
            }
        }, 150);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String the_uid = user.getUid();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;//세로스크롤 차단
            }
        };


        recyclerView.setLayoutManager(layoutManager);
        final PetdataAdapter petdataAdapter = new PetdataAdapter(petAccountList, uidList);
        recyclerView.setAdapter(petdataAdapter);//데이터 넣기기


        mDatabase.getReference().child(the_uid).child("PetAccount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()
                petAccountList.clear();
                uidList.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())           //여러 값을 불러와 하나씩
                {
                    PetAccount petAccount = ds.getValue(PetAccount.class);
                    String uidKey = ds.getKey();

                    petAccountList.add(petAccount);
                    uidList.add(uidKey);
                }
                petdataAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
/*
        mReference = mDatabase.getReference("PetAccount" + the_uid); // 변경값을 확인할 child 이름
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                adapter.clear();

                for (DataSnapshot messageData : dataSnapshot.getChildren()) {
                    String msg2 = messageData.getValue().toString();
                    Array.add(msg2);
                    adapter.add(msg2);
                    // child 내에 있는 데이터만큼 반복합니다.

                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
*/

        listener();

        rd_ask.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.ask_1) {
                    //Toast.makeText(PetProfileFeed.this, "다이어트 필요", Toast.LENGTH_SHORT).show();
                    flt_result = 1;
                } else if (i == R.id.ask_16) {
                    //Toast.makeText(PetProfileFeed.this, "중성화 O", Toast.LENGTH_SHORT).show();
                    flt_result = 1.6;
                } else if (i == R.id.ask_18) {
                    //Toast.makeText(PetProfileFeed.this, "중성화 X", Toast.LENGTH_SHORT).show();
                    flt_result = 1.8;
                } else if (i == R.id.ask_2) {
                    //Toast.makeText(PetProfileFeed.this, "임신중", Toast.LENGTH_SHORT).show();
                    flt_result = 2;
                }
            }
        });

        rd_feed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.et_manual) {
                    //Toast.makeText(PetProfileFeed.this, "수동 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    str_feed = "Manual";
                } else if (i == R.id.et_auto) {
                    //Toast.makeText(PetProfileFeed.this, "자동 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    str_feed = "Auto";
                }
            }
        });



    }

    

    private void setAlarm() {
        //AlarmReceiver에 값 전달
        Intent receiverIntent = new Intent(PetProfileFeed.this, AlarmRecevier.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(PetProfileFeed.this, 0, receiverIntent, 0);

        //String from = "16:51:00"; //임의로 날짜와 시간을 지정

        //날짜 포맷을 바꿔주는 소스코드
        //SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
       // Date datetime = null;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 00 );
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Boolean dailyNotify = true; // 무조건 알람을 사용

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, PetProfileFeed.class);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        // calendar.setTime(datetime);

        //alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(),pendingIntent);

        if (dailyNotify) {


            if (alarmManager != null) {

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }

            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

        }
//        else { //Disable Daily Notifications
//            if (PendingIntent.getBroadcast(this, 0, alarmIntent, 0) != null && alarmManager != null) {
//                alarmManager.cancel(pendingIntent);
//                //Toast.makeText(this,"Notifications were disabled",Toast.LENGTH_SHORT).show();
//            }
//            pm.setComponentEnabledSetting(receiver,
//                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                    PackageManager.DONT_KILL_APP);
//        }
    }



    private void listener(){
        btn_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeNewUser(Double.parseDouble(et_weight2.getText().toString()) ,Double.parseDouble(et_kcal.getText().toString()) ,flt_result, str_feed, null);


                mReference.child("feed_data").child("message").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class);
                        message.setText(value);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
                    }
                });
                myStr = et_weight2.getText().toString();
                myStr2 = et_kcal.getText().toString();
                editor.putString("MyStr", myStr);
                editor.putString("MyStr2", myStr2);
                editor.apply();
                Intent alarmIntent = new Intent(PetProfileFeed.this, AlarmRecevier.class);
                pendingIntent = PendingIntent.getBroadcast(PetProfileFeed.this, 0, alarmIntent, 0);
                setAlarm();
/*
                Constraints constraints = new Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build();

                PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(MyWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

                WorkManager workManager = WorkManager.getInstance(getApplicationContext());
                workManager.enqueueUniquePeriodicWork("Counter", ExistingPeriodicWorkPolicy.KEEP,periodicWorkRequest);
                Toast.makeText(PetProfileFeed.this, "🎉 Scheduled job!", Toast.LENGTH_LONG).show();
*/
            }


        });
    }






    private void writeNewUser(double DWeight, double Kcal, double Num, String Status, String Intake) {
       feed_data feed_data = new feed_data(DWeight, Kcal, Num, null, null);
       try {
           manualkcal = Long.parseLong(manual_feed.getText().toString());
           mReference.child("feed_data").setValue(feed_data);
           mReference.child("feed_data").child("manual kcal").setValue(manualkcal);
           mReference.child("feed_data").child("status").setValue(Status);
           //Toast.makeText(PetProfileFeed.this, "사료 지급중", Toast.LENGTH_SHORT).show();
       } catch (NumberFormatException e) {
           int manualkcal2 = 0;
           mReference.child("feed_data").setValue(feed_data);
           mReference.child("feed_data").child("manual kcal").setValue(manualkcal2);
           mReference.child("feed_data").child("status").setValue(Status);
           //Toast.makeText(PetProfileFeed.this, "사료 지급중", Toast.LENGTH_SHORT).show();
       }

    }

}




