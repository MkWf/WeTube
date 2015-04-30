package com.gmail.markdevw.wetube.api;

import android.content.Context;
import android.util.Log;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.api.model.MessageItem;
import com.gmail.markdevw.wetube.api.model.PlaylistItem;
import com.gmail.markdevw.wetube.api.model.TagItem;
import com.gmail.markdevw.wetube.api.model.UserItem;
import com.gmail.markdevw.wetube.api.model.VideoItem;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Mark on 3/26/2015.
 */
public class DataSource {
    private String API_KEY = "AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk";
    private List<VideoItem> videos;
    private List<UserItem> users;
    private List<UserItem> friends;
    private YouTube youtube;
    private YouTube.Search.List query;
    private int currentPage = 0;
    private String prevPageToken;
    private String nextPageToken;
    private String currentSearch;
    private UserItem currentRecipient;
    private final long NUMBER_OF_VIDEOS_RETURNED = 20;
    private List<MessageItem> messages = new Vector<>();
    private List<String> tags = new ArrayList<>();
    private List<TagItem> commonTags = new ArrayList<>();
    private List<TagItem> uncommonTags = new ArrayList<>();
    private List<PlaylistItem> playlist = new ArrayList<>();
    private boolean sessionController = false;

    public DataSource(Context context){
        videos = new ArrayList<VideoItem>();
        playlist = new ArrayList<PlaylistItem>();
        users = new ArrayList<UserItem>();
        friends = new ArrayList<UserItem>();
        youtube = new YouTube.Builder(new NetHttpTransport(),
                new JacksonFactory(), new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest hr) throws IOException {}
        }).setApplicationName(context.getString(R.string.app_name)).build();

        try{
            query = youtube.search().list("id,snippet");
            query.setKey(API_KEY);
            query.setType("video");
            query.setFields("nextPageToken,prevPageToken,items(id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)");
            query.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
        }catch(IOException e){
            Log.d("YC", "Could not initialize: " + e);
        }

    }

    public void setSessionController(boolean isController) { this.sessionController = isController; }
    public List<PlaylistItem> getPlaylist() { return playlist; }
    public List<TagItem> getCommonTags() { return commonTags; }
    public List<TagItem> getUncommonTags() { return uncommonTags; }
    public List<String> getTags() { return tags; }
    public List<MessageItem> getMessages() { return messages;}
    public void setCurrentRecipient(UserItem recipient){ this.currentRecipient = recipient;}
    public UserItem getCurrentRecipient() { return this.currentRecipient; }
    public List<VideoItem> getVideos(){
        return videos;
    }
    public List<UserItem> getUsers() { return users; }
    public List<UserItem> getFriends() { return friends; }
    public int getCurrentPage(){ return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public void setPrevPageToken(String prevPageToken){ this.prevPageToken = prevPageToken; }
    public void setNextPageToken(String nextPageToken) { this.nextPageToken = nextPageToken; }
    public String getPrevPageToken() { return prevPageToken; }
    public String getNextPageToken() { return nextPageToken; }
    public void setCurrentSearch(String search) {this.currentSearch = search; }
    public String getCurrentSearch() { return this.currentSearch; }
    public String getAPI_KEY() { return API_KEY; }

    public void searchForVideos(String searchTerms){
        setCurrentSearch(searchTerms);
        query.setQ(searchTerms);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            setPrevPageToken(response.getPrevPageToken());
            setNextPageToken(response.getNextPageToken());

            videos.clear();
            currentPage = 1;

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(SearchResult result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                videos.add(item);
            }
        }catch(IOException e){
            Log.d("YC", "Could not search: "+e);
        }
    }

    public void searchForVideos(String searchTerms, String pageToken){
        if(pageToken == null){
            return;
        }else if(pageToken.equals(prevPageToken)){
            currentPage--;
        }else{
            currentPage++;
        }

        query.setPageToken(pageToken);
        query.setQ(searchTerms);
        try{
            SearchListResponse response = query.execute();
            List<SearchResult> results = response.getItems();

            setPrevPageToken(response.getPrevPageToken());
            setNextPageToken(response.getNextPageToken());

            videos.clear();

            List<VideoItem> items = new ArrayList<VideoItem>();
            for(SearchResult result:results){
                VideoItem item = new VideoItem();
                item.setTitle(result.getSnippet().getTitle());
                item.setDescription(result.getSnippet().getDescription());
                item.setThumbnailURL(result.getSnippet().getThumbnails().getDefault().getUrl());
                item.setId(result.getId().getVideoId());
                videos.add(item);
            }
        }catch(IOException e){
            Log.d("YC", "Could not search: "+e);
        }
    }

    /*public void getLoggedInUsers(){
        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        //don't include yourself
        query.whereNotEqualTo("objectId", currentUserId);
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> userList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i=0; i<userList.size(); i++) {
                        users.add(new UserItem(userList.get(i).getUsername()));
                    }
                } else {
                    Toast.makeText(WeTubeApplication.getSharedInstance(),
                            "Error loading user list",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }*/
}
