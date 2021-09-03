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
import android.widget.ImageView;
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
    private Button btn_petprofilefix, btn_petprofiledelete;
    private ImageView btn_bcs, btn_feed, btn_foodreport, btn_bcsreport;
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
        btn_petprofiledelete = findViewById(R.id.btn_petprofiledelete);
        btn_bcs = findViewById(R.id.btn_bcs);
        btn_feed = findViewById(R.id.btn_feed);
        btn_bcsreport = findViewById(R.id.btn_bcsreport);
        btn_foodreport = findViewById(R.id.btn_foodreport);


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
                Intent intent = new Intent(PetProfileFix.this, PetProfileUpdate.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        btn_petprofiledelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.getReference().child(the_uid).child("PetAccount").child(uidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PetProfileFix.this, "삭제 성공", Toast.LENGTH_SHORT).show();
                        finish();
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

        btn_bcs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PetProfileFix.this, bcsActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        btn_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PetProfileFix.this, PetProfileFeed.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        btn_bcsreport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PetProfileFix.this, BcsreportActivity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(layoutManager);
        final PetdataAdapter petdataAdapter = new PetdataAdapter(petAccountList, uidList);
        recyclerView.setAdapter(petdataAdapter);//데이터 넣기기





        mDatabase.getReference().child(the_uid).child("PetAccount").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()
                petAccountList.clear();
                uidList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren())           //여러 값을 불러와 하나씩
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








    }





    private void writeNewUser(double DWeight, double Kcal, double Num, String Status) {
       feed_data feed_data = new feed_data(DWeight, Kcal, Num, Status);

       mReference.child("feed_data").setValue(feed_data);
       Toast.makeText(PetProfileFix.this, "사료 지급시작", Toast.LENGTH_SHORT).show();
    }

}




