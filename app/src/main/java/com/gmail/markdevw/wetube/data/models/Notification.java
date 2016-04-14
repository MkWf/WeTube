package com.gmail.markdevw.wetube.data.models;

/**
 * Created by Mark on 4/13/2016.
 */
public class Notification {
    private String name;
    private int notificationType;
    private boolean read;

    public Notification(){}

    public Notification(String name, int notificationType, boolean isRead){
        this.name = name;
        this.notificationType = notificationType;
        this.read = isRead;
    }

    public String getName() {
        return name;
    }

    public int getNotificationType() {
        return notificationType;
    }

    public boolean isRead() {
        return read;
    }
}
