package com.example.pethealth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

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

public class dailyfoodActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<intakegroup> arrayList;
    private List<String> uidList = new ArrayList<>();
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dailyfood);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recyclerVieww); // 아이디 연결
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>();


        database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String the_uid = user.getUid();

        adapter = new dailyfoodAdapter(arrayList, uidList);
        recyclerView.setAdapter(adapter); //리사이클럽에 어댑터 연결

        databaseReference = database.getReference(the_uid+"/intakelist"); // DB테이블 연결
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                // 파이어베이스 데이터베이스의 데이터를 받아오는 곳
                arrayList.clear(); // 기존 배열리스트가 존재하지 않게 초기화
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){ // 반복문으로 데이터 List를 추출해냄
                    intakegroup group = snapshot.getValue(intakegroup.class); // 만들어뒀던 PP1 객체에 데이터를 담는다
                    arrayList.add(group); // 담은 데이터를 배열리스트에 넣고 리사이클러뷰로 보낼 준비비
                }
                adapter.notifyDataSetChanged(); // 리스트 저장 및 새로고침
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.e("dailyfoodActivity", String.valueOf(databaseError.toException())); //에러문 출력
            }

        });
    }
}