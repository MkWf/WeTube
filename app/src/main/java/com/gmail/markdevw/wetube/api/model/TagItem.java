package com.gmail.markdevw.wetube.api.model;

/**
 * Created by Mark on 4/17/2015.
 */
public class TagItem {

    private String mTag;

    public TagItem(String tag){
        setTag(tag);
    }

    public void setTag(String tag) {this.mTag = tag;}
    public String getTag() { return mTag; }
}
