package com.parse.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Mark on 5/6/2015.
 */

@ParseClassName("Blocked")
public class Blocked extends ParseObject {

    private String blockedUser;
    private String blockedBy;

    public Blocked(){}

    public Blocked(String blockedBy, String blockedUser){
        setBlockedBy(blockedBy);
        setBlockedUser(blockedUser);
    }

    public void setBlockedUser(String blockedUser) { this.blockedUser = blockedUser;}
    public void setBlockedBy(String blockedBy) { this.blockedBy = blockedBy; }
}
