package com.gmail.markdevw.wetube.api;

import android.content.Context;
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
    private final int NUMBER_OF_VIDEOS_RETURNED = 50;
    private final int MAX_FRIENDS = 100;
    private static final int TWO_DIGIT_TIME_CHECK = 3;

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

    public int getCurrentPage(){ return mCurrentPage; }
    public void setPrevPageToken(String prevPageToken){ this.mPrevPageToken = prevPageToken; }
    public void setNextPageToken(String nextPageToken) { this.mNextPageToken = nextPageToken; }
    public String getPrevPageToken() { return mPrevPageToken; }
    public String getNextPageToken() { return mNextPageToken; }

    public void setCurrentSearch(String search) {this.mCurrentSearch = search; }
    public String getCurrentSearch() { return this.mCurrentSearch; }

    public String getAPI_KEY() { return API_KEY; }

    public void searchForVideos(final String searchTerms, final VideoResponseListener listener){
        setCurrentSearch(searchTerms);

        mCurrentPage = 1;
        final StringBuilder videoIdBuilder = new StringBuilder(700);


        Observable<VideoItemContainer> call = youTubeAPI.getVideos(searchTerms);
        call.subscribeOn(Schedulers.newThread())
           .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<VideoItemContainer>() {
                    @Override
                    public void onCompleted() {
                        Observable<DurationContainer> call = youTubeAPI.getVideoDuration(videoIdBuilder.toString());
                        call.subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<DurationContainer>() {
                                    @Override
                                    public void onCompleted() {
                                        listener.onSuccess();
                                    }

                                    @Override
                                    public void onError(Throwable e) {

                                    }

                                    @Override
                                    public void onNext(DurationContainer durationContainer) {
                                        if (mVideos.size() == 0) {
                                            return;
                                        }
                                        List<com.gmail.markdevw.wetube.api.model.video.duration_response.Item> items = durationContainer.getItems();
                                        int size = items.size();
                                        for (int i = 0; i < size; i++) {
                                            mVideos.get(i).setDuration(durationStringConverter(items.get(i).getContentDetails().getDuration()));
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {
                        listener.onError(searchTerms);
                    }

                    @Override
                    public void onNext(VideoItemContainer videoItemContainer) {
                        setPrevPageToken(videoItemContainer.getPrevPageToken());
                        setNextPageToken(videoItemContainer.getNextPageToken());

                        List<Item> items = videoItemContainer.getItems();
                        int size = items.size();

                        List<VideoItem> list = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            VideoItem item = new VideoItem();
                            String id = items.get(i).getId().getVideoId();
                            videoIdBuilder.append(id);
                            if (i < size - 1) {
                                videoIdBuilder.append(",");
                            }
                            item.setId(id);
                            item.setTitle(items.get(i).getSnippet().getTitle());
                            item.setDescription(items.get(i).getSnippet().getDescription());
                            item.setThumbnailURL(items.get(i).getSnippet().getThumbnails().getDefault().getUrl());
                            list.add(item);
                        }

                        mVideos.clear();
                        mVideos.addAll(list);
                    }
                });
    }

//    public void searchForVideos(String searchTerms, String pageToken, final VideoResponseListener listener){
//        if(pageToken == null){
//            return;
//        }else if(pageToken.equals(mPrevPageToken)){
//            mCurrentPage--;
//        } else {
//            mCurrentPage++;
//        }
//
//        youTubeAPI.getVideos(searchTerms, pageToken)
//            .enqueue(new Callback<VideoItemContainer>() {
//                @Override
//                public void onResponse(Response<VideoItemContainer> response, Retrofit retrofit) {
//                    if (response.code() == 200) {
//                        setPrevPageToken(response.body().getPrevPageToken());
//                        setNextPageToken(response.body().getNextPageToken());
//
//                        List<Item> items = response.body().getItems();
//                        int size = items.size();
//                        List<VideoItem> list = new ArrayList<>(size);
//
//                        StringBuilder videoIdBuilder = new StringBuilder(500);
//
//                        for (int i = 0; i < size; i++) {
//                            VideoItem item = new VideoItem();
//                            String id = items.get(i).getId().getVideoId();
//                            videoIdBuilder.append(id);
//                            if (i < size - 1) {
//                                videoIdBuilder.append(",");
//                            }
//                            item.setId(id);
//                            item.setTitle(items.get(i).getSnippet().getTitle());
//                            item.setDescription(items.get(i).getSnippet().getDescription());
//                            item.setThumbnailURL(items.get(i).getSnippet().getThumbnails().getDefault().getUrl());
//                            list.add(item);
//                        }
//
//                        mVideos.clear();
//                        mVideos.addAll(list);
//
//                        youTubeAPI.getVideoDuration(videoIdBuilder.toString())
//                                .enqueue(new Callback<DurationContainer>() {
//                                    @Override
//                                    public void onResponse(Response<DurationContainer> response, Retrofit retrofit) {
//                                        if (mVideos.size() == 0) {
//                                            return;
//                                        }
//                                        if (response.code() == 200) {
//                                            List<com.gmail.markdevw.wetube.api.model.video.duration_response.Item> items = response.body().getItems();
//                                            int size = items.size();
//                                            for (int i = 0; i < size; i++) {
//                                                mVideos.get(i).setDuration(durationStringConverter(items.get(i).getContentDetails().getDuration()));
//                                            }
//                                            listener.onSuccess();
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(Throwable t) {
//                                        Toast.makeText(WeTubeApplication.getSharedInstance(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//                                    }
//                                });
//                        }
//                    }
//                    @Override
//                    public void onFailure (Throwable t){
//                        Toast.makeText(WeTubeApplication.getSharedInstance(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//                    }
//                });
//            }

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

    public void convertTimeIndex(String duration, StringBuilder builder, int index1, int index2) {
        if (index2 - index1 == TWO_DIGIT_TIME_CHECK) {
            builder.append(duration.substring(index1 + 1, index2));
        } else {
            builder.append("0")
                    .append(duration.substring(index1 + 1, index2));
        }
    }
}
