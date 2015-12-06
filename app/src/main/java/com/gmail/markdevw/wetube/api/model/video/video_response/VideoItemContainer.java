package com.gmail.markdevw.wetube.api.model.video.video_response;

import java.util.List;

/**
 * Created by Mark on 12/6/2015.
 */
public class VideoItemContainer {
    private String nextPageToken;
    private String prevPageToken;

    private List<Item> items;

    public VideoItemContainer(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems(){
        return items;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public String getPrevPageToken() {
        return prevPageToken;
    }

    public void setPrevPageToken(String prevPageToken) {
        this.prevPageToken = prevPageToken;
    }
}
