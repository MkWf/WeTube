package com.gmail.markdevw.wetube.api.model.video;

/**
 * Created by Mark on 3/26/2015.
 */
public class VideoItem {

    private String mTitle;
    private String mDescription;
    private String mThumbnailURL;
    private String mId;
    private String mDuration;

    public String getId() {
        return mId;
    }
    public void setId(String id) {
        this.mId = id;
    }

    public String getTitle() {
        return mTitle;
    }
    public void setTitle(String title) { this.mTitle = title; }

    public String getDescription() {
        return mDescription;
    }
    public void setDescription(String description) {
        this.mDescription = description;
    }

    public String getThumbnailURL() {
        return mThumbnailURL;
    }
    public void setThumbnailURL(String thumbnail) { this.mThumbnailURL = thumbnail; }

    public String getmDuration() {
        return mDuration;
    }

    public void setDuration(String mDuration) {
        this.mDuration = mDuration;
    }
}

