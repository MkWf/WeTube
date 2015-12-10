package com.gmail.markdevw.wetube.api;

import com.gmail.markdevw.wetube.api.model.video.duration_response.DurationContainer;
import com.gmail.markdevw.wetube.api.model.video.video_response.VideoItemContainer;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by Mark on 12/6/2015.
 */
public interface YouTubeAPI {
    @GET("search?part=id,snippet&type=video&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk" +
            "&fields=prevPageToken,nextPageToken,items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)&maxResults=20")
    Observable<VideoItemContainer> getVideos(@Query("q") String search);

    @GET("search?part=id,snippet&type=video&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk" +
            "&fields=prevPageToken,nextPageToken,items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)&maxResults=20")
    Observable<VideoItemContainer> getVideos(@Query("q") String search, @Query("pageToken") String pageToken);

    @GET("videos?&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk&part=contentDetails&fields=items(contentDetails/duration)")
    Observable<DurationContainer> getVideoDuration(@Query("id") String ids);
}
