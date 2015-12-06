package com.gmail.markdevw.wetube.api.model.video.response;

/**
 * Created by Mark on 12/6/2015.
 */
public class Item {
    private Id id;
    private Snippet snippet;

    public Id getId() {
        return id;
    }
    public void setId(Id id) {
        this.id = id;
    }

    public Snippet getSnippet() {
        return snippet;
    }
    public void setSnippet(Snippet snippet) {
        this.snippet = snippet;
    }
}
