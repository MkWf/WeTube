package com.gmail.markdevw.wetube.api.model;

/**
 * Created by Mark on 4/30/2015.
 */
public class PlaylistItem {
    private String title;
    private String thumbnailURL;
    private String id;
    private boolean toBeDeleted;

    public PlaylistItem(String title, String thumbnailURL, String id){
        setTitle(title);
        setThumbnailURL(thumbnailURL);
        setId(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnail) {
        this.thumbnailURL = thumbnail;
    }

    public boolean isToBeDeleted() { return toBeDeleted;}

    public void setToBeDeleted(boolean toBeDeleted) { this.toBeDeleted = toBeDeleted;}
}
