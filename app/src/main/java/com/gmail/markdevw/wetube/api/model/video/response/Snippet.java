package com.gmail.markdevw.wetube.api.model.video.response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mark on 12/6/2015.
 */
public class Snippet {

    private String title;
    private String description;
    private Thumbnails thumbnails;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Thumbnails getThumbnails() {
        return thumbnails;
    }
    public void setThumbnails(Thumbnails thumbnails) {
        this.thumbnails = thumbnails;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }
}
