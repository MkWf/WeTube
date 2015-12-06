package com.gmail.markdevw.wetube.api;

import com.gmail.markdevw.wetube.api.model.video.video_response.VideoItemContainer;

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

    @GET("/youtube/v3/videos?&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk&part=contentDetails&fields=items(contentDetails/duration)")
    Call<VideoItemContainer> getVideoDuration(@Query("id") String ids);
}
