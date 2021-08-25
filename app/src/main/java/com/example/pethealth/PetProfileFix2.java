package com.example.pethealth;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.CursorLoader;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.pethealth.fragments.PetAccount;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class PetProfileFix2 extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;

    private String imageUrl="";
    private int GALLEY_CODE = 10;

    private ImageView et_image;
    private EditText et_name, et_birthday;
    private Spinner et_species;
    private RadioGroup et_radiogroup;
    private RadioButton et_gender, et_gender2;
    private Button btn_Petprofile;
    private String str_result, sfName;
    ProgressDialog progressDialog;


    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("Size");

    private final int GET_GALLERY_IMAGE = 200;
    private ImageView imageview, imageview2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_petprofile);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        imageview = findViewById(R.id.imageView);
        imageview2 = findViewById(R.id.imageView2);
        btn_Petprofile = findViewById(R.id.btn_Petprofile);
        et_image = findViewById(R.id.et_image);
        et_name = findViewById(R.id.et_name);
        et_birthday = findViewById(R.id.et_birth);
        et_species = findViewById(R.id.et_species);
        et_radiogroup = findViewById(R.id.et_radiogroup);
        et_gender = findViewById(R.id.et_gender);
        et_gender2 = findViewById(R.id.et_gender2);

        Spinner speciesSpinner = findViewById(R.id.et_species);
        ArrayAdapter speciesAdapter = ArrayAdapter.createFromResource(this, R.array.dog_species, android.R.layout.simple_spinner_item);
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.setAdapter(speciesAdapter);

        listener();

        et_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.et_gender) {
                    Toast.makeText(PetProfileFix2.this,"수컷입니다", Toast.LENGTH_SHORT).show();
                    str_result = et_gender.getText().toString();
                } else if(i == R.id.et_gender2) {
                    Toast.makeText(PetProfileFix2.this,"암컷입니다", Toast.LENGTH_SHORT).show();
                    str_result = et_gender2.getText().toString();
                }
            }
        });


    }

    private void listener()
    {
        btn_Petprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //파이어베이스에 파일 업로드와 데이터 베이스 저장
                uploadImg(imageUrl);
            }
        });
        //이미지 업로드

        imageview.setOnClickListener(new View.OnClickListener() {
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
                    .into(imageview2);
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
                        Toast.makeText(PetProfileFix2.this, "업로드 성공", Toast.LENGTH_SHORT).show();

                        //파이어베이스에 데이터베이스 업로드
                        @SuppressWarnings("VisibleForTests")
                        Uri downloadUrl = task.getResult();

                        PetAccount petAccount = new PetAccount();
                        petAccount.setImage(downloadUrl.toString());
                        petAccount.setBirthday(et_birthday.getText().toString());
                        petAccount.setName(et_name.getText().toString());
                        petAccount.setGender(et_gender.getText().toString());
                        petAccount.setSpecies(et_species.getSelectedItem().toString());
                        petAccount.setUid(mAuth.getCurrentUser().getUid());
//                      imageDTO.setUserid(mAuth.getCurrentUser().getEmail());

                        //image 라는 테이블에 json 형태로 담긴다.
                        //database.getReference().child("Profile").setValue(imageDTO);
                        //  .push()  :  데이터가 쌓인다.
                        database.getReference().child("PetAccount" + the_uid).push().setValue(petAccount);

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
            Toast.makeText(PetProfileFix2.this, "이미지 선택 안함", Toast.LENGTH_SHORT).show();
        }
    }
}
