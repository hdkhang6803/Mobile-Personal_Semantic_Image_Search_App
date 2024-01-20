package com.example.mobile_semantic_image_search_frontend.Object;

public class User {
    private String userID;
    private String phoneNum;
    private String userName;

    public  User(){

    }
    public User(String id, String phone, String name){
        this.userID = id;
        this.phoneNum = phone;
        this.userName = name;
    }

    public String getUserID() {
        return userID;
    }

    public String getName() {
        return userName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}
