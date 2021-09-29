package com.example.pethealth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.pethealth.fragments.AccountFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileSelectActivity extends AppCompatActivity {

    Uri imageUri;
    StorageReference storageReference;
    ProgressDialog progressDialog;
    Button selectImagebtn, uploadimagebtn;
    EditText et_rename, et_message;
    ImageView firebaseimage;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_select);

        selectImagebtn = (Button)findViewById(R.id.selectImagebtn);
        uploadimagebtn = (Button)findViewById(R.id.uploadimagebtn);
        firebaseimage = (ImageView)findViewById(R.id.firebaseimage);

        et_rename = findViewById(R.id.et_rename);
        et_message = findViewById(R.id.et_message);

        selectImagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                selectImage();


            }
        });

        uploadimagebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                uploadImage();
                addUserInfo(et_rename.getText().toString(), et_message.getText().toString());

            }
        });

    }

    private void uploadImage() {

        try {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("파일 업로드중....");
            progressDialog.show();


            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.KOREA);
            Date now = new Date();
            String fileName = formatter.format(now);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String the_uid = user.getUid();
            storageReference = FirebaseStorage.getInstance().getReference("images" + the_uid + "/1");

            storageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            firebaseimage.setImageURI(null);
                            Toast.makeText(ProfileSelectActivity.this,"업로드 성공",Toast.LENGTH_SHORT).show();
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();
                            finish();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {


                    if (progressDialog.isShowing())
                        progressDialog.dismiss();
                    Toast.makeText(ProfileSelectActivity.this,"업로드 실패",Toast.LENGTH_SHORT).show();


                } });
        } catch (IllegalArgumentException e) {
            Toast.makeText(ProfileSelectActivity.this,"사진이 선택되지 않았습니다.",Toast.LENGTH_SHORT).show();
            progressDialog.cancel();
        }






    }

    private void selectImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,100);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && data != null && data.getData() != null){

            imageUri = data.getData();
            firebaseimage.setImageURI(imageUri);


        }
    }
    private void addUserInfo(String name, String message){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String the_uid = user.getUid();

        UserInfo userinfo = new UserInfo(name, message);
        databaseReference.child(the_uid).child("Userinfo").setValue(userinfo);
    }
}