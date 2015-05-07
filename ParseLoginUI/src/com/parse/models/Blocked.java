package com.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Mark on 5/6/2015.
 */

@ParseClassName("Blocked")
public class Blocked extends ParseObject {

    public Blocked(){}

    public Blocked(String blockedBy, String blockedUser){
        setBlockedBy(blockedBy);
        setBlockedUser(blockedUser);
    }

    public void setBlockedUser(String blockedUser) { put("userId", blockedUser);}
    public void setBlockedBy(String blockedBy) { put("blockedBy", blockedBy); }
}
