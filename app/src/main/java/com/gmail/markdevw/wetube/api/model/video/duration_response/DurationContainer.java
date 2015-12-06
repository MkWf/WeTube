package com.gmail.markdevw.wetube.api.model.video.duration_response;

import java.util.List;

/**
 * Created by Mark on 12/6/2015.
 */
public class DurationContainer {
    private List<Item> items;

    public DurationContainer(List<Item> items) {
        this.items = items;
    }

    public List<Item> getItems(){
        return items;
    }
}
