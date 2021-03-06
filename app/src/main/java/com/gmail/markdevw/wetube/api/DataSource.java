package com.gmail.markdevw.wetube.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.api.model.MessageItem;
import com.gmail.markdevw.wetube.api.model.PlaylistItem;
import com.gmail.markdevw.wetube.api.model.TagItem;
import com.gmail.markdevw.wetube.api.model.UserItem;
import com.gmail.markdevw.wetube.api.model.video.VideoItem;
import com.gmail.markdevw.wetube.api.model.video.duration_response.DurationContainer;
import com.gmail.markdevw.wetube.api.model.video.video_response.Item;
import com.gmail.markdevw.wetube.api.model.video.video_response.VideoItemContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Mark on 3/26/2015.
 */
public class DataSource {

    public interface VideoResponseListener{
        public void onSuccess();
        public void onError(String search);
    }

    private final String API_KEY = "AIzaSyDqalWrQoW2KoHoYLoyKl-FhncIQd2C3Rk";
    private final int NUMBER_OF_VIDEOS_RETURNED = 20;
    private final int MAX_FRIENDS = 100;
    private static final int TWO_DIGIT_TIME_CHECK = 3;

    private List<VideoItem> mVideos;
    private List<UserItem> mUsers;
    private List<UserItem> mFriends;
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
    private YouTubeAPI youTubeAPI;

