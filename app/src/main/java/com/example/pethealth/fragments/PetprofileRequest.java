package com.example.pethealth.fragments;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class PetprofileRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://kh8956.dothome.co.kr/Petprofile.php";
    private Map<String, String> map;


    public PetprofileRequest(String Petimage, String Petname, String Petbirthday, String Species, String gender, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("Petimage", Petimage);
        map.put("Petname", Petname);
        map.put("Petbirthday", Petbirthday);
        map.put("Species", Species);
        map.put("gender", gender);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}