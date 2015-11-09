package com.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Mark on 5/6/2015.
 */

@ParseClassName("Friend")
public class Friend extends ParseObject {

    public Friend(){ /* Do Noting */ }

    public Friend(WeTubeUser user, WeTubeUser friend){
        setUser(user);
        setFriend(friend);
    }

    public void setUser(WeTubeUser user) { put("friend1", user);}
    public void setFriend(WeTubeUser friend) { put("friend2", friend); }
}
