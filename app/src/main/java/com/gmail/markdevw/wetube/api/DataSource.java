package com.gmail.markdevw.wetube.api;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;

import com.gmail.markdevw.wetube.api.model.MessageItem;
import com.gmail.markdevw.wetube.api.model.PlaylistItem;
import com.gmail.markdevw.wetube.api.model.TagItem;
import com.gmail.markdevw.wetube.api.model.UserItem;
import com.gmail.markdevw.wetube.api.model.video.VideoItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by Mark on 3/26/2015.
 */
public class DataSource {
    private final String API_KEY = "AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk";
    private final int NUMBER_OF_VIDEOS_RETURNED = 50;
    private final int MAX_FRIENDS = 100;

    private List<VideoItem> mVideos;
    private List<UserItem> mUsers;
    private List<UserItem> mFriends;
    private int mCurrentPage;
    private String mPrevPageToken;
    private String mNextPageToken;
    private String mCurrentSearch;
    private UserItem mCurrentRecipient;
    private List<MessageItem> mMessages;
    private List<TagItem> mCommonTags;
    private List<TagItem> mUncommonTags;
    private List<PlaylistItem> mPlaylist;
    private boolean mIsSessionController;
    private boolean mIsPlayerVisible;
    private boolean mIsInVideoActivity;

    private int friendSize;
    private ActionBarActivity usersActivity;
    private ActionBarActivity mainActivity;

    public DataSource(Context context){
        mVideos = new ArrayList<>(NUMBER_OF_VIDEOS_RETURNED);
        mPlaylist = new ArrayList<>();
        mUsers = new ArrayList<>();
        mFriends = new ArrayList<>();
        mMessages = new Vector<>();
        mCommonTags = new ArrayList<>();
        mUncommonTags = new ArrayList<>();
        mPlaylist = new ArrayList<>();
    }

    public void setMainActivity(ActionBarActivity activity) {this.mainActivity = activity;}
    public ActionBarActivity getMainActivity() {return mainActivity;}

    public void setUsersActivity(ActionBarActivity activity) {this.usersActivity = activity;}
    public ActionBarActivity getUsersActivity() {return usersActivity;}

    public boolean isInVideoActivity() { return mIsInVideoActivity;}
    public void setVideoActivity(boolean isInVideoActivity) { this.mIsInVideoActivity = isInVideoActivity; }

    public int getFriendsSize() { return friendSize; }
    public void setFriendsSize(int size) { this.friendSize = size;}
    public int getMaxFriends() { return MAX_FRIENDS; }
    public List<UserItem> getFriends() { return mFriends; }

    public boolean isPlayerVisible() {return mIsPlayerVisible;}
    public void setPlayerVisible(boolean isPlayerVisible) { this.mIsPlayerVisible = isPlayerVisible; }

    public boolean isSessionController() {return mIsSessionController;}
    public void setSessionController(boolean isController) { this.mIsSessionController = isController; }

    public List<PlaylistItem> getPlaylist() { return mPlaylist; }

    public List<TagItem> getCommonTags() { return mCommonTags; }
    public List<TagItem> getUncommonTags() { return mUncommonTags; }

    public List<MessageItem> getMessages() { return mMessages;}

    public void setCurrentRecipient(UserItem recipient){ this.mCurrentRecipient = recipient;}
    public UserItem getCurrentRecipient() { return this.mCurrentRecipient; }

    public List<VideoItem> getVideos(){ return mVideos; }

    public List<UserItem> getUsers() { return mUsers; }

    public int getCurrentPage(){ return mCurrentPage; }
    public void setPrevPageToken(String prevPageToken){ this.mPrevPageToken = prevPageToken; }
    public void setNextPageToken(String nextPageToken) { this.mNextPageToken = nextPageToken; }
    public String getPrevPageToken() { return mPrevPageToken; }
    public String getNextPageToken() { return mNextPageToken; }

    public void setCurrentSearch(String search) {this.mCurrentSearch = search; }
    public String getCurrentSearch() { return this.mCurrentSearch; }

    public String getAPI_KEY() { return API_KEY; }

    public void searchForVideos(String searchTerms){
        setCurrentSearch(searchTerms);
    }

    public void searchForVideos(String searchTerms, String pageToken){
        if(pageToken == null){
            return;
        }else if(pageToken.equals(mPrevPageToken)){
            mCurrentPage--;
        }else{
            mCurrentPage++;
        }
    }
}
