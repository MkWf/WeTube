package com.gmail.markdevw.wetube.api;

import com.gmail.markdevw.wetube.api.model.video.response.VideoItemContainer;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Mark on 12/6/2015.
 */
public interface YouTubeAPI {
    @GET("/youtube/v3/search?part=id,snippet&type=video&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk" +
            "&fields=prevPageToken,nextPageToken,items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)&maxResults=50")
    Call<VideoItemContainer> getVideos(@Query("q") String search);

    @GET("/youtube/v3/search?part=id,snippet&type=video&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk" +
            "&fields=prevPageToken,nextPageToken,items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)&maxResults=50")
    Call<VideoItemContainer> getVideos(@Query("q") String search, @Query("pageToken") String pageToken);
}
