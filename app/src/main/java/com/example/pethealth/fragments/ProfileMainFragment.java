package com.example.pethealth.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pethealth.PP1;
import com.example.pethealth.PetAdapter;
import com.example.pethealth.PetProfileFix;
import com.example.pethealth.PetProfileFix2;
import com.example.pethealth.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class ProfileMainFragment extends Fragment implements PetAdapter.ListItemClickListener{

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<PP1> arrayList;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;


    public ProfileMainFragment() {
        // Required empty public constructor
    }

    @Override//28:00
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profilemain, container, false);

        //photo = (ImageView)view.findViewById(R.id.account_iv_profile);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView); // 아이디 연결
        recyclerView.setHasFixedSize(true); // 리사이클러뷰 기존성능 강화
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        arrayList = new ArrayList<>(); // PP1 객체를 담을 어레이 리스트
        ImageButton imageButton = (ImageButton) view.findViewById(R.id.btn_image);
        
        database = FirebaseDatabase.getInstance(); // 파이어베이스 데이터베이스 연동
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String the_uid = user.getUid();

        adapter = new PetAdapter(arrayList, getActivity(), this);
        recyclerView.setAdapter(adapter); //리사이클럽에 어댑터 연결

        databaseReference = database.getReference(the_uid+"/PetAccount"); // DB테이블 연결
        databaseReference.addValueEventListener(new ValueEventListener() {
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
                Log.e("ProfileMainFragment", String.valueOf(databaseError.toException())); //에러문 출력
            }

        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PetProfileFix2.class);
                startActivity(intent);
            }
        });





        return view;
    }

    @Override
    public void listItemClick(int position) {
        Intent intent = new Intent(getActivity(), PetProfileFix.class);
        intent.putExtra("position", position);
        startActivity(intent);
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
                Log.e("ProfileMainFragment", String.valueOf(databaseError.toException())); //에러문 출력
            }

        });

    }
}
