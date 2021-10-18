package com.example.pethealth.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pethealth.ImageDTO;
import com.example.pethealth.LoginActivity;
import com.example.pethealth.ProfileSelectActivity;
import com.example.pethealth.R;
import com.example.pethealth.UploadActivity;
import com.example.pethealth.UploadedImageActivity;
import com.example.pethealth.UploadedImageAdapter;
import com.example.pethealth.UserInfo;
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
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {
    private TextView tv_result;
    private ImageView iv_profile;
    private FirebaseAuth auth;// 파이어베이스 인증 객체

    private final int GALLERY_CODE = 10;
    ImageView photo;

    private RecyclerView recyclerView, recyclerView2;
    private List<ImageDTO> imageDTOList = new ArrayList<>();
    private List<String> uidList = new ArrayList<>();
    private FirebaseDatabase firebaseDatabase;
    private RecyclerView.LayoutManager layoutManager;
    private String userinfo_name;
    private String userinfo_message;
    private TextView user_name, user_message;

    private UploadedImageAdapter adapter = new UploadedImageAdapter();
    private int count = -1;
    private GridLayoutManager gridLayoutManager;


    public AccountFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String the_uid = user.getUid();

        photo = (ImageView)view.findViewById(R.id.account_iv_profile);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference submitProfile = storageReference.child("images" + the_uid + "/1");
        submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getContext()).load(uri).into(photo);

                    }
                });
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
        //로그아웃 구현
        view.findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                Toast.makeText(getContext(),"로그아웃에 성공하였습니다.", Toast.LENGTH_SHORT).show();
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

        user_name = view.findViewById(R.id.userinfo_name);
        user_message = view.findViewById(R.id.userinfo_message);


        int numberOfColums = 3;
        gridLayoutManager = new GridLayoutManager(getContext(), numberOfColums);


        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        firebaseDatabase.getReference().child(the_uid).child("Userinfo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    UserInfo group = dataSnapshot.getValue(UserInfo.class);

                    //각각의 값 받아오기 get어쩌구 함수들은 intakegroup.class에서 지정한것
                    userinfo_name = group.getName();
                    userinfo_message = group.getMessage();

                    //텍스트뷰에 받아온 문자열 대입하기
                    user_name.setText(userinfo_name);
                    user_message.setText(userinfo_message);
                } catch (NullPointerException e) {

                }
                
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Log.e("MainActivity", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                gridLayoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        final UploadedImageAdapter uploadedImageAdapter = new UploadedImageAdapter(imageDTOList, uidList);
        recyclerView.setAdapter(uploadedImageAdapter);//데이터 넣기기
        //옵저버 패턴 --> 변화가 있으면 클라이언트에 알려준다.
        firebaseDatabase.getReference().child(the_uid).child("Petstagram").addValueEventListener(new ValueEventListener() {
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
                        //Toast.makeText(getContext(), "OK Click", Toast.LENGTH_SHORT).show();
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
                        firebaseDatabase.getReference().child(the_uid).child("Petstagram").child(uidList.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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

        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String the_uid = user.getUid();
            photo = (ImageView) getActivity().findViewById(R.id.account_iv_profile);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            StorageReference submitProfile = storageReference.child("images" + the_uid + "/1");
            submitProfile.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    try {
                        getView().post(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(getContext()).load(uri).into(photo);

                            }
                        });
                    } catch (NullPointerException e) {
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {

                }
            });

        } catch (NullPointerException e) {
        }

    }

}

