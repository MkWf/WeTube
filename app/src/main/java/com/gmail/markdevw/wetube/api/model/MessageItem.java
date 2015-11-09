package com.gmail.markdevw.wetube.api.model;

/**
 * Created by Mark on 4/8/2015.
 */
public class MessageItem {
    public static final int INCOMING_MSG = 0;
    public static final int OUTGOING_MSG = 1;

    private String mMessage;
    private int mMessageType;

    public MessageItem(String message, int type){
        setMessage(message);
        setType(type);
    }

    public void setMessage(String message) { this.mMessage = message; }
    public String getMessage() { return mMessage; }

    public void setType(int type) {this.mMessageType = type;}
    public int getType() { return this.mMessageType; }
}
