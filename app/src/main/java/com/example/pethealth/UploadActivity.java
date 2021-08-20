package com.example.pethealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class UploadActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;

    private Button btnBack, btnOk;
    private ImageView ivProfile;
    private TextInputEditText etTitle, etDesc;
    private String imageUrl="";
    private int GALLEY_CODE = 10;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        initView();
        listener();
    }

    private void initView()
    {
        btnBack = (Button)findViewById(R.id.btn_profile_back);
        btnOk = (Button)findViewById(R.id.btn_profile_Ok);
        ivProfile = (ImageView)findViewById(R.id.iv_profile);
      //  etTitle = (TextInputEditText)findViewById(R.id.dt_profile_title);
        etDesc =(TextInputEditText)findViewById(R.id.dt_profile_desc);
    }
    private void listener()
    {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                startActivityForResult(intent,GALLEY_CODE);
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //파이어베이스에 파일 업로드와 데이터 베이스 저장
                uploadImg(imageUrl);
            }
        });
        //이미지 업로드

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //로컬 사진첩으로 넘어간다.
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                startActivityForResult(intent,GALLEY_CODE);
            }
        });
    }

    //사진 고른 후 돌아오는 코드
    //로컬 파일에서 업로드
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == GALLEY_CODE)
        {
            imageUrl = getRealPathFromUri(data.getData());
            RequestOptions cropOptions = new RequestOptions();
            Glide.with(getApplicationContext())
                    .load(imageUrl)
                    .apply(cropOptions.optionalCircleCrop())
                    .into(ivProfile);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //절대경로를 구한다.
    private String getRealPathFromUri(Uri uri)
    {
        String[] proj=  {MediaStore.Images.Media.DATA};
        CursorLoader cursorLoader = new CursorLoader(this,uri,proj,null,null,null);
        Cursor cursor = cursorLoader.loadInBackground();

        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String url = cursor.getString(columnIndex);
        cursor.close();
        return  url;
    }

    private void uploadImg(String uri)
    {
        try {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("파일 업로드중....");
            progressDialog.show();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String the_uid = user.getUid();

            Uri file = Uri.fromFile(new File(uri));
            final StorageReference riversRef = storageRef.child(the_uid+"/"+file.getLastPathSegment());
            UploadTask uploadTask = riversRef.putFile(file);


            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return riversRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(UploadActivity.this, "업로드 성공", Toast.LENGTH_SHORT).show();

                        //파이어베이스에 데이터베이스 업로드
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = task.getResult();

                        ImageDTO imageDTO = new ImageDTO();
                        imageDTO.setImageUrl(downloadUrl.toString());
                        imageDTO.setTitle(file.getLastPathSegment());
                        imageDTO.setDescription(etDesc.getText().toString());
                        imageDTO.setUid(mAuth.getCurrentUser().getUid());
//                      imageDTO.setUserid(mAuth.getCurrentUser().getEmail());

                        //image 라는 테이블에 json 형태로 담긴다.
                        //database.getReference().child("Profile").setValue(imageDTO);
                        //  .push()  :  데이터가 쌓인다.
                        database.getReference().child(the_uid).push().setValue(imageDTO);

                        /*Intent intent = new Intent(getApplicationContext(), UserActivity.class);
                        startActivity(intent);*/
                        finish();

                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });

        }catch (NullPointerException e)
        {
            Toast.makeText(UploadActivity.this, "이미지 선택 안함", Toast.LENGTH_SHORT).show();
        }
    }
}