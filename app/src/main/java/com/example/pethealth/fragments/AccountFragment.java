package com.example.pethealth.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pethealth.ImageDTO;
import com.example.pethealth.ProfileSelectActivity;
import com.example.pethealth.R;
import com.example.pethealth.UploadActivity;
import com.example.pethealth.UploadedImageActivity;
import com.example.pethealth.UploadedImageAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private TextView tv_result;
    private ImageView iv_profile;
    private FirebaseAuth auth;        // 파이어베이스 인증 객체

    private final int GALLERY_CODE = 10;
    ImageView photo;


    private RecyclerView recyclerView;
    private List<ImageDTO> imageDTOList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase;

    private UploadedImageAdapter adapter = new UploadedImageAdapter();
    private int count = -1;
    private GridLayoutManager gridLayoutManager;


    public AccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);


        photo = (ImageView)view.findViewById(R.id.account_iv_profile);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child("images/1");
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri).into(photo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {

            }
        });

        view.findViewById(R.id.account_btn_follow_signout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ProfileSelectActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.account_btn_follow_signout2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UploadActivity.class);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.count1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UploadedImageActivity.class);
                startActivity(intent);
            }
        });
        firebaseDatabase = FirebaseDatabase.getInstance();
        recyclerView = view.findViewById(R.id.recyclerview);


        int numberOfColums = 3;
        gridLayoutManager = new GridLayoutManager(getContext(), numberOfColums);

        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);



        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                gridLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String the_uid = user.getUid();

        final UploadedImageAdapter uploadedImageAdapter = new UploadedImageAdapter(imageDTOList, uidList);
        recyclerView.setAdapter(uploadedImageAdapter);//데이터 넣기기
        //옵저버 패턴 --> 변화가 있으면 클라이언트에 알려준다.
        firebaseDatabase.getReference().child(the_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //변화된 값이 DataSnapshot 으로 넘어온다.
                //데이터가 쌓이기 때문에  clear()
                imageDTOList.clear();
                uidList.clear();

                for(DataSnapshot ds : dataSnapshot.getChildren())           //여러 값을 불러와 하나씩
                {
                    ImageDTO imageDTO = ds.getValue(ImageDTO.class);
                    String uidKey = ds.getKey();

                    imageDTOList.add(imageDTO);
                    uidList.add(uidKey);
                }
                uploadedImageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        uploadedImageAdapter.setOnItemClickListener(new UploadedImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder.setTitle("").setMessage("");

                builder.setPositiveButton("피드 보기", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Toast.makeText(getContext(), "OK Click", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), UploadedImageActivity.class);
                        intent.putExtra("position", position);
                        startActivity(intent);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {

                        // Toast.makeText(getContext(), "하위", Toast.LENGTH_SHORT).show();
                    }
                });

                builder.setNeutralButton("삭제하기", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int id)
                    {
                        storage.getReference().child(the_uid).child(imageDTOList.get(position).getTitle()).delete().addOnSuccessListener(new OnSuccessListener<Object>() {
                            @Override
                            public void onSuccess(Object o) {
                                onDeleteContent(position);
                            }
                        });
                    }
                    private void onDeleteContent(int position)
                    {
                        firebaseDatabase.getReference().child(the_uid).child(uidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println("error: "+e.getMessage());
                                Toast.makeText(getContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }

        });
/*
        Button buttonInsert = (Button)view.findViewById(R.id.button_main_insert);
        buttonInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                count++;

                Dictionary data = new Dictionary(count+"","Apple" + count, "사과" + count);

                //mArrayList.add(0, dict); //RecyclerView의 첫 줄에 삽입
                mArrayList.add(data); // RecyclerView의 마지막 줄에 삽입

                mAdapter.notifyDataSetChanged();             }
        });

*/
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child("images/1");
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(getContext()).load(uri).into(photo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {

            }
        });


    }




}

