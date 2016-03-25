package com.gmail.markdevw.wetube.data;

/**
 * Created by Mark on 3/24/2016.
 */
public class User {

    private String password;
    private String email;

    public User(){

    }

    public User(String password, String email){
        this.password = password;
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
