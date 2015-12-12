package com.gmail.markdevw.wetube.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.adapters.MessageItemAdapter;
import com.gmail.markdevw.wetube.adapters.PlaylistItemAdapter;
import com.gmail.markdevw.wetube.adapters.VideoItemAdapter;
import com.gmail.markdevw.wetube.api.DataSource;
import com.gmail.markdevw.wetube.api.model.MessageItem;
import com.gmail.markdevw.wetube.api.model.PlaylistItem;
import com.gmail.markdevw.wetube.api.model.video.VideoItem;
import com.gmail.markdevw.wetube.fragments.VideoListFragment;
import com.gmail.markdevw.wetube.services.MessageService;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.models.Blocked;
import com.parse.models.WeTubeUser;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * Created by Mark on 3/24/2015.
 */

public class MainActivity extends ActionBarActivity implements VideoListFragment.Delegate, YouTubePlayer.OnInitializedListener,
        YouTubePlayer.OnFullscreenListener,
        YouTubePlayer.PlaybackEventListener, MessageItemAdapter.Delegate,
        YouTubePlayer.PlaylistEventListener, PlaylistItemAdapter.Delegate,
        DrawerLayout.DrawerListener, YouTubePlayer.PlayerStateChangeListener,
        DialogInterface.OnDismissListener, DataSource.VideoResponseListener {

    @Bind(R.id.tb_activity_main) Toolbar mToolbar;
    @Bind(R.id.ll_activity_main_chat_bar) LinearLayout mChatbar;
    @Bind(R.id.rv_activity_main) RecyclerView mMessageRecyclerView;
    @Bind(R.id.activity_main_message_field) EditText mMessageField;
    @Bind(R.id.activity_main_send_button) Button mSendMessage;
    @Bind(R.id.fl_activity_video_list) FrameLayout mVideoList;
    @Bind(R.id.horizontal_line_video) View mVideoChatDivider;
    @Bind(R.id.dl_activity_main) DrawerLayout mDrawerLayout;
    @Bind(R.id.playlist_size) TextView mPlaylistSize;
    @Bind(R.id.rv_nav_activity_main) RecyclerView mPlaylistRecyclerView;

    private final int MAX_PLAYLIST_SIZE = 50;

    private Handler mHandler;
    private YouTubePlayerFragment mPlayerFragment;
    private YouTubePlayer mYouTubePlayer;
    private boolean mIsFullscreen;
    private MessageService.MessageServiceInterface mMessageService;
    private ServiceConnection mServiceConnection;
    private MessageClientListener mMessageClientListener;
    private MessageItemAdapter mMessageItemAdapter;
    private PlaylistItemAdapter mPlaylistItemAdapter;
    private String mName, mId;
    private List<String> mPlaylistIDs;
    private String mMsgSplitter = "=-=-=";
    private HashMap<String, String> mMessages;
    private ActionBarDrawerToggle mDrawerToggle;
    private boolean mIsBlocking;
    private boolean mIsFirstMessage = true;
    private AlertDialog mDialog;
    private Queue<Message> mMessageQueue;
    private boolean mIsPaused, mHasYourVideoEnded, mHasTheirVideoEnded,
            mHasVideoStarted, mIsAdPlaying, mIsRecoveringFromAd;
    private int mCurrentPlaylistIndex;
    private MenuItem searchViewItem;
    private List<VideoItem> pendingPlaylistAdditions = new ArrayList<>(10);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        WeTubeApplication.getSharedDataSource().setMainActivity(this);
        WeTubeApplication.getSharedDataSource().setVideoActivity(true);

        getRecipientData();
        startMessageService();

        initToolbar();
        initYouTubePlayerFragment();
        initChatRecyclerView();
        initPlaylistRecyclerView();
        initDrawerLayout();

        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mPlaylistIDs = new ArrayList<>();
        mMessages = new HashMap<>();
        mMessageQueue = new LinkedBlockingQueue<>();
        mHandler = new Handler();

        getFragmentManager()
                .beginTransaction()
                .add(R.id.fl_activity_video_list, new VideoListFragment(), "Video")
                .commit();
    }

    public void getRecipientData() {
        mName = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName();
        mId = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId();
    }

    public void startMessageService() {
        mServiceConnection = new MyServiceConnection();
        bindService(new Intent(this, MessageService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    public void initDrawerLayout() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, 0, 0);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    public void initPlaylistRecyclerView() {
        mPlaylistItemAdapter = new PlaylistItemAdapter();
        mPlaylistItemAdapter.setDelegate(this);

        mPlaylistRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mPlaylistRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPlaylistRecyclerView.setAdapter(mPlaylistItemAdapter);
    }

    public void initChatRecyclerView() {
        mMessageItemAdapter = new MessageItemAdapter();
        mMessageItemAdapter.setDelegate(this);

        mMessageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mMessageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mMessageRecyclerView.setAdapter(mMessageItemAdapter);
    }

    public void initToolbar() {
        mToolbar.setTitle("User: " + mName);
        setSupportActionBar(mToolbar);
    }

    public void initYouTubePlayerFragment() {
        mPlayerFragment = (YouTubePlayerFragment)getFragmentManager()
                .findFragmentById(R.id.youtubeplayerfragment);
        mPlayerFragment.initialize(WeTubeApplication.getSharedDataSource().getAPI_KEY(), this);

        getFragmentManager()
                .beginTransaction()
                .hide(mPlayerFragment)
                .commit();
    }

    @Override
    public void onSearchButtonClicked(VideoListFragment videoListFragment, EditText searchBox) {
        final String search = WeTubeApplication.getSharedDataSource().getCurrentSearch();

        if(search.isEmpty()){
            Toast.makeText(this, "Enter a search keyword first", Toast.LENGTH_LONG).show();
        }else{
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "Started search for " + search.toUpperCase() + "...");
            WeTubeApplication.getSharedDataSource().searchForVideos(search, this);
        }
    }

    @Override
    public void onPrevPageButtonClicked(VideoListFragment videoListFragment, EditText searchBox, ImageButton prevPage) {
        final String search = WeTubeApplication.getSharedDataSource().getCurrentSearch();
        WeTubeApplication.getSharedDataSource().searchForVideos(search, WeTubeApplication.getSharedDataSource().getPrevPageToken(), this);

    }

    @Override
    public void onNextPageButtonClicked(VideoListFragment videoListFragment, EditText searchBox, ImageButton nextPage) {
        final String search = WeTubeApplication.getSharedDataSource().getCurrentSearch();
        WeTubeApplication.getSharedDataSource().searchForVideos(search, WeTubeApplication.getSharedDataSource().getNextPageToken(), this);
    }

    @Override
    public void onVideoItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem) {
        if(pendingPlaylistAdditions.contains(videoItem)){
            return;
        }

        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
        int size = videos.size();
        for(int i = 0; i < size; i++){
            if(videos.get(i).getId().equals(videoItem.getId())){
                Toast.makeText(this, "Video already in playlist", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(WeTubeApplication.getSharedDataSource().getPlaylist().size() < MAX_PLAYLIST_SIZE){
            if(WeTubeApplication.getSharedDataSource().isSessionController()){
                pendingPlaylistAdditions.add(videoItem);
                mMessageService.sendMessage(mId,
                        mMsgSplitter + "addtoplaylist" +
                        mMsgSplitter + videoItem.getTitle() +
                        mMsgSplitter + videoItem.getThumbnailURL() +
                        mMsgSplitter + videoItem.getId() +
                        mMsgSplitter + videoItem.getDuration());
            }else{
                String message = mMsgSplitter + "linkedvideo" +
                        mMsgSplitter + videoItem.getTitle() +
                        mMsgSplitter + videoItem.getThumbnailURL() +
                        mMsgSplitter + videoItem.getId() +
                        mMsgSplitter + videoItem.getDuration();
                mMessageService.sendMessage(mId, message);
            }
        }else{
            Toast.makeText(this, "Max playlist size reached (50)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMessageVideoItemClicked(MessageItemAdapter itemAdapter, String title, String thumbnail, String id, String duration) {
        int size = pendingPlaylistAdditions.size();
        for(int i = 0; i < size; i++){
            if(title.equals(pendingPlaylistAdditions.get(i).getTitle())){
                return;
            }
        }

        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
        size = videos.size();
        for(int i = 0; i < size; i++){
            if(videos.get(i).getId().equals(id)){
                Toast.makeText(this, "Video already in playlist", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        VideoItem item = new VideoItem();
        item.setTitle(title);
        pendingPlaylistAdditions.add(item);
        if(WeTubeApplication.getSharedDataSource().getPlaylist().size() < MAX_PLAYLIST_SIZE){
            if(WeTubeApplication.getSharedDataSource().isSessionController()){
                mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                        mMsgSplitter + "addtoplaylist" +
                        mMsgSplitter + title +
                        mMsgSplitter + thumbnail +
                        mMsgSplitter + id +
                        mMsgSplitter + duration);
            }else{
                Toast.makeText(this, "Only the controller can add videos to the playlist from chat", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Max playlist size reached (50)", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Are you sure you want to go back to the video search?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            mMsgSplitter + "playlistexit" +
                            mMsgSplitter + ParseUser.getCurrentUser().getUsername() +
                            mMsgSplitter + ParseUser.getCurrentUser().getObjectId());
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Are you sure you want to leave this session?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            mMsgSplitter + "sessionend" +
                            mMsgSplitter + ParseUser.getCurrentUser().getUsername() +
                            mMsgSplitter + ParseUser.getCurrentUser().getObjectId());
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        this.mYouTubePlayer = youTubePlayer;
        this.mYouTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        this.mYouTubePlayer.setOnFullscreenListener(this);
        this.mYouTubePlayer.setPlaybackEventListener(this);
        this.mYouTubePlayer.setPlayerStateChangeListener(this);
        this.mYouTubePlayer.setPlaylistEventListener(this);
        this.mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            this.mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
        }else{
            this.mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        Toast.makeText(getApplicationContext(), "YouTubePlayer failed to initialize", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        layout();
    }

    @Override
    public void onFullscreen(boolean isFullscreen) {
        layout();
    }

    private void layout() {
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (isPortrait) {
            mIsFullscreen = false;
            setLayoutSize(mPlayerFragment.getView(), MATCH_PARENT, WRAP_CONTENT);
            mChatbar.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.VISIBLE);
        } else {
            if(mPlayerFragment.isVisible()){
                mIsFullscreen = true;
                setLayoutSize(mPlayerFragment.getView(), MATCH_PARENT, MATCH_PARENT);
                mChatbar.setVisibility(View.GONE);
                mToolbar.setVisibility(View.GONE);
            }
        }
    }

    private static void setLayoutSize(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMessageService.removeMessageClientListener(mMessageClientListener);
        unbindService(mServiceConnection);

        WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
        user.setSessionStatus(false);
        user.saveInBackground();

        try{
            mYouTubePlayer.release();
        }catch(NullPointerException e){

        }

        WeTubeApplication.getSharedDataSource().setCurrentRecipient(null);
        WeTubeApplication.getSharedDataSource().setMainActivity(null);
        WeTubeApplication.getSharedDataSource().getPlaylist().clear();
        WeTubeApplication.getSharedDataSource().getMessages().clear();
        WeTubeApplication.getSharedDataSource().getVideos().clear();
    }

    private void sendMessage() {
        if(mMessageField.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Type a message first before sending", Toast.LENGTH_LONG).show();
        }else{
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), mMessageField.getText().toString());
            mMessageField.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        searchViewItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setQueryHint(getString(R.string.search_youtube));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter a search keyword first", Toast.LENGTH_LONG).show();
                }else{
                    mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "Started search for " + query.toUpperCase() + "...");
                    WeTubeApplication.getSharedDataSource().searchForVideos(query, new DataSource.VideoResponseListener() {
                        @Override
                        public void onSuccess() {
                            Fragment f = getFragmentManager().findFragmentByTag("Video");
                            VideoListFragment vlf = (VideoListFragment) f;
                            vlf.getVideoItemAdapter().notifyDataSetChanged();
                            vlf.getRecyclerView().scrollToPosition(0);
                        }

                        @Override
                        public void onError(String search) {

                        }
                    });
                }
                return true;
            }


            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        MenuItem controller = menu.findItem(R.id.action_pass_control);
        MenuItem play = menu.findItem(R.id.action_play);

        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            controller.setEnabled(true);
            controller.setVisible(true);

            play.setEnabled(true);
            play.setVisible(true);
        }else{
            controller.setEnabled(false);
            controller.setVisible(false);

            play.setEnabled(false);
            play.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_play:
                List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                if(videos.size() == 0){
                    Toast.makeText(this, "Playlist is empty", Toast.LENGTH_SHORT).show();
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }else{
                    mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            mMsgSplitter + "playliststart");
                }
                break;
            case R.id.action_pass_control:
                mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                        mMsgSplitter + "passcontroller");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onPlayListItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem, int index, View itemView) {
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                    mMsgSplitter + "playlistindex" +
                    mMsgSplitter + String.valueOf(index));
        }
    }

    @Override
    public void onDeleteItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem) {
        int index = WeTubeApplication.getSharedDataSource().getPlaylist().indexOf(playlistItem);
        playlistItem.setToBeDeleted(true);
        mMessageService.sendMessage(mId,
                mMsgSplitter + "deleteitemplaylist" +
                mMsgSplitter + String.valueOf(index));
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerClosed(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }

    @Override
    public void onLoading() {

        WeTubeApplication.getSharedDataSource();
    }

    @Override
    public void onLoaded(String s) {
        WeTubeApplication.getSharedDataSource();
    }

    @Override
    public void onAdStarted() {
        mIsAdPlaying = true;
        mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                mMsgSplitter + "watchingad");
    }

    @Override
    public void onVideoStarted() {
        if(mIsAdPlaying){
            mIsAdPlaying = false;
            mIsRecoveringFromAd = true;
        }
        mHasVideoStarted = true;
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        }else if(mIsRecoveringFromAd){
            //Do Nothing
        }
    }

    @Override
    public void onVideoEnded() {

    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
        WeTubeApplication.getSharedDataSource();
    }

    @Override
    public void onPlaying() {
        if(mIsRecoveringFromAd && WeTubeApplication.getSharedDataSource().isSessionController()) {
            mYouTubePlayer.pause();
            mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
            mHasVideoStarted = false;
        }
        else if(mHasVideoStarted && !mIsAdPlaying){
            mYouTubePlayer.pause();
            mHasVideoStarted = false;

            if(!WeTubeApplication.getSharedDataSource().isSessionController()){
                mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                        mMsgSplitter + "videostart");
            }
        }else if(WeTubeApplication.getSharedDataSource().isSessionController() && !mIsAdPlaying) {
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                    mMsgSplitter + "play" + mMsgSplitter);
        }
    }

    @Override
    public void onPaused() {
        if(mIsRecoveringFromAd){
            mIsRecoveringFromAd = false;
        }
       else if(WeTubeApplication.getSharedDataSource().isSessionController() && !mIsAdPlaying){
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                    mMsgSplitter + "pause" + mMsgSplitter);
       }
    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onBuffering(boolean b) {
        WeTubeApplication.getSharedDataSource();
    }

    @Override
    public void onSeekTo(int i) {
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                    mMsgSplitter + "seek" +
                    mMsgSplitter + String.valueOf(i));
        }
    }

    @Override
    public void onPrevious() {
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                    mMsgSplitter + "playlistprev");
        }
    }

    @Override
    public void onNext() {
       if(WeTubeApplication.getSharedDataSource().isSessionController() && !mHasYourVideoEnded){
           mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                   mMsgSplitter + "playlistnext");
       }
    }

    @Override
    public void onPlaylistEnded() {

    }

    public void clearDialogsById(String id){

        for(Message message : mMessageQueue) {
            ArrayList<String> msg = new ArrayList<>(Arrays.asList(message.getTextBody().split(mMsgSplitter)));
            if(msg.get(3).equals(id)){
                mMessageQueue.remove(message);
            }
        }
        mIsBlocking = false;
        if(!mMessageQueue.isEmpty()){
            showNextMessage();
        }
    }

    public void showNextMessage() {
        if (mMessageQueue != null && mMessageQueue.size() > 0) {
            Message message = mMessageQueue.poll();
            ArrayList<String> msg = new ArrayList<>(Arrays.asList(message.getTextBody().split(mMsgSplitter)));

            if (msg.get(1).equals("friendadd")) {
                final String name = msg.get(2);
                final String id = msg.get(3);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Friend request from " + name);

                if(WeTubeApplication.getSharedDataSource().getFriendsSize() == WeTubeApplication.getSharedDataSource().getMaxFriends()){
                    builder.setNegativeButton("Friends list is full", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mMessageService.sendMessage(id,
                                    mMsgSplitter + "friendfull" +
                                    mMsgSplitter + ParseUser.getCurrentUser().getUsername());
                            dialog.cancel();
                        }
                    });
                    builder.setNeutralButton("Block User", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mIsBlocking = true;
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Are you sure you want to block " + name + " ?");

                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Blocked block = new Blocked(ParseUser.getCurrentUser().getObjectId(), id);
                                    block.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            mMessageService.sendMessage(id,
                                                    mMsgSplitter + "blockuser" +
                                                    mMsgSplitter + ParseUser.getCurrentUser().getUsername() +
                                                    mMsgSplitter + ParseUser.getCurrentUser().getObjectId());
                                        }
                                    });

                                    clearDialogsById(id);
                                    dialog.dismiss();
                                }
                            });
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.setCancelable(false);
                            mDialog = builder.create();
                            mDialog.show();
                        }
                    });
                    builder.setCancelable(false);
                    mDialog = builder.create();
                    mDialog.show();
                }else{
                    builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                            mMessageService.sendMessage(id,
                                    mMsgSplitter + "friendaccept" +
                                    mMsgSplitter + user.getUsername() +
                                    mMsgSplitter + user.getObjectId());
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mMessageService.sendMessage(id,
                                    mMsgSplitter + "frienddecline" +
                                    mMsgSplitter + ParseUser.getCurrentUser().getUsername());
                            dialog.cancel();
                        }
                    });
                    builder.setNeutralButton("Block User", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mIsBlocking = true;
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Are you sure you want to block " + name + " ?");

                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Blocked block = new Blocked(ParseUser.getCurrentUser().getObjectId(), id);
                                    block.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            mMessageService.sendMessage(id,
                                                    mMsgSplitter + "blockuser" +
                                                    mMsgSplitter + ParseUser.getCurrentUser().getUsername() +
                                                    mMsgSplitter + ParseUser.getCurrentUser().getObjectId());
                                        }
                                    });

                                    clearDialogsById(id);
                                    dialog.dismiss();
                                }
                            });
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.setCancelable(false);
                            mDialog = builder.create();
                            mDialog.show();
                        }
                    });
                }
                builder.setCancelable(false);
                mDialog = builder.create();
                mDialog.show();
            }
            if (mDialog != null) {
                mDialog.setOnDismissListener(this);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {

        if(!mMessageQueue.isEmpty() && !mIsBlocking){
            showNextMessage();
        }
    }

    @Override
    public void onSuccess() {
        Fragment f = getFragmentManager().findFragmentByTag("Video");
        VideoListFragment vlf = (VideoListFragment) f;
        vlf.getVideoItemAdapter().notifyDataSetChanged();
        vlf.getRecyclerView().scrollToPosition(0);
        mToolbar.setTitle("Page: " + WeTubeApplication.getSharedDataSource().getCurrentPage() + "   User: " + mName);
    }

    @Override
    public void onError(String search) {
        Toast.makeText(MainActivity.this, "Failed to search for " + search, Toast.LENGTH_LONG).show();
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mMessageService = (MessageService.MessageServiceInterface) iBinder;
            mMessageClientListener = new MyMessageClientListener();
            mMessageService.addMessageClientListener(mMessageClientListener);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMessageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {
        String msg;
        String msgId;

        String title;
        String thumbnail;
        String id;

        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            String msg = mMessages.get(failureInfo.getMessageId());
            if(msg.startsWith(mMsgSplitter + "addtoplaylist")){
                ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to add " + title + " to playlist", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "linkedvideo")){
                ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to add " + title + " to chat", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "deleteitemplaylist")){
                ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                String title = msgSplit.get(1);
                int size = WeTubeApplication.getSharedDataSource().getPlaylist().size();
                for(int i = 0; i < size; i++){
                    if(title.equals(WeTubeApplication.getSharedDataSource().getPlaylist().get(i).getTitle())){
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(i).setToBeDeleted(false);
                        break;
                    }
                }
                Toast.makeText(MainActivity.this, "Failed to delete " + title + " from playlist", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "passcontroller")){
                Toast.makeText(MainActivity.this, "Failed to pass controls to "
                        + WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName(), Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "playlistindex")){
                Toast.makeText(MainActivity.this, "Failed to start video", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "playliststart")){
                Toast.makeText(MainActivity.this, "Failed to start playlist", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + "playlistnext")) {
                Toast.makeText(MainActivity.this, "Failed to play next video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + "playlistprev")){
                Toast.makeText(MainActivity.this, "Failed to play previous video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + "pause")) {
                mYouTubePlayer.play();
                Toast.makeText(MainActivity.this, "Failed to pause video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + "play")){
                mYouTubePlayer.pause();
                Toast.makeText(MainActivity.this, "Failed to play video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + "seek")) {
                Toast.makeText(MainActivity.this, "Failed to seek to new position", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + "videostart")){
                mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "videostart");
                Toast.makeText(MainActivity.this, "Failed to initiate video play", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
            mMessages.remove(failureInfo.getMessageId());
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            String msg = message.getTextBody();
            if(msg.startsWith(mMsgSplitter + "friendadd")){
                mMessageQueue.add(message);
                if(!mIsFirstMessage){
                    if(mDialog != null && !mDialog.isShowing() && !mMessageQueue.isEmpty()){
                        showNextMessage();
                    }
                }else{
                    mIsFirstMessage = false;
                    showNextMessage();
                }
            }else{
                if (msg.startsWith(mMsgSplitter + "addtoplaylist")) {
                    ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(message.getTextBody().split(mMsgSplitter)));
                    String title = msgSplit.get(2);
                    String thumbnail = msgSplit.get(3);
                    String id = msgSplit.get(4);
                    String duration = msgSplit.get(5);

                    WeTubeApplication.getSharedDataSource().getPlaylist().add(new PlaylistItem(title, thumbnail, id, WeTubeApplication.getSharedDataSource().getPlaylist().size() + 1, duration));
                    mPlaylistSize.setText(mCurrentPlaylistIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    int size = pendingPlaylistAdditions.size();
                    for(int i = 0; i < size; i++){
                        if(title.equals(pendingPlaylistAdditions.get(i).getTitle())){
                            pendingPlaylistAdditions.remove(i);
                        }
                    }
                } else if (msg.startsWith(mMsgSplitter + "linkedvideo")) {
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(message.getTextBody(), MessageItem.INCOMING_MSG));
                    mMessageItemAdapter.notifyDataSetChanged();
                    mMessageRecyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
                } else if (msg.startsWith(mMsgSplitter + "deleteitemplaylist")) {
                    ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                    String index = msgSplit.get(2);
                    int i = Integer.parseInt(index);
                    if(WeTubeApplication.getSharedDataSource().getPlaylist().size() > 0){
                        WeTubeApplication.getSharedDataSource().getPlaylist().remove(i);
                        List<PlaylistItem> list = WeTubeApplication.getSharedDataSource().getPlaylist();
                        int size = list.size();
                        for(int j = 0; j < size; j++){
                            list.get(j).setIndex(j+1);
                        }
                    }
                    if(mPlaylistIDs.size() > 0){
                        mPlaylistIDs.remove(Integer.parseInt(index));
                    }
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    int item = mCurrentPlaylistIndex + 1;

                    if(i < mCurrentPlaylistIndex && mCurrentPlaylistIndex != 0){
                        --mCurrentPlaylistIndex;
                        mPlaylistSize.setText(item + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else if(i > mCurrentPlaylistIndex && mCurrentPlaylistIndex != 0){
                        mPlaylistSize.setText(item + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else{
                        mCurrentPlaylistIndex = 0;
                        mPlaylistSize.setText(mCurrentPlaylistIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }
                } else if(msg.startsWith(mMsgSplitter + "passcontroller")){
                    WeTubeApplication.getSharedDataSource().setSessionController(true);
                    invalidateOptionsMenu();
                    mPlaylistItemAdapter.notifyDataSetChanged();
                    mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    Toast.makeText(MainActivity.this, mName + " has given you control", Toast.LENGTH_SHORT).show();
                } else if(msg.startsWith(mMsgSplitter + "playliststart")){
                    if(WeTubeApplication.getSharedDataSource().isPlayerVisible()){
                        mYouTubePlayer.loadVideos(mPlaylistIDs, 0, 100);
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                        mCurrentPlaylistIndex = 0;
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
                        mPlaylistItemAdapter.notifyDataSetChanged();
                        String index = String.valueOf(mCurrentPlaylistIndex + 1);
                        mPlaylistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else{
                        disableSearch();
                        WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                        mCurrentPlaylistIndex = 0;
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
                        mPlaylistItemAdapter.notifyDataSetChanged();

                        mCurrentPlaylistIndex = 0;
                        mPlaylistIDs.clear();
                        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                        int size = videos.size();
                        for(int i = 0; i < size; i++) {
                            mPlaylistIDs.add(videos.get(i).getId());
                        }

                        getFragmentManager()
                                .beginTransaction()
                                .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                                .show(mPlayerFragment)
                                .addToBackStack(null)
                                .commit();

                        mVideoList.setVisibility(View.GONE);
                        mVideoChatDivider.setVisibility(View.VISIBLE);
                        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                        String index = String.valueOf(mCurrentPlaylistIndex + 1);
                        mPlaylistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                        mYouTubePlayer.loadVideos(mPlaylistIDs, 0, 100);
                    }
                }else if(msg.startsWith(mMsgSplitter + "playlistnext")){
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                    mCurrentPlaylistIndex++;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    String index = String.valueOf(mCurrentPlaylistIndex + 1);
                    mPlaylistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    mYouTubePlayer.next();
                }else if(msg.startsWith(mMsgSplitter + "playlistprev")) {
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                    mCurrentPlaylistIndex--;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    String index = String.valueOf(mCurrentPlaylistIndex + 1);
                    mPlaylistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());

                    mYouTubePlayer.previous();
                }else if(msg.startsWith(mMsgSplitter + "playlistindex")) {
                    ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                    String index = msgSplit.get(2);
                    int video = Integer.parseInt(index);

                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(video).setSelected(true);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    mCurrentPlaylistIndex = video;

                    String indexItem = String.valueOf(mCurrentPlaylistIndex + 1);
                    mPlaylistSize.setText(indexItem + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());

                    if (mPlayerFragment.isVisible()) {
                        mYouTubePlayer.loadVideos(mPlaylistIDs, video, 0);
                    } else {
                        disableSearch();
                        WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
                        mPlaylistIDs.clear();
                        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                        int size = videos.size();
                        for (int i = 0; i < size; i++) {
                            mPlaylistIDs.add(videos.get(i).getId());
                        }

                        getFragmentManager()
                                .beginTransaction()
                                .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                                .show(mPlayerFragment)
                                .addToBackStack(null)
                                .commit();

                        mVideoList.setVisibility(View.GONE);
                        mVideoChatDivider.setVisibility(View.VISIBLE);
                        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                        mYouTubePlayer.loadVideos(mPlaylistIDs, video, 1);
                    }
                }else if(msg.startsWith(mMsgSplitter + "playlistexit")){
                    getFragmentManager().popBackStack();

                    mVideoList.setVisibility(View.VISIBLE);
                    mToolbar.setVisibility(View.VISIBLE);
                    mVideoChatDivider.setVisibility(View.GONE);
                    enableSearch();
                    MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    mCurrentPlaylistIndex = 0;

                    WeTubeApplication.getSharedDataSource().setPlayerVisible(false);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    mPlaylistSize.setText(mCurrentPlaylistIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(mMsgSplitter + "pause")) {
                    try{
                        mYouTubePlayer.pause();
                    }catch(NullPointerException e){

                    }

                }else if(msg.startsWith(mMsgSplitter + "play")) {
                    mYouTubePlayer.play();
                }else if(msg.startsWith(mMsgSplitter + "seek")) {
                    ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                    int seek = Integer.parseInt(msgSplit.get(2));
                    mYouTubePlayer.seekToMillis(seek);
                }else if(msg.startsWith(mMsgSplitter + "videostart")){
                    mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                }else if(msg.startsWith((mMsgSplitter + "videoended"))) {
                    mHasTheirVideoEnded = true;
                    if (WeTubeApplication.getSharedDataSource().isSessionController() && mHasYourVideoEnded) {
                        mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    }
                }else if(msg.startsWith(mMsgSplitter + "sessionend")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(mName + " has left the session");

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                            user.setSessionStatus(false);
                            user.saveInBackground();

                            mYouTubePlayer.release();
                            MainActivity.super.onBackPressed();
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }else if(msg.startsWith(mMsgSplitter + "watchingad")){
                    //mYouTubePlayer.pause();
                    Toast.makeText(WeTubeApplication.getSharedInstance(), "Your video is paused while "
                            + WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName()
                            + " is viewing an advertisement", Toast.LENGTH_LONG).show();
                    if(WeTubeApplication.getSharedDataSource().isSessionController()){
                        mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    }
                }else{
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(msg, MessageItem.INCOMING_MSG));
                    mMessageItemAdapter.notifyDataSetChanged();
                    mMessageRecyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);

                    if(mIsFullscreen){
                        Toast toast = Toast.makeText(WeTubeApplication.getSharedInstance(), msg, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.TOP, 0, 0);
                        toast.show();
                    }
                }
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {
            mMessages.put(message.getMessageId(), message.getTextBody());
        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
            String msg = mMessages.get(deliveryInfo.getMessageId());
            if(msg != null){
                if(msg.startsWith(mMsgSplitter + "addtoplaylist")){
                    ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                    String title = msgSplit.get(2);
                    String thumbnail = msgSplit.get(3);
                    String id = msgSplit.get(4);
                    String duration = msgSplit.get(5);

                    WeTubeApplication.getSharedDataSource().getPlaylist().add(new PlaylistItem(title, thumbnail, id, WeTubeApplication.getSharedDataSource().getPlaylist().size() + 1, duration));
                    mPlaylistSize.setText(mCurrentPlaylistIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    int size = pendingPlaylistAdditions.size();
                    for(int i = 0; i < size; i++){
                        if(title.equals(pendingPlaylistAdditions.get(i).getTitle())){
                            pendingPlaylistAdditions.remove(i);
                        }
                    }
                }else if(msg.startsWith(mMsgSplitter + "linkedvideo")){
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(msg, MessageItem.OUTGOING_MSG));
                    mMessageItemAdapter.notifyDataSetChanged();
                    mMessageRecyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
                }else if(msg.startsWith(mMsgSplitter + "deleteitemplaylist")){
                    ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                    String index = msgSplit.get(2);
                    int i = Integer.parseInt(index);
                    if(WeTubeApplication.getSharedDataSource().getPlaylist().size() > 0){
                        WeTubeApplication.getSharedDataSource().getPlaylist().remove(i);
                        List<PlaylistItem> list = WeTubeApplication.getSharedDataSource().getPlaylist();
                        int size = list.size();
                        for(int j = 0; j < size; j++){
                            list.get(j).setIndex(j+1);
                        }
                    }
                    if(mPlaylistIDs.size() > 0){
                        mPlaylistIDs.remove(Integer.parseInt(index));
                    }
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    int item = mCurrentPlaylistIndex + 1;
                    if(i < mCurrentPlaylistIndex && mCurrentPlaylistIndex != 0){
                        --mCurrentPlaylistIndex;
                        mPlaylistSize.setText(item + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else if(i > mCurrentPlaylistIndex && mCurrentPlaylistIndex != 0){
                        mPlaylistSize.setText(item + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else{
                        mCurrentPlaylistIndex = 0;
                        mPlaylistSize.setText(mCurrentPlaylistIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }
                }else if(msg.startsWith(mMsgSplitter + "passcontroller")){
                    WeTubeApplication.getSharedDataSource().setSessionController(false);
                    invalidateOptionsMenu();
                    mPlaylistItemAdapter.notifyDataSetChanged();
                    mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    Toast.makeText(MainActivity.this, "You have given control to " + mName, Toast.LENGTH_SHORT).show();
                }else if(msg.startsWith(mMsgSplitter + "playliststart")) {
                    if(WeTubeApplication.getSharedDataSource().isPlayerVisible()){
                        mYouTubePlayer.loadVideos(mPlaylistIDs, 0, 100);
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                        mCurrentPlaylistIndex = 0;
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
                        mPlaylistItemAdapter.notifyDataSetChanged();
                        String index = String.valueOf(mCurrentPlaylistIndex + 1);
                        mPlaylistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else{
                        disableSearch();
                        WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                        mCurrentPlaylistIndex = 0;
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
                        mPlaylistItemAdapter.notifyDataSetChanged();

                        mPlaylistIDs.clear();
                        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                        int size = videos.size();
                        for (int i = 0; i < size; i++) {
                            mPlaylistIDs.add(videos.get(i).getId());
                        }

                        getFragmentManager()
                                .beginTransaction()
                                .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                                .show(mPlayerFragment)
                                .addToBackStack(null)
                                .commit();

                        mVideoList.setVisibility(View.GONE);
                        //mToolbar.setVisibility(View.GONE);
                        mVideoChatDivider.setVisibility(View.VISIBLE);
                        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                        String index = String.valueOf(mCurrentPlaylistIndex + 1);
                        mPlaylistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                        mYouTubePlayer.loadVideos(mPlaylistIDs, 0, 100);
                    }
                }else if(msg.startsWith(mMsgSplitter + "playlistindex")) {
                    ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                    String index = msgSplit.get(2);
                    int video = Integer.parseInt(index);

                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(video).setSelected(true);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    mCurrentPlaylistIndex = video;
                    String indexItem = String.valueOf(video + 1);

                    mPlaylistSize.setText(indexItem + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    if(mPlayerFragment.isVisible()){
                        mYouTubePlayer.loadVideos(mPlaylistIDs, video, 0);
                    }else{
                        disableSearch();
                        WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
                        mPlaylistIDs.clear();
                        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                        int size = videos.size();
                        for (int i = 0; i < size; i++) {
                            mPlaylistIDs.add(videos.get(i).getId());
                        }

                        getFragmentManager()
                                .beginTransaction()
                                .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                                .show(mPlayerFragment)
                                .addToBackStack(null)
                                .commit();

                        mVideoList.setVisibility(View.GONE);
                        //mToolbar.setVisibility(View.GONE);
                        mVideoChatDivider.setVisibility(View.VISIBLE);
                        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                        mYouTubePlayer.loadVideos(mPlaylistIDs, video, 0);
                    }
                }else if(msg.startsWith(mMsgSplitter + "pause")) {
                    mIsPaused = true;
                }else if(msg.startsWith(mMsgSplitter + "playlistprev")){
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                    mCurrentPlaylistIndex--;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    String index = String.valueOf(mCurrentPlaylistIndex + 1);
                    mPlaylistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(mMsgSplitter + "playlistnext")) {
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
                    mCurrentPlaylistIndex++;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    String index = String.valueOf(mCurrentPlaylistIndex + 1);
                    mPlaylistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(mMsgSplitter + "playlistexit")) {
                    getFragmentManager().popBackStack();

                    mVideoList.setVisibility(View.VISIBLE);
                    mToolbar.setVisibility(View.VISIBLE);
                    mVideoChatDivider.setVisibility(View.GONE);
                    enableSearch();
                    MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    mCurrentPlaylistIndex = 0;

                    WeTubeApplication.getSharedDataSource().setPlayerVisible(false);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    mPlaylistSize.setText(mCurrentPlaylistIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(mMsgSplitter + "sessionend")) {
                    WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                    user.setSessionStatus(false);
                    user.saveInBackground();

                    WeTubeApplication.getSharedDataSource().getPlaylist().clear();
                    WeTubeApplication.getSharedDataSource().getMessages().clear();
                    mYouTubePlayer.release();
                    MainActivity.super.onBackPressed();
                }else if(msg.startsWith(mMsgSplitter + "play") || msg.startsWith(mMsgSplitter + "friendaccept") || msg.startsWith(mMsgSplitter + "frienddecline")
                        || msg.startsWith(mMsgSplitter + "sessionaccept") || msg.startsWith(mMsgSplitter +  "friendfull") || msg.startsWith(mMsgSplitter + "blockuser")
                        || msg.startsWith(mMsgSplitter + "seek") || msg.startsWith(mMsgSplitter + "videostart") || msg.startsWith(mMsgSplitter + "watchingad")){
                    //Do Nothing
                }else{
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(msg, MessageItem.OUTGOING_MSG));
                    mMessageItemAdapter.notifyDataSetChanged();
                    mMessageRecyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
                }
                mMessages.remove(deliveryInfo.getMessageId());
            }
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }

    public void enableSearch() {
        searchViewItem.setVisible(true);
        searchViewItem.setEnabled(true);
    }

    public void disableSearch() {
        searchViewItem.setVisible(false);
        searchViewItem.setEnabled(false);
    }
}
