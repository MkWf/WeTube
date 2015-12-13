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
    String key = "&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk";

    String searchPart = "search?part=id,snippet";
    String type = "&type=video";
    String searchFields = "&fields=nextPageToken,items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)";
    String maxResults = "&maxResults=20";

    String videosPart = "videos?&part=contentDetails";
    String videosFields = "&fields=items(contentDetails/duration)";

    @GET(searchPart + type + searchFields + maxResults + key)
    Observable<VideoItemContainer> getVideos(@Query("q") String search);

    @GET(searchPart + type + searchFields + maxResults + key)
    Observable<VideoItemContainer> getVideos(@Query("q") String search, @Query("pageToken") String pageToken);

    @GET(videosPart + videosFields + key)
    Observable<DurationContainer> getVideoDuration(@Query("id") String ids);
}
