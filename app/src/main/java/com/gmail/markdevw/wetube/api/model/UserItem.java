package com.gmail.markdevw.wetube.api.model;

/**
 * Created by Mark on 4/3/2015.
 */
public class UserItem {
    private String mName;
    private String mId;
    private boolean mIsInSession;
    private boolean mIsLoggedIn;
    private boolean mIsFriend;

    public UserItem(String name, String id){
        setName(name);
        setId(id);
    }

    public UserItem(String name, String id, boolean isInSession, boolean isLoggedIn, boolean isFriend){
        setName(name);
        setId(id);
        setSessionStatus(isInSession);
        setOnlineStatus(isLoggedIn);
        setFriendStatus(isFriend);
    }

    public String getName() { return mName;}
    public void setName(String name) { this.mName = name; }

    public String getId() { return mId; }
    public void setId(String id) {this.mId = id; }

    public boolean getSessionStatus() { return mIsInSession; }
    public void setSessionStatus(boolean isInSession) {this.mIsInSession = isInSession; }

    public boolean getOnlineStatus() { return mIsLoggedIn; }
    public void setOnlineStatus(boolean isLoggedIn) {this.mIsLoggedIn = isLoggedIn; }
    
    public void setFriendStatus(boolean isFriend) {this.mIsFriend = isFriend;}
    public boolean getFriendStatus() { return this.mIsFriend; }

}
