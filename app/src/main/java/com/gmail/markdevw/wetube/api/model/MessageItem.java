package com.gmail.markdevw.wetube.api.model;

/**
 * Created by Mark on 4/8/2015.
 */
public class MessageItem {
    public static final int INCOMING_MSG = 0;
    public static final int OUTGOING_MSG = 1;

    String message;
    int type;

    public MessageItem(String message, int type){
        setMessage(message);
        setType(type);
    }

    public void setMessage(String message) {this.message = message;}
    public String getMessage() { return message; }
    public void setType(int type) {this.type = type;}
    public int getType() { return this.type; }
}
