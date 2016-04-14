package com.gmail.markdevw.wetube.data.models;

/**
 * Created by Mark on 4/13/2016.
 */
public class User {
    private String email;
    private String name;
    private boolean loggedIn;
    private boolean inSession;

    public User(){}

    public User(String name, String email, boolean loggedIn, boolean inSession) {
        this.name = name;
        this.email = email;
        this.loggedIn = loggedIn;
        this.inSession = inSession;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public boolean isInSession() {
        return inSession;
    }
}
