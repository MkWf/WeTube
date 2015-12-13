package com.gmail.markdevw.wetube.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
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
        mToolbar.setTitle(getString(R.string.toolbar_user_in_session_with) + mName);
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
    public void onVideoItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem) {
        if(pendingPlaylistAdditions.contains(videoItem)){
            return;
        }

        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
        int size = videos.size();
        for(int i = 0; i < size; i++){
            if(videos.get(i).getId().equals(videoItem.getId())){
                Toast.makeText(this, R.string.video_already_in_playlist, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(WeTubeApplication.getSharedDataSource().getPlaylist().size() < MAX_PLAYLIST_SIZE){
            if(WeTubeApplication.getSharedDataSource().isSessionController()){
                pendingPlaylistAdditions.add(videoItem);
                mMessageService.sendMessage(mId,
                        mMsgSplitter + getString(R.string.sinch_addToPlaylist) +
                        mMsgSplitter + videoItem.getTitle() +
                        mMsgSplitter + videoItem.getThumbnailURL() +
                        mMsgSplitter + videoItem.getId() +
                        mMsgSplitter + videoItem.getDuration());
            }else{
                String message = mMsgSplitter + getString(R.string.sinch_linkedVideo) +
                        mMsgSplitter + videoItem.getTitle() +
                        mMsgSplitter + videoItem.getThumbnailURL() +
                        mMsgSplitter + videoItem.getId() +
                        mMsgSplitter + videoItem.getDuration();
                mMessageService.sendMessage(mId, message);
            }
        }else{
            Toast.makeText(this, R.string.max_playlist_size, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, R.string.video_already_in_playlist, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        VideoItem item = new VideoItem();
        item.setTitle(title);
        pendingPlaylistAdditions.add(item);
        if(WeTubeApplication.getSharedDataSource().getPlaylist().size() < MAX_PLAYLIST_SIZE){
            if(WeTubeApplication.getSharedDataSource().isSessionController()){
                mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                        mMsgSplitter + getString(R.string.sinch_addToPlaylist) +
                        mMsgSplitter + title +
                        mMsgSplitter + thumbnail +
                        mMsgSplitter + id +
                        mMsgSplitter + duration);
            }else{
                Toast.makeText(this, R.string.toast_add_video_to_playlist_not_controller, Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, getString(R.string.max_playlist_size), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(R.string.return_to_video_search_dialog_title);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            mMsgSplitter + getString(R.string.sinch_playlistExit) +
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
            builder.setTitle(R.string.exit_session_dialog_title);

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            mMsgSplitter + getString(R.string.sinch_sessionEnd) +
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
        Toast.makeText(getApplicationContext(), R.string.youtube_init_fail, Toast.LENGTH_LONG).show();
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
            mIsFullscreen = true;
            if(mPlayerFragment.isVisible()){
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
            Toast.makeText(getApplicationContext(), R.string.message_empty, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, R.string.search_empty, Toast.LENGTH_LONG).show();
                }else{
                    mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            getString(R.string.search_start_partial1) + query.toUpperCase() + getString(R.string.search_start_partial2));
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
                    Toast.makeText(this, R.string.playlist_empty, Toast.LENGTH_SHORT).show();
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }else{
                    mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            mMsgSplitter + getString(R.string.sinch_playlistStart));
                }
                break;
            case R.id.action_pass_control:
                mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                        mMsgSplitter + getString(R.string.sinch_controllerReceive));
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
                    mMsgSplitter + getString(R.string.sinch_playlistIndex) +
                    mMsgSplitter + String.valueOf(index));
        }
    }

    @Override
    public void onDeleteItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem) {
        int index = WeTubeApplication.getSharedDataSource().getPlaylist().indexOf(playlistItem);
        playlistItem.setToBeDeleted(true);
        mMessageService.sendMessage(mId,
                mMsgSplitter + getString(R.string.sinch_playlistDeleteItem) +
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
                mMsgSplitter + getString(R.string.sinch_watchingAd));
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
                        mMsgSplitter + getString(R.string.sinch_videoStart));
            }
        }else if(WeTubeApplication.getSharedDataSource().isSessionController() && !mIsAdPlaying) {
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                    mMsgSplitter + getString(R.string.sinch_play) + mMsgSplitter);
        }
    }

    @Override
    public void onPaused() {
        if(mIsRecoveringFromAd){
            mIsRecoveringFromAd = false;
        }
       else if(WeTubeApplication.getSharedDataSource().isSessionController() && !mIsAdPlaying){
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                    mMsgSplitter + getString(R.string.sinch_pause) + mMsgSplitter);
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
                    mMsgSplitter + getString(R.string.sinch_seek) +
                    mMsgSplitter + String.valueOf(i));
        }
    }

    @Override
    public void onPrevious() {
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                    mMsgSplitter + getString(R.string.sinch_playlistPrev));
        }
    }

    @Override
    public void onNext() {
       if(WeTubeApplication.getSharedDataSource().isSessionController() && !mHasYourVideoEnded){
           mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                   mMsgSplitter + getString(R.string.sinch_playlistNext));
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
            if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_addToPlaylist))){
                ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to add " + title + " to playlist", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_linkedVideo))){
                ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to add " + title + " to chat", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistDeleteItem))){
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
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_controllerReceive))){
                Toast.makeText(MainActivity.this, "Failed to pass controls to "
                        + WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName(), Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistIndex))){
                Toast.makeText(MainActivity.this, "Failed to start video", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistStart))){
                Toast.makeText(MainActivity.this, "Failed to start playlist", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistNext))) {
                Toast.makeText(MainActivity.this, "Failed to play next video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistPrev))){
                Toast.makeText(MainActivity.this, "Failed to play previous video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_pause))) {
                mYouTubePlayer.play();
                Toast.makeText(MainActivity.this, "Failed to pause video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_play))){
                mYouTubePlayer.pause();
                Toast.makeText(MainActivity.this, "Failed to play video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_seek))) {
                Toast.makeText(MainActivity.this, "Failed to seek to new position", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_videoStart))){
                mMessageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), getString(R.string.sinch_videoStart));
                Toast.makeText(MainActivity.this, "Failed to initiate video play", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
            mMessages.remove(failureInfo.getMessageId());
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            String msg = message.getTextBody();
            if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_friendAdd))){
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
                if (msg.startsWith(mMsgSplitter + getString(R.string.sinch_addToPlaylist))) {
                    addVideoToPlaylist(msg);
                } else if (msg.startsWith(mMsgSplitter + getString(R.string.sinch_linkedVideo))) {
                    addMessageToChat(new MessageItem(message.getTextBody(), MessageItem.INCOMING_MSG));
                } else if (msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistDeleteItem))) {
                    removeItemFromPlaylist(msg);
                } else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_controllerReceive))){
                    passController(true);
                } else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistStart))){
                    startVideoPlayback();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistNext))){
                    playlistNext();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistPrev))) {
                    playlistPrev();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistIndex))) {
                    startVideoPlaybackByIndex(msg);
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistExit))){
                    getFragmentManager().popBackStack();

                    mVideoList.setVisibility(View.VISIBLE);
                    mToolbar.setVisibility(View.VISIBLE);
                    mVideoChatDivider.setVisibility(View.GONE);
                    enableSearch();
                    MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    mCurrentPlaylistIndex = 0;

                    WeTubeApplication.getSharedDataSource().setPlayerVisible(false);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    mPlaylistSize.setText(mCurrentPlaylistIndex + getString(R.string.playlist_forward_slash) +
                            WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_pause))) {
                    mYouTubePlayer.pause();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_play))) {
                    mYouTubePlayer.play();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_seek))) {
                    ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(msg.split(mMsgSplitter)));
                    int seek = Integer.parseInt(msgSplit.get(2));
                    mYouTubePlayer.seekToMillis(seek);
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_videoStart))){
                    mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                }else if(msg.startsWith((mMsgSplitter + getString(R.string.sinch_videoEnded)))) {
                    mHasTheirVideoEnded = true;
                    if (WeTubeApplication.getSharedDataSource().isSessionController() && mHasYourVideoEnded) {
                        mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    }
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_sessionEnd))) {
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
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_watchingAd))){
                    //mYouTubePlayer.pause();
                    Toast.makeText(WeTubeApplication.getSharedInstance(), getString(R.string.toast_ad_pause_partial1)
                            + WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName()
                            + getString(R.string.toast_ad_pause_partial2), Toast.LENGTH_LONG).show();
                    if(WeTubeApplication.getSharedDataSource().isSessionController()){
                        mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    }
                }else{
                    addMessageToChat(new MessageItem(msg, MessageItem.INCOMING_MSG));

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
                if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_addToPlaylist))){
                    addVideoToPlaylist(msg);
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_linkedVideo))){
                    addMessageToChat(new MessageItem(msg, MessageItem.OUTGOING_MSG));
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistDeleteItem))){
                    removeItemFromPlaylist(msg);
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_controllerReceive))){
                    passController(false);
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistStart))) {
                    startVideoPlayback();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistIndex))) {
                    startVideoPlaybackByIndex(msg);
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_pause))) {
                    mIsPaused = true;
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistPrev))){
                    playlistPrev();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistNext))) {
                    playlistNext();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_playlistExit))) {
                    getFragmentManager().popBackStack();

                    mVideoList.setVisibility(View.VISIBLE);
                    mToolbar.setVisibility(View.VISIBLE);
                    mVideoChatDivider.setVisibility(View.GONE);
                    enableSearch();
                    MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    mCurrentPlaylistIndex = 0;

                    WeTubeApplication.getSharedDataSource().setPlayerVisible(false);
                    mPlaylistItemAdapter.notifyDataSetChanged();

                    mPlaylistSize.setText(mCurrentPlaylistIndex + getString(R.string.playlist_forward_slash) +
                            WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_sessionEnd))) {
                    WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                    user.setSessionStatus(false);
                    user.saveInBackground();

                    WeTubeApplication.getSharedDataSource().getPlaylist().clear();
                    WeTubeApplication.getSharedDataSource().getMessages().clear();
                    mYouTubePlayer.release();
                    MainActivity.super.onBackPressed();
                }else if(msg.startsWith(mMsgSplitter + getString(R.string.sinch_play)) ||
                        msg.startsWith(mMsgSplitter + getString(R.string.sinch_friendAccept)) ||
                        msg.startsWith(mMsgSplitter + getString(R.string.sinch_friendDecline)) ||
                        msg.startsWith(mMsgSplitter + getString(R.string.sinch_sessionAccept)) ||
                        msg.startsWith(mMsgSplitter +  getString(R.string.sinch_friendFull)) ||
                        msg.startsWith(mMsgSplitter + getString(R.string.sinch_blockuser)) ||
                        msg.startsWith(mMsgSplitter + getString(R.string.sinch_seek)) ||
                        msg.startsWith(mMsgSplitter + getString(R.string.sinch_videoStart)) ||
                        msg.startsWith(mMsgSplitter + getString(R.string.sinch_watchingAd))){
                    //Do Nothing
                }else{
                    addMessageToChat(new MessageItem(msg, MessageItem.OUTGOING_MSG));
                }
                mMessages.remove(deliveryInfo.getMessageId());
            }
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }

    /**
     * Can be called either from the video search or during video play
     *
     * @param message  Message sent/received to start playing videos from a playlist selection
     */
    public void startVideoPlaybackByIndex(String message) {
        ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(message.split(mMsgSplitter)));
        String index = msgSplit.get(2);
        int video = Integer.parseInt(index);

        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
        WeTubeApplication.getSharedDataSource().getPlaylist().get(video).setSelected(true);
        mPlaylistItemAdapter.notifyDataSetChanged();

        mCurrentPlaylistIndex = video;
        String indexItem = String.valueOf(video + 1);

        mPlaylistSize.setText(indexItem + getString(R.string.playlist_forward_slash) +
                WeTubeApplication.getSharedDataSource().getPlaylist().size());

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
            videoSearchToVideoPlayTransition();
            mYouTubePlayer.loadVideos(mPlaylistIDs, video, 0);
        }
    }

    /**
     * Moves back in the playlist by updating the playlist counter and the selected video.
     * Notifies the YouTube Player to move to the previous video
     */
    public void playlistPrev() {
        if(!WeTubeApplication.getSharedDataSource().isSessionController()){
            mYouTubePlayer.previous();
        }

        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
        mCurrentPlaylistIndex--;
        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
        mPlaylistItemAdapter.notifyDataSetChanged();

        String index = String.valueOf(mCurrentPlaylistIndex + 1);
        mPlaylistSize.setText(index + getString(R.string.playlist_forward_slash) +
                WeTubeApplication.getSharedDataSource().getPlaylist().size());

    }

    /**
     * Moves forward in the playlist by updating the playlist counter and the selected video.
     * Notifies the YouTube Player to move to the next video
     */
    public void playlistNext() {
        if(!WeTubeApplication.getSharedDataSource().isSessionController()){
            mYouTubePlayer.next();
        }

        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
        mCurrentPlaylistIndex++;
        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
        mPlaylistItemAdapter.notifyDataSetChanged();

        String index = String.valueOf(mCurrentPlaylistIndex + 1);
        mPlaylistSize.setText(index + getString(R.string.playlist_forward_slash) +
                WeTubeApplication.getSharedDataSource().getPlaylist().size());

    }

    /**
     * Performs all the actions necessary to take the user from video search to watching videos
     */
    public void startVideoPlayback() {
        if(WeTubeApplication.getSharedDataSource().isPlayerVisible()){
            updatePlaylistForVideoPlayback();
        }else{
            disableSearch();
            videoSearchToVideoPlayTransition();
            updatePlaylistForVideoPlayback();
        }
    }

    /**
     * Will set the playlist and YouTube player to the start of the playlist.
     */
    public void updatePlaylistForVideoPlayback() {
        mPlaylistIDs.clear();
        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
        int size = videos.size();
        for (int i = 0; i < size; i++) {
            mPlaylistIDs.add(videos.get(i).getId());
        }
        mYouTubePlayer.loadVideos(mPlaylistIDs, 0, 100);

        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(false);
        mCurrentPlaylistIndex = 0;
        WeTubeApplication.getSharedDataSource().getPlaylist().get(mCurrentPlaylistIndex).setSelected(true);
        WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
        mPlaylistItemAdapter.notifyDataSetChanged();

        String index = String.valueOf(mCurrentPlaylistIndex + 1);
        mPlaylistSize.setText(index + getString(R.string.playlist_forward_slash) +
                WeTubeApplication.getSharedDataSource().getPlaylist().size());
    }

    /**
     * Hides the video search list and displays the YouTube player
     */
    public void videoSearchToVideoPlayTransition() {
        getFragmentManager()
                .beginTransaction()
                .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                .show(mPlayerFragment)
                .addToBackStack(null)
                .commit();

        mVideoList.setVisibility(View.GONE);
        mVideoChatDivider.setVisibility(View.VISIBLE);
        MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    /**
     * Updates the Toolbar and YouTube player based on the change in controller status
     *
     * @param isReceivingControl Are we passing the controller or receiving it
     */
    public void passController(boolean isReceivingControl){
        WeTubeApplication.getSharedDataSource().setSessionController(isReceivingControl);
        invalidateOptionsMenu();
        mPlaylistItemAdapter.notifyDataSetChanged();

        if(isReceivingControl){
            mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
            Toast.makeText(MainActivity.this, mName + getString(R.string.controller_receive), Toast.LENGTH_SHORT).show();
        }else{
            mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
            Toast.makeText(MainActivity.this, getString(R.string.controller_pass) + mName, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     *
     * After removing the PlaylistItem, the video ids that are passed to the YouTube Player to
     * create a playlist must also be updated. The playlist counter is also updated.
     *
     * @param message  Message sent/received to delete a specific item in the playlist
     */
    public void removeItemFromPlaylist(String message) {
        ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(message.split(mMsgSplitter)));
        String index = msgSplit.get(2);
        int ind = Integer.parseInt(index);

        List<PlaylistItem> list = WeTubeApplication.getSharedDataSource().getPlaylist();
        if(list.size() > 0){
            list.remove(ind);
            int size = list.size();
            for(int j = 0; j < size; j++){
                list.get(j).setIndex(j+1);
            }
        }
        mPlaylistItemAdapter.notifyDataSetChanged();

        if (mPlaylistIDs.size() > 0) {
            mPlaylistIDs.remove(ind);
        }

        updatePlaylistIndex(ind);
    }

    /**
     * Updates the Playlist text based on the index of the removed item and how it relates to
     * the position of the current playlist index.
     *
     * Example:  The current playlist text is "7/10", meaning its on the 7th video out of a total of 10 videos
     * in the playlist.
     *
     * There are 3 scenarios to consider:
     *      1)indexRemoved has a lower index than current playlist index
     *          7/10 -> 6/9  and mCurrentPlaylistIndex goes from 7 to 6
     *      2)indexRemoved has a higher index than current playlist index
     *          7/10 -> 7/9 and mCurrentPlaylistIndex is not effected
     *      3)indexRemoved is the current playlist index
     *          7/10 -> 0/9 since there's no longer a current index until the user selects a new video
     *
     * @param indexRemoved  The index of the PlaylistItem that is being removed
     */
    public void updatePlaylistIndex(int indexRemoved) {
        int index = mCurrentPlaylistIndex + 1;
        if(indexRemoved < mCurrentPlaylistIndex && mCurrentPlaylistIndex != 0){
            --mCurrentPlaylistIndex;
            mPlaylistSize.setText(index + getString(R.string.playlist_forward_slash) + WeTubeApplication.getSharedDataSource().getPlaylist().size());
        }else if(indexRemoved > mCurrentPlaylistIndex && mCurrentPlaylistIndex != 0){
            mPlaylistSize.setText(index + getString(R.string.playlist_forward_slash) + WeTubeApplication.getSharedDataSource().getPlaylist().size());
        }else{
            mCurrentPlaylistIndex = 0;
            mPlaylistSize.setText(mCurrentPlaylistIndex + getString(R.string.playlist_forward_slash) +
                    WeTubeApplication.getSharedDataSource().getPlaylist().size());
        }
    }

    /**
     * Adds a new MessageItem to the messages list to be displayed in the chat.
     *
     * @param messageItem   The message to be added to the messages list
     */
    public void addMessageToChat(MessageItem messageItem) {
        WeTubeApplication.getSharedDataSource().getMessages().add(messageItem);
        mMessageItemAdapter.notifyDataSetChanged();
        mMessageRecyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
    }

    /**
     * When the session controller clicks on a video to add to the playlist, a message is created that stores
     * all the information necessary to create a new PlaylistItem and sent to the other user.
     *
     * @param message
     */
    public void addVideoToPlaylist(String message) {
        ArrayList<String> msgSplit = new ArrayList<>(Arrays.asList(message.split(mMsgSplitter)));
        String title = msgSplit.get(2);
        String thumbnail = msgSplit.get(3);
        String id = msgSplit.get(4);
        String duration = msgSplit.get(5);

        WeTubeApplication.getSharedDataSource().getPlaylist().add(
                new PlaylistItem(title, thumbnail, id, WeTubeApplication.getSharedDataSource().getPlaylist().size() + 1, duration));
        mPlaylistSize.setText(mCurrentPlaylistIndex +
                getString(R.string.playlist_forward_slash) +
                WeTubeApplication.getSharedDataSource().getPlaylist().size());
        mPlaylistItemAdapter.notifyDataSetChanged();

        int size = pendingPlaylistAdditions.size();
        for(int i = 0; i < size; i++){
            if(title.equals(pendingPlaylistAdditions.get(i).getTitle())){
                pendingPlaylistAdditions.remove(i);
                size -= 1;
            }
        }
    }

    /**
     * When a user transitions from video play to video search, the SearchView menu item
     * returns to the toolbar
     */
    public void enableSearch() {
        searchViewItem.setVisible(true);
        searchViewItem.setEnabled(true);
    }

    /**
     * When a user transitions from video search to video play, the SearchView menu item
     * is no longer used
     */
    public void disableSearch() {
        searchViewItem.setVisible(false);
        searchViewItem.setEnabled(false);
    }
}
