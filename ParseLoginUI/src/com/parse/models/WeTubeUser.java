package com.parse.models;

import com.parse.ParseUser;

import org.json.JSONArray;

/**
 * Created by Mark on 2/26/2015.
 */
public class WeTubeUser extends ParseUser {

    public WeTubeUser(){ /*Do Nothing */ }

    public void setLoggedStatus(boolean isLoggedIn) { put("isLoggedIn", isLoggedIn); }
    public boolean getLoggedStatus() { return getBoolean("isLoggedIn"); }

    public void setSessionStatus(boolean isInSession) { put("isInSession", isInSession); }
    public boolean getSessionStatus() { return getBoolean("isInSession"); }

    public void setDefaultTags(JSONArray array) { put("tags", array); }
}

