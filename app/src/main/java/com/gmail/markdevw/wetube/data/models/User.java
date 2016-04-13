package com.gmail.markdevw.wetube.data.models;

/**
 * Created by Mark on 4/13/2016.
 */
public class User {
    private String email;
    private String name;
    private boolean isLoggedIn;
    private boolean isInSession;

    public User(){}

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public boolean isInSession() {
        return isInSession;
    }
}
