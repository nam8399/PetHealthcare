package com.example.pethealth.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.pethealth.R;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private ImageView et_image;
    private EditText et_name, et_birthday;
    private Spinner et_species;
    private RadioGroup et_radiogroup;
    private RadioButton et_gender, et_gender2;
    private Button btn_Petprofile;
    private String str_result, sfName;

    private EditText et_aduino;
    private Button btn_aduino;

    DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    DatabaseReference conditionRef = mRootRef.child("Size");

    private final int GET_GALLERY_IMAGE = 200;
    private ImageView imageview;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_petprofile, container, false);

        SharedPreferences sf = getContext().getSharedPreferences(sfName, 0);
        String get_name = sf.getString("name", "");
        String get_birth = sf.getString("Birthday", "");


        et_image = view.findViewById(R.id.et_image);
        et_name = view.findViewById(R.id.et_name);
        et_birthday = view.findViewById(R.id.et_birth);
        et_species = view.findViewById(R.id.et_species);
        et_radiogroup = view.findViewById(R.id.et_radiogroup);
        et_gender = view.findViewById(R.id.et_gender);
        et_gender2 = view.findViewById(R.id.et_gender2);

        et_gender.setChecked(sf.getBoolean("check1", false));
        et_gender2.setChecked(sf.getBoolean("check2", false));

        et_name.setText(get_name);
        et_birthday.setText(get_birth);

        et_aduino = (EditText)view.findViewById(R.id.et_aduino);
        btn_aduino = (Button)view.findViewById(R.id.btn_aduino);


        et_radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.et_gender) {
                    //Toast.makeText(getContext(),"It's a Female", Toast.LENGTH_SHORT).show();
                    str_result = et_gender.getText().toString();
                } else if(i == R.id.et_gender2) {
                    //Toast.makeText(getContext(),"It's Male", Toast.LENGTH_SHORT).show();
                    str_result = et_gender2.getText().toString();
                }
            }
        });

        Spinner speciesSpinner = (Spinner)view.findViewById(R.id.et_species);
        ArrayAdapter speciesAdapter = ArrayAdapter.createFromResource(getContext(),R.array.dog_species, android.R.layout.simple_spinner_item);
        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speciesSpinner.setAdapter(speciesAdapter);

        btn_Petprofile = view.findViewById(R.id.btn_Petprofile);
        btn_Petprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String Petimage = et_image.getContext().toString();
                String Petname = et_name.getText().toString();
                String Petbirthday = et_birthday.getText().toString();
                String Species = et_species.getSelectedItem().toString();
                String gender = str_result;

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 회원등록에 성공한 경우
                                Toast.makeText(getContext(), "정보 등록에 성공하였습니다.", Toast.LENGTH_SHORT).show();
                            } else { // 회원등록에 실패한 경우
                                Toast.makeText(getContext(), "정보 등록에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };
                // 서버로 Volley를 이용해서 요청을 함.
                PetprofileRequest petprofileRequest = new PetprofileRequest(Petimage, Petname, Petbirthday, Species, gender, responseListener);
                RequestQueue queue = Volley.newRequestQueue(getContext());
                queue.add(petprofileRequest);

                // Inflate the layout for this fragment
            }
            });

        et_image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent. setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, GET_GALLERY_IMAGE);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        conditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String text = dataSnapshot.getValue(String.class);
                //textView.setText(text);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btn_aduino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conditionRef.setValue(et_aduino.getText().toString());
            }
        });
    }

    public void onStop() {
        super.onStop();
        SharedPreferences sf = getContext().getSharedPreferences(sfName, 0);
        SharedPreferences.Editor editor = sf.edit();
        String get_name = et_name.getText().toString();
        String get_birthday = et_birthday.getText().toString();
        String get_species = et_species.getSelectedItem().toString();
        RadioButton get_gender = (RadioButton)et_radiogroup.findViewById(R.id.et_gender);
        RadioButton get_gender2 = (RadioButton)et_radiogroup.findViewById(R.id.et_gender2);
        editor.putString("name", get_name);
        editor.putString("Birthday", get_birthday);
        editor.putString("Species", get_species);
        editor.putString("Gender", str_result);
        editor.putBoolean("check1",get_gender.isChecked());
        editor.putBoolean("check2",get_gender2.isChecked());
        editor.commit();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri selectedImageUri = data.getData();
            et_image.setImageURI(selectedImageUri);

        }
    }
}