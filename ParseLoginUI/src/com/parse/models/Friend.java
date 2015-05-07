package com.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Mark on 5/6/2015.
 */

@ParseClassName("Friend")
public class Friend extends ParseObject {

    public Friend(){}

    public Friend(String user, String friend){
        setUser(user);
        setFriend(friend);
    }

    public void setUser(String user) { put("friend1", user);}
    public void setFriend(String friend) { put("friend2", friend); }
}
