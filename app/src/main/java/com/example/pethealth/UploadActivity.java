package com.example.pethealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.ManufacturerUtils;
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

    int nCurrentPermission = 0;
    static final int PERMISSIONS_REQUEST = 0x00000001;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        OnCheckPermission();


        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        initView();
        listener();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void OnCheckPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "??? ????????? ???????????? ????????? ???????????? ?????????.", Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST);
            }
        }
    }


    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "??? ????????? ?????? ????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(this, "??? ????????? ?????? ????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
                }

                break;
        }
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
                //????????????????????? ?????? ???????????? ????????? ????????? ??????
                uploadImg(imageUrl);
            }
        });
        //????????? ?????????

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????? ??????????????? ????????????.
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                startActivityForResult(intent,GALLEY_CODE);
            }
        });
    }

    //?????? ?????? ??? ???????????? ??????
    //?????? ???????????? ?????????
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

    //??????????????? ?????????.
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
            progressDialog.setTitle("?????? ????????????....");
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
                        Toast.makeText(UploadActivity.this, "????????? ??????", Toast.LENGTH_SHORT).show();

                        //????????????????????? ?????????????????? ?????????
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = task.getResult();

                        ImageDTO imageDTO = new ImageDTO();
                        imageDTO.setImageUrl(downloadUrl.toString());
                        imageDTO.setTitle(file.getLastPathSegment());
                        imageDTO.setDescription(etDesc.getText().toString());
                        imageDTO.setUid(mAuth.getCurrentUser().getUid());
//                      imageDTO.setUserid(mAuth.getCurrentUser().getEmail());

                        //image ?????? ???????????? json ????????? ?????????.
                        //database.getReference().child("Profile").setValue(imageDTO);
                        //  .push()  :  ???????????? ?????????.
                        database.getReference().child(the_uid).child("Petstagram").push().setValue(imageDTO);

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
            Toast.makeText(UploadActivity.this, "????????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }
    }
}