package com.gmail.markdevw.wetube.data.models;

/**
 * Created by Mark on 4/13/2016.
 */
public class Friend {
    private String email;
    private String name;

    public Friend(){}

    public Friend(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
