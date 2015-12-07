package com.gmail.markdevw.wetube.api.model;

/**
 * Created by Mark on 4/30/2015.
 */
public class PlaylistItem {
    
    private String mTitle;
    private String mThumbnailURL;
    private String mId;
    private boolean mIsToBeDeleted;
    private boolean mIsSelected;
    private int mItemIndex;
    private String mDuration;

    public PlaylistItem(){ /* Do Noting */ }

    public PlaylistItem(String title, String thumbnailURL, String id, int index, String duration){
        setTitle(title);
        setThumbnailURL(thumbnailURL);
        setId(id);
        setIndex(index);
        setDuration(duration);
    }

    public String getId() { return mId; }
    public void setId(String id) { this.mId = id; }

    public String getTitle() { return mTitle; }
    public void setTitle(String title) { this.mTitle = title; }

    public String getThumbnailURL() { return mThumbnailURL; }
    public void setThumbnailURL(String thumbnail) { this.mThumbnailURL = thumbnail; }

    public boolean isToBeDeleted() { return mIsToBeDeleted;}
    public void setToBeDeleted(boolean toBeDeleted) { this.mIsToBeDeleted = toBeDeleted;}

    public void setSelected(boolean isSelected) { this.mIsSelected = isSelected;}
    public boolean isSelected() { return mIsSelected;}

    public int getIndex() { return mItemIndex;}
    public void setIndex(int index) { this.mItemIndex = index;}

    public String getDuration() {
        return mDuration;
    }

    public void setDuration(String mDuration) {
        this.mDuration = mDuration;
    }
}


