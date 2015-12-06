package com.gmail.markdevw.wetube.api.model.video.video_response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Mark on 12/6/2015.
 */
public class Thumbnails {

    @SerializedName("default")
    private Default _default;

    public Default getDefault() {
        return _default;
    }

    public void setDefault(Default _default) {
        this._default = _default;
    }
}