    public DataSource(Context context){
        mVideos = new ArrayList<>(NUMBER_OF_VIDEOS_RETURNED);
        mPlaylist = new ArrayList<>();
        mUsers = new ArrayList<>();
        mFriends = new ArrayList<>();
        mMessages = new Vector<>();
        mCommonTags = new ArrayList<>();
        mUncommonTags = new ArrayList<>();
        mPlaylist = new ArrayList<>();

        youTubeAPI = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.youtube_baseUrl))
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(YouTubeAPI.class);

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


    public void setNextPageToken(String nextPageToken) { this.mNextPageToken = nextPageToken; }
    public String getNextPageToken() { return mNextPageToken; }

    public void setCurrentSearch(String search) {this.mCurrentSearch = search; }
    public String getCurrentSearch() { return this.mCurrentSearch; }

    public String getAPI_KEY() { return API_KEY; }


    /**
     *  Searches for YouTube videos based on the search query provided
     *
     *
     * @param query   The String we pass to YouTube Data API to search for videos
     * @param listener    Listener that gets called based on the success/error of the search
     */
    public void searchForVideos(final String query, final VideoResponseListener listener){
        setCurrentSearch(query);

        final StringBuilder videoIdBuilder = new StringBuilder(700);

        Observable<VideoItemContainer> call = youTubeAPI.getVideos(query);
        call.subscribeOn(Schedulers.newThread())
           .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<VideoItemContainer>() {
                    @Override
                    public void onCompleted() {
                        unsubscribe();
                        Observable<DurationContainer> call = youTubeAPI.getVideoDuration(videoIdBuilder.toString());
                        call.subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<DurationContainer>() {
                                    @Override
                                    public void onCompleted() {
                                        listener.onSuccess();
                                        unsubscribe();
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(DurationContainer durationContainer) {
                                        addDurationsToItems(durationContainer);
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(query);
                    }

                    @Override
                    public void onNext(VideoItemContainer videoItemContainer) {
                        setNextPageToken(videoItemContainer.getNextPageToken());

                        List<Item> items = videoItemContainer.getItems();
                        int size = items.size();

                        mVideos.clear();
                        for (int i = 0; i < size; i++) {
                            buildDurationSearchString(items, i, videoIdBuilder);
                            mVideos.add(createVideoItem(items, i));
                        }
                    }
                });
    }

    /**
     *  Searches for videos beyond the initial search result using page tokens
     *
     * @param query   The String we pass to YouTube Data API to search for videos
     * @param pageToken    Used to get the next page in videos
     * @param listener    Listener that gets called based on the success/error of the search
     */
    public void searchForVideos(final String query, final String pageToken, final VideoResponseListener listener) {
        if (pageToken == null) {
            return;
        }

        final StringBuilder videoIdBuilder = new StringBuilder(700);
        Observable<VideoItemContainer> call = youTubeAPI.getVideos(query, pageToken);
        call.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<VideoItemContainer>() {
                    @Override
                    public void onCompleted() {
                        unsubscribe();
                        Observable<DurationContainer> call = youTubeAPI.getVideoDuration(videoIdBuilder.toString());
                        call.subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<DurationContainer>() {
                                    @Override
                                    public void onCompleted() {
                                        listener.onSuccess();
                                        unsubscribe();
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(DurationContainer durationContainer) {
                                        addDurationsToItems(durationContainer);
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(query);
                    }

                    @Override
                    public void onNext(VideoItemContainer videoItemContainer) {
                        setNextPageToken(videoItemContainer.getNextPageToken());

                        List<Item> items = videoItemContainer.getItems();
                        int size = items.size();

                        for (int i = 0; i < size; i++) {
                            buildDurationSearchString(items, i, videoIdBuilder);

                            if(i == 0){
                                fillOutPlaceholderItem(items, i);
                            }else{
                                mVideos.add(createVideoItem(items, i));
                            }
                        }
                    }
                });
    }

    /**
     * Takes a DurationContainer and sets the duration for VideoItems that have been already added
     *
     * Each video search returns NUMBER_OF_VIDEOS_RETURNED. After we make the second call to get the
     * duration of each video, we need to back in the list by NUMBER_OF_VIDEOS_RETURNED and work our
     * way to the end filling out the setDuration() for each newly added VideoItem
     *
     * @param response  Response object
     */
    public void addDurationsToItems(DurationContainer response) {
        List<com.gmail.markdevw.wetube.api.model.video.duration_response.Item> items = response.getItems();
        int size = items.size();
        int videosSize = mVideos.size() - NUMBER_OF_VIDEOS_RETURNED;
        for (int i = 0; i < size; i++) {
            mVideos.get(i+videosSize).setDuration(durationStringConverter(items.get(i).getContentDetails().getDuration()));
        }
    }

    /**  Builds a String of video ids that are separated by commas that can be passed to YouTube Data API to fetch video durations.
     *
     *
     * @param response    Response items returned from YouTube Data API
     * @param index       The current index in the response
     * @param videoIdBuilder   StringBuilder that puts together our ids separated by commas
     */
    public void buildDurationSearchString(List<Item> response, int index, StringBuilder videoIdBuilder) {
        String id = response.get(index).getId().getVideoId();
        videoIdBuilder.append(id);
        if (index < response.size() - 1) {
            videoIdBuilder.append(",");
        }
    }

    /**
     * Creates a VideoItem from the response data at the provided index
     *
     * @param response   Response items returned from YouTube Data API
     * @param index   Index in response
     * @return  New VideoItem
     */

    @NonNull
    public VideoItem createVideoItem(List<Item> response, int index) {
        VideoItem item = new VideoItem();
        item.setId(response.get(index).getId().getVideoId());
        item.setTitle(response.get(index).getSnippet().getTitle());
        item.setDescription(response.get(index).getSnippet().getDescription());
        item.setThumbnailURL(response.get(index).getSnippet().getThumbnails().getDefault().getUrl());
        return item;
    }

    /**
     *  Adds data to a Dummy item that was inserted at the start of a paged search
     *
     *  To achieve the result where the Dummy item turns into a real item with data worth displaying,
     *  rather than creating a new VideoItem, we insert data into the Dummy item to trigger the ProgressDialog
     *  to disappear. Its visibility is toggled off by the addition of data in its adapter.
     *
     *
     * @param response   Response items returned from YouTube Data API
     * @param index   Index in the list
     */
    public void fillOutPlaceholderItem(List<Item> response, int index) {
        mVideos.get(mVideos.size() - 1).setId(response.get(index).getId().getVideoId());
        mVideos.get(mVideos.size() - 1).setTitle(response.get(index).getSnippet().getTitle());
        mVideos.get(mVideos.size() - 1).setDescription(response.get(index).getSnippet().getDescription());
        mVideos.get(mVideos.size() - 1).setThumbnailURL(response.get(index).getSnippet().getThumbnails().getDefault().getUrl());
    }

    /**
     *  Converts YouTube Data API durations for videos into a normal format
     *
     *  All durations begin with 'PT' and then the time follows.
     *  Examples:
     *      PT1H1M -> 1:01:00
     *      PT13M15S -> 13:15
     *
     * @param duration The String duration provided by YouTube Data API for a specific video
     * @return A string that has been converted to a standard video length format
     */
    public String durationStringConverter(String duration) {
        StringBuilder sb = new StringBuilder(10);

        int hIndex, mIndex, sIndex, ptIndex;
        ptIndex = 1;
        hIndex = duration.indexOf("H");
        mIndex = duration.indexOf("M");
        sIndex = duration.indexOf("S");

        final int noValue = -1;

        if (hIndex != noValue) {
            sb.append(duration.substring(ptIndex + 1, hIndex))
                    .append(":");
            if (mIndex != noValue) {
                convertTimeIndex(duration, sb, hIndex, mIndex);
                sb.append(":");
                if (sIndex != noValue) {
                    convertTimeIndex(duration, sb, mIndex, sIndex);
                } else {
                    sb.append("00");
                }
            } else if (sIndex != noValue) {
                sb.append("00:");
                convertTimeIndex(duration, sb, hIndex, sIndex);
            } else {
                sb.append("00:00");
            }
        } else if (mIndex != noValue) {
            sb.append(duration.substring(ptIndex + 1, mIndex))
                    .append(":");
            if (sIndex != noValue) {
                convertTimeIndex(duration, sb, mIndex, sIndex);
            } else {
                sb.append("00");
            }
        } else {
            sb.append("00:");
            convertTimeIndex(duration, sb, ptIndex, sIndex);
        }
        return sb.toString();
    }

    /**
     *  Helper method for durationStringConverter(String duration) that assists in converting
     *  hours, minutes, and seconds.
     *
     *  There can only ever be a 2 or 3 index difference between H, M, S.
     *  Example: PT1H1M15S
     *  There's a 2 index difference between H and M, but a 3 index difference between M and S.
     *  We use TWO_DIGIT_TIME_CHECK to determine whether we need to append a 0 in front or not, so
     *  we don't have a converted time that looks like 1:1:15, but rather 1:01:15.
     *
     * @param duration  The String duration provided by YouTube Data API for a specific video
     * @param builder  StringBuilder which creates the converted string
     * @param startIndex   The starting index for the time we're looking for
     * @param endIndex  The ending index for the time we're looking for
     */
    public void convertTimeIndex(String duration, StringBuilder builder, int startIndex, int endIndex) {
        if (endIndex - startIndex == TWO_DIGIT_TIME_CHECK) {
            builder.append(duration.substring(startIndex + 1, endIndex));
        } else {
            builder.append("0")
                    .append(duration.substring(startIndex + 1, endIndex));
        }
    }


}
