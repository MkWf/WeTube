package com.gmail.markdevw.wetube.data;

/**
 * Created by Mark on 3/24/2016.
 */
public class User {

    private String name;
    private String email;
    private boolean isLoggedIn;
    private boolean isInSession;

    public User(){

    }

    public User(String name, String email, boolean isLoggedIn, boolean isInSession){
        this.name = name;
        this.email = email;
        this.isLoggedIn = isLoggedIn;
        this.isInSession = isInSession;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public boolean isInSession() {
        return isInSession;
    }

    public String getName() {

        return name;
    }
    public String getEmail() {
        return email;
    }
}
