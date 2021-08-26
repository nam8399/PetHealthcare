package com.example.pethealth;

public class UserInfo {
    String name;
    String message;

    public UserInfo(){}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public UserInfo(String name, String message){
        this.name = name;
        this.message = message;
    }
}