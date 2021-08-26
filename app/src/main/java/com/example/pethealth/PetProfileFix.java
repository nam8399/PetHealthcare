package com.example.pethealth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;

public class PetProfileFix extends AppCompatActivity {

    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private ChildEventListener mChild;
    private TextView tv_name2, tv_birthday2, tv_species2, tv_gender2;

    private RecyclerView recyclerView;
    private List<PetAccount> petAccountList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    ProgressDialog progressDialog;

    private EditText et_weight2, et_kcal;
    private RadioGroup rd_ask, rd_feed;
    private RadioButton et_auto, et_manual, ask_1, ask_16, ask_18, ask_2;
    private Button btn_feed, btn_petprofilefix, btn_petprofiledelete;
    private double flt_result;
    private String str_feed;
     List<Object> Array = new ArrayList<Object>();

    SharedPreferences pref;          // 프리퍼런스
    SharedPreferences.Editor editor; // 에디터
    String myStr, myStr2;                   // 문자 변수




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_petprofilefix);

        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference();
        recyclerView = findViewById(R.id.recyclerview2);
        btn_petprofilefix = findViewById(R.id.btn_petprofilefix);


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

        pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        editor = pref.edit();

        myStr = pref.getString("MyStr", "");
        myStr2 = pref.getString("MyStr2", "");

        et_weight2.setText(myStr);
        et_kcal.setText(myStr2);




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

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this){
            @Override
            public boolean canScrollVertically() {
                return false;//세로스크롤 차단
            }
        };

        btn_petprofilefix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PetProfileFix.this, PetProfileFix2.class);
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        final PetdataAdapter petdataAdapter = new PetdataAdapter(petAccountList, uidList);
        recyclerView.setAdapter(petdataAdapter);//데이터 넣기기

        petdataAdapter.setOnItemClickListener(new PetdataAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                btn_petprofiledelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDatabase.getReference().child("PetAccount" +the_uid).child(uidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(PetProfileFix.this, "삭제 성공", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("error: "+e.getMessage());
                                Toast.makeText(PetProfileFix.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }

        });



        mDatabase.getReference().child("PetAccount" + the_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()
                petAccountList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())           //여러 값을 불러와 하나씩
                {
                    PetAccount petAccount = ds.getValue(PetAccount.class);
                    petAccountList.add(petAccount);
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
                    Toast.makeText(PetProfileFix.this, "다이어트 필요", Toast.LENGTH_SHORT).show();
                    flt_result = 1;
                } else if (i == R.id.ask_16) {
                    Toast.makeText(PetProfileFix.this, "중성화 O", Toast.LENGTH_SHORT).show();
                    flt_result = 1.6;
                } else if (i == R.id.ask_18) {
                    Toast.makeText(PetProfileFix.this, "중성화 X", Toast.LENGTH_SHORT).show();
                    flt_result = 1.8;
                } else if (i == R.id.ask_2) {
                    Toast.makeText(PetProfileFix.this, "임신중", Toast.LENGTH_SHORT).show();
                    flt_result = 2;
                }
            }
        });

        rd_feed.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.et_manual) {
                    Toast.makeText(PetProfileFix.this, "수동 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    str_feed = "Maunal";
                }  else if (i == R.id.et_auto) {
                    Toast.makeText(PetProfileFix.this, "자동 설정되었습니다.", Toast.LENGTH_SHORT).show();
                    str_feed = "Auto";
                }
            }
        });





    }




    private void listener(){
        btn_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeNewUser(Double.parseDouble(et_weight2.getText().toString()) ,Double.parseDouble(et_kcal.getText().toString()) ,flt_result, str_feed);
                myStr = et_weight2.getText().toString();
                myStr2 = et_kcal.getText().toString();
                editor.putString("MyStr", myStr);
                editor.putString("MyStr2", myStr2);
                editor.apply();

            }
        });
    }


    private void writeNewUser(double DWeight, double Kcal, double Num, String Status) {
       feed_data feed_data = new feed_data(DWeight, Kcal, Num, Status);

       mReference.child("feed_data").setValue(feed_data);
       Toast.makeText(PetProfileFix.this, "사료 지급시작", Toast.LENGTH_SHORT).show();
    }

}




