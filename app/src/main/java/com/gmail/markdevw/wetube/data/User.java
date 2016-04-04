package com.gmail.markdevw.wetube.data;

/**
 * Created by Mark on 3/24/2016.
 */
public class User {

    private String userName;
    private String email;

    public User(){

    }

    public User(String userName, String email){
        this.userName = userName;
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }
    public String getEmail() {
        return email;
    }
}
