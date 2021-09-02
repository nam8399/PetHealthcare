package com.example.pethealth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import com.example.pethealth.fragments.PetAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BcsreportActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<PP1> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private List<String> uidList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bcsreport);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerView); // 아이디 연결
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); // PP1 객체를 담을 어레이 리스트


        database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String the_uid = user.getUid();

        Intent intent = getIntent();
        int position = intent.getIntExtra("position", 0);



        databaseReference = database.getReference(the_uid+"/PetAccount").child(uidList.get(position)).child("BcsReport"); // DB테이블 연결
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스의 데이터를 받아오는 곳
                arrayList.clear(); // 기존 배열리스트가 존재하지 않게 초기화
                uidList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ // 반복문으로 데이터 List를 추출해냄
                    PP1 pp1 = snapshot.getValue(PP1.class); // 만들어뒀던 PP1 객체에 데이터를 담는다
                    arrayList.add(pp1); // 담은 데이터를 배열리스트에 넣고 리사이클러뷰로 보낼 준비비
                    String uidKey = snapshot.getKey();

                    uidList.add(uidKey);
                }
                adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e("BcsreportActivity", String.valueOf(databaseError.toException())); //에러문 출력
            }

        });


        adapter = new PetAdapter(arrayList, this);
        recyclerView.setAdapter(adapter); //리사이클럽에 어댑터 연결


    }


    @Override
    public void onResume() {
        super.onResume();

        database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String the_uid = user.getUid();

        databaseReference = database.getReference(the_uid + "/PetAccount"); // DB테이블 연결
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스의 데이터를 받아오는 곳
                arrayList.clear(); // 기존 배열리스트가 존재하지 않게 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ // 반복문으로 데이터 List를 추출해냄
                    PP1 pp1 = snapshot.getValue(PP1.class); // 만들어뒀던 PP1 객체에 데이터를 담는다
                    arrayList.add(pp1); // 담은 데이터를 배열리스트에 넣고 리사이클러뷰로 보낼 준비비
                }
                adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e("BcsreportActivity", String.valueOf(databaseError.toException())); //에러문 출력
            }

        });

    }
}