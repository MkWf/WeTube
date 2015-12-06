package com.gmail.markdevw.wetube.api;

import retrofit.http.GET;

/**
 * Created by Mark on 12/6/2015.
 */
public interface YouTubeAPI {
    @GET("/youtube/v3/search?part=id,snippet&type=video&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk&fields=prevPageToken,nextPageToken,items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)&maxResults=50")

    @GET("/youtube/v3/search?part=id,snippet&type=video&key=AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk&fields=prevPageToken,nextPageToken,items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)&maxResults=50")
    
}
