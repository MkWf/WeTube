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
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import com.gmail.markdevw.wetube.R;
import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.adapters.MessageItemAdapter;
import com.gmail.markdevw.wetube.adapters.PlaylistItemAdapter;
import com.gmail.markdevw.wetube.adapters.VideoItemAdapter;
import com.gmail.markdevw.wetube.api.model.MessageItem;
import com.gmail.markdevw.wetube.api.model.PlaylistItem;
import com.gmail.markdevw.wetube.api.model.VideoItem;
import com.gmail.markdevw.wetube.fragments.VideoListFragment;
import com.gmail.markdevw.wetube.services.MessageService;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.parse.ParseUser;
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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/*MAKE SURE BOTH USERS HAVE CORRECT PLAYLISTS, GO THROUGH CODE AGAIN. MAYBE MESSAGES ARENT BEING DELIVERED AND HAVE TO USE ONMESSAGEDELIVERED
NO REASON TO LOADVIDEOS BY SENDING MESSAGE WITH ALL THE VIDEOIDS TOGETHER. OTHER USER ALREADY HAS THE PLAYLISTS, JUST USE FOR LOOP TO LOOP THROUGH
PLAYLISTSITEM.GETID TO CREATE THE PLAYLIST INSTEAD

COPY-PASTE ALL THE ONINCOMING AND PLAYBACK STUFF INTO SUBLIME AND JUST HAVE THE PLAYLIST STUFF IN CODE FOR NOW TO GET IT WORKING THEN RE-ADD ALL
THE OTHER STUFF BACK*/


/**
 * Created by Mark on 3/24/2015.
 */

public class MainActivity extends ActionBarActivity implements VideoListFragment.Delegate, YouTubePlayer.OnInitializedListener,
        YouTubePlayer.OnFullscreenListener, View.OnClickListener,
        YouTubePlayer.PlaybackEventListener, MessageItemAdapter.Delegate, YouTubePlayer.PlaylistEventListener, PlaylistItemAdapter.Delegate, DrawerLayout.DrawerListener, YouTubePlayer.PlayerStateChangeListener {

    Handler handler;
    Toolbar toolbar;
    FrameLayout list;
    LinearLayout chatbar;
    YouTubePlayerFragment playerFragment;
    YouTubePlayer youTubePlayer;
    String currentVideo;
    boolean isFullscreen;
    boolean isPortrait;
    private static final int LANDSCAPE_VIDEO_PADDING_DP = 5;
    private MessageService.MessageServiceInterface messageService;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MessageClientListener messageClientListener = new MyMessageClientListener();
    private RecyclerView recyclerView;
    private RecyclerView playListRecyclerView;
    private MessageItemAdapter messageItemAdapter;
    private PlaylistItemAdapter playlistItemAdapter;
    private EditText messageField;
    private Button sendMessage;
    private FrameLayout videoList;
    private View videoChatDivider;
    private final String name = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName();
    private final String id = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId();
    private List<String> playlistIds = new ArrayList<>();
    private int playlistIndex = 0;
    private String msgSplitter = "=-=-=";
    private HashMap<String, String> messages = new HashMap<String, String>();
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;

    private final int MESSAGE = 0;
    private final int VIDEO_START = 1;
    private final int VIDEO_PAUSE = 2;
    private final int VIDEO_UNPAUSE = 3;
    private final int VIDEO_SEEK = 4;
    private final int VIDEO_BUFFER = 5;
    private final int VIDEO_NEXT = 6;
    private final int VIDEO_PREV = 7;

    private int messageType;
    private boolean isPaused = false;
    private boolean hasVideoEnded = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindService(new Intent(this, MessageService.class), serviceConnection, BIND_AUTO_CREATE);

        list = (FrameLayout) findViewById(R.id.fl_activity_video_list);

        chatbar = (LinearLayout) findViewById(R.id.ll_activity_main_chat_bar);

        playerFragment = (YouTubePlayerFragment)getFragmentManager()
                .findFragmentById(R.id.youtubeplayerfragment);
        playerFragment.initialize(WeTubeApplication.getSharedDataSource().getAPI_KEY(), this);

        videoList = (FrameLayout) findViewById(R.id.fl_activity_video_list);

        getFragmentManager()
                .beginTransaction()
                .hide(playerFragment)
                .commit();

        toolbar = (Toolbar) findViewById(R.id.tb_activity_main);
        toolbar.setTitle("User: " + name);
        setSupportActionBar(toolbar);

        videoChatDivider = findViewById(R.id.horizontal_line_video);

        messageItemAdapter = new MessageItemAdapter();
        messageItemAdapter.setDelegate(this);

        recyclerView = (RecyclerView) findViewById(R.id.rv_activity_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(messageItemAdapter);

        playlistItemAdapter = new PlaylistItemAdapter();
        playlistItemAdapter.setDelegate(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.dl_activity_main);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0);
        drawerLayout.setDrawerListener(drawerToggle);

        playListRecyclerView = (RecyclerView) findViewById(R.id.rv_nav_activity_main);
        playListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        playListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        playListRecyclerView.setAdapter(playlistItemAdapter);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.fl_activity_video_list, new VideoListFragment(), "Video")
                .commit();

        messageField = (EditText) findViewById(R.id.activity_main_message_field);
        sendMessage = (Button) findViewById(R.id.activity_main_send_button);
        sendMessage.setOnClickListener(this);

        handler = new Handler();
    }

    @Override
    public void onSearchButtonClicked(VideoListFragment videoListFragment, EditText searchBox) {
        final String search = WeTubeApplication.getSharedDataSource().getCurrentSearch();

        if(search.isEmpty()){
            Toast.makeText(this, "Enter a search keyword first", Toast.LENGTH_LONG).show();
        }else{
            new Thread(){
                public void run(){
                    WeTubeApplication.getSharedDataSource().searchForVideos(search);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Fragment f = getFragmentManager().findFragmentByTag("Video");
                            VideoListFragment vlf = (VideoListFragment)f;
                            vlf.getVideoItemAdapter().notifyDataSetChanged();
                            vlf.getRecyclerView().scrollToPosition(0);
                            toolbar.setTitle("Page: " + WeTubeApplication.getSharedDataSource().getCurrentPage() + "   User: " + name);
                        }
                    });
                }
            }.start();
        }
    }

    @Override
    public void onPrevPageButtonClicked(VideoListFragment videoListFragment, EditText searchBox) {
        final String search = WeTubeApplication.getSharedDataSource().getCurrentSearch();

        new Thread(){
            public void run(){
                WeTubeApplication.getSharedDataSource().searchForVideos(search, WeTubeApplication.getSharedDataSource().getPrevPageToken());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Fragment f = getFragmentManager().findFragmentByTag("Video");
                        VideoListFragment vlf = (VideoListFragment)f;
                        vlf.getVideoItemAdapter().notifyDataSetChanged();
                        vlf.getRecyclerView().scrollToPosition(0);
                        toolbar.setTitle("Page: " + WeTubeApplication.getSharedDataSource().getCurrentPage() + "   User: " + name);
                    }
                });
            }
        }.start();
    }

    @Override
    public void onNextPageButtonClicked(VideoListFragment videoListFragment, EditText searchBox) {
        final String search = WeTubeApplication.getSharedDataSource().getCurrentSearch();

        new Thread(){
            public void run(){
                WeTubeApplication.getSharedDataSource().searchForVideos(search, WeTubeApplication.getSharedDataSource().getNextPageToken());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Fragment f = getFragmentManager().findFragmentByTag("Video");
                        VideoListFragment vlf = (VideoListFragment)f;
                        vlf.getVideoItemAdapter().notifyDataSetChanged();
                        vlf.getRecyclerView().scrollToPosition(0);
                        toolbar.setTitle("Page: " + WeTubeApplication.getSharedDataSource().getCurrentPage());
                    }
                });
            }
        }.start();
    }

    @Override
    public void onVideoItemClicked(VideoItemAdapter itemAdapter, VideoItem videoItem) {
        boolean isMatchFound = false;
        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
        for(int i = 0; i<videos.size(); i++){
            if(videos.get(i).getId().equals(videoItem.getId())){
                Toast.makeText(this, "Video already in playlist", Toast.LENGTH_SHORT).show();
                isMatchFound = true;
                break;
            }
        }

        if(!isMatchFound){
            if(WeTubeApplication.getSharedDataSource().isSessionController()){
                messageService.sendMessage(id, "addtoplaylist" + msgSplitter + videoItem.getTitle() + msgSplitter + videoItem.getThumbnailURL() + msgSplitter
                                               + videoItem.getId());
            }else{
                String message = "linkedvideo" + msgSplitter + videoItem.getTitle() + msgSplitter + videoItem.getThumbnailURL() + msgSplitter
                                    + videoItem.getId();
                messageService.sendMessage(id, message);
            }
        }
    }

    @Override
    public void onMessageVideoItemClicked(MessageItemAdapter itemAdapter, String title, String thumbnail, String id) {
        boolean isMatchFound = false;
        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
        for(int i = 0; i<videos.size(); i++){
            if(videos.get(i).getId().equals(id)){
                Toast.makeText(this, "Video already in playlist", Toast.LENGTH_SHORT).show();
                isMatchFound = true;
                break;
            }
        }

        if(!isMatchFound){
            if(WeTubeApplication.getSharedDataSource().isSessionController()){
                messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "addtoplaylist" + msgSplitter + title + msgSplitter + thumbnail + msgSplitter + id);
            }else{
                Toast.makeText(this, "Only the controller can add videos to the playlist from chat", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0){
            getFragmentManager().popBackStack();
            videoList.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            videoChatDivider.setVisibility(View.GONE);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            playlistIndex = 0;
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Are you sure you want to leave this session?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            "sessionend-" + ParseUser.getCurrentUser().getUsername() + "-" + ParseUser.getCurrentUser().getObjectId());
                    dialog.dismiss();
                    WeTubeApplication.getSharedDataSource().getPlaylist().clear();
                    MainActivity.super.onBackPressed();
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
        this.youTubePlayer = youTubePlayer;
        //this.youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
        this.youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        this.youTubePlayer.setOnFullscreenListener(this);
      //  this.youTubePlayer.setPlaybackEventListener(this);
        this.youTubePlayer.setPlayerStateChangeListener(this);
        this.youTubePlayer.setPlaylistEventListener(this);
        this.youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
      //  if (!b && currentVideo != null) {
         //   this.youTubePlayer.cueVideo(currentVideo);
       // }
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            this.youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
        }else{
            this.youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
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
        this.isFullscreen = isFullscreen;
        layout();
    }

    private void layout() {
        isPortrait =
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (isFullscreen) {
            setLayoutSize(playerFragment.getView(), MATCH_PARENT, MATCH_PARENT);
            chatbar.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
        } else if (isPortrait) {
            setLayoutSize(playerFragment.getView(), MATCH_PARENT, WRAP_CONTENT);
            chatbar.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        } else {
            int screenWidth = dpToPx(getResources().getConfiguration().screenWidthDp);
            setLayoutSize(playerFragment.getView(), MATCH_PARENT, MATCH_PARENT);
            chatbar.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
        }
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private static void setLayoutSize(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    private static void setLayoutSizeAndGravity(View view, int width, int height, int gravity) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.width = width;
        params.height = height;
        params.gravity = gravity;
        view.setLayoutParams(params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);

        WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
        user.setSessionStatus(false);
        user.saveInBackground();

        WeTubeApplication.getSharedDataSource().setCurrentRecipient(null);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.activity_main_send_button){
            sendMessage();
        }
    }

    private void sendMessage() {
        if(messageField.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Type a message first before sending", Toast.LENGTH_LONG).show();
        }else{
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), messageField.getText().toString());
            messageType = MESSAGE;
            WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(messageField.getText().toString(), MessageItem.OUTGOING_MSG));
            messageItemAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
            messageField.setText("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

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
                    drawerLayout.openDrawer(Gravity.LEFT);
                }else{
                    messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "playliststart");
                }
                break;
            case R.id.action_pass_control:
                messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "passcontroller");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onPlayListItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem) {

    }

    @Override
    public void onDeleteItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem) {
        int index = WeTubeApplication.getSharedDataSource().getPlaylist().indexOf(playlistItem);
        messageService.sendMessage(id, "deleteitemplaylist" + msgSplitter + String.valueOf(index));
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
        WeTubeApplication.getSharedDataSource();
    }

    @Override
    public void onVideoStarted() {
        WeTubeApplication.getSharedDataSource();
    }

    @Override
    public void onVideoEnded() {
        hasVideoEnded = true;
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
        WeTubeApplication.getSharedDataSource();
    }

    @Override
    public void onPlaying() {
        if(isPaused){
            isPaused = false;
            messageType = VIDEO_UNPAUSE;
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "/unpause$");
        }
    }

    @Override
    public void onPaused() {
        messageType = VIDEO_PAUSE;
        isPaused = true;
        messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "/pause$");
    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onBuffering(boolean b) {
        if(b){
            messageType = VIDEO_BUFFER;
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "/pause$");
        }else{
            messageType = VIDEO_BUFFER;
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "/unpause$");
        }
    }

    @Override
    public void onSeekTo(int i) {
        //  messageType = VIDEO_SEEK;
        // messageService.sendMessage(id, "/seek$" + i);
    }

    @Override
    public void onPrevious() {
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId()
                    ,"playlistprev");
        }
    }

    @Override
    public void onNext() {
       if(WeTubeApplication.getSharedDataSource().isSessionController() && !hasVideoEnded){
           messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId()
                                ,"playlistnext");
       }
    }

    @Override
    public void onPlaylistEnded() {

    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageService.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
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
            String msg = messages.get(failureInfo.getMessageId());
            if(msg.startsWith("addtoplaylist")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to add " + title + " to playlist", Toast.LENGTH_LONG).show();

                messages.remove(failureInfo.getMessageId());
            }else if(msg.startsWith("linkedvideo")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to add " + title + " to chat", Toast.LENGTH_LONG).show();

                messages.remove(failureInfo.getMessageId());
            }else if(msg.startsWith("deleteitemplaylist")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to delete " + title + " from playlist", Toast.LENGTH_LONG).show();

                messages.remove(failureInfo.getMessageId());
            }else if(msg.startsWith("passcontroller")){
                Toast.makeText(MainActivity.this, "Failed to pass controls to "
                        + WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName(), Toast.LENGTH_LONG).show();

                messages.remove(failureInfo.getMessageId());
            }else if(msg.startsWith("playliststart")){
                Toast.makeText(MainActivity.this, "Failed to start playlist", Toast.LENGTH_LONG).show();

                messages.remove(failureInfo.getMessageId());
            }else if(msg.startsWith("playlistnext")){
                Toast.makeText(MainActivity.this, "Failed to playlist", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            String msg = message.getTextBody();
            if (msg.startsWith("addtoplaylist")) {
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(message.getTextBody().split(msgSplitter)));
                String title = msgSplit.get(1);
                String thumbnail = msgSplit.get(2);
                String id = msgSplit.get(3);

                WeTubeApplication.getSharedDataSource().getPlaylist().add(new PlaylistItem(title, thumbnail, id));
                playlistItemAdapter.notifyDataSetChanged();
            } else if (msg.startsWith("linkedvideo")) {
                WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(message.getTextBody(), MessageItem.INCOMING_MSG));
                messageItemAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
            } else if (msg.startsWith(("deleteitemplaylist"))) {
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                String index = msgSplit.get(1);
                WeTubeApplication.getSharedDataSource().getPlaylist().remove(Integer.parseInt(index));
                playlistItemAdapter.notifyDataSetChanged();
            } else if(msg.startsWith("passcontroller")){
                WeTubeApplication.getSharedDataSource().setSessionController(true);
                invalidateOptionsMenu();
                playlistItemAdapter.notifyDataSetChanged();
                youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                Toast.makeText(MainActivity.this, name + " has given you control", Toast.LENGTH_SHORT).show();
            } else if(msg.startsWith("playliststart")){
                playlistIds.clear();
                List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                for(int i = 0; i<videos.size(); i++) {
                    playlistIds.add(videos.get(i).getId());
                }

                getFragmentManager()
                        .beginTransaction()
                        .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                        .show(playerFragment)
                        .addToBackStack(null)
                        .commit();

                videoList.setVisibility(View.GONE);
                //toolbar.setVisibility(View.GONE);
                videoChatDivider.setVisibility(View.VISIBLE);
                MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                youTubePlayer.loadVideos(playlistIds);
            }else if(msg.startsWith("playlistnext")){
                youTubePlayer.next();
            }else if(msg.startsWith("playlistprev")){
                youTubePlayer.previous();
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {
            messages.put(message.getMessageId(), message.getTextBody());
        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
            String msg = messages.get(deliveryInfo.getMessageId());
            if(msg != null){
                if(msg.startsWith("addtoplaylist")){
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                    String title = msgSplit.get(1);
                    String thumbnail = msgSplit.get(2);
                    String id = msgSplit.get(3);

                    WeTubeApplication.getSharedDataSource().getPlaylist().add(new PlaylistItem(title, thumbnail, id));
                    playlistItemAdapter.notifyDataSetChanged();
                    messages.remove(deliveryInfo.getMessageId());
                }else if(msg.startsWith("linkedvideo")){
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(msg, MessageItem.OUTGOING_MSG));
                    messageItemAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
                    messages.remove(deliveryInfo.getMessageId());
                }else if(msg.startsWith(("deleteitemplaylist"))){
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                    String index = msgSplit.get(1);
                    WeTubeApplication.getSharedDataSource().getPlaylist().remove(Integer.parseInt(index));
                    playlistItemAdapter.notifyDataSetChanged();
                    messages.remove(deliveryInfo.getMessageId());
                }else if(msg.startsWith("passcontroller")){
                    WeTubeApplication.getSharedDataSource().setSessionController(false);
                    invalidateOptionsMenu();
                    playlistItemAdapter.notifyDataSetChanged();
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    messages.remove(deliveryInfo.getMessageId());
                    Toast.makeText(MainActivity.this, "You have give control to " + name, Toast.LENGTH_SHORT).show();
                }else if(msg.startsWith("playliststart")){
                    playlistIds.clear();
                    List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                    for(int i = 0; i<videos.size(); i++) {
                        playlistIds.add(videos.get(i).getId());
                    }

                    getFragmentManager()
                            .beginTransaction()
                            .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                            .show(playerFragment)
                            .addToBackStack(null)
                            .commit();

                    videoList.setVisibility(View.GONE);
                    //toolbar.setVisibility(View.GONE);
                    videoChatDivider.setVisibility(View.VISIBLE);
                    MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                    youTubePlayer.loadVideos(playlistIds);
                    messages.remove(deliveryInfo.getMessageId());
                }else if(msg.startsWith("playlistnext")){

                }
            }
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }
}

 /*   @Override
    public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
        if(messageType == VIDEO_START){
            youTubePlayer.loadVideos(playlistIds);
        }else if(messageType == VIDEO_PAUSE){
            youTubePlayer.pause();
        }
    }

            @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            String msg = message.getTextBody();
            if(msg.startsWith("videoplay")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split("---")));
                playlistIds.clear();

                for(int i = 1; i<msgSplit.size(); i++){
                    playlistIds.add(msgSplit.get(i));
                }

                getFragmentManager()
                        .beginTransaction()
                        .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                        .show(playerFragment)
                        .addToBackStack(null)
                        .commit();

                videoList.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
                videoChatDivider.setVisibility(View.VISIBLE);
                MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                youTubePlayer.loadVideos(playlistIds);
            }
            if(msg.startsWith("/video$") && message.getSenderId().equals(id)){
                currentVideo = msg.substring(7);

                getFragmentManager()
                        .beginTransaction()
                        .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                        .show(playerFragment)
                        .addToBackStack(null)
                        .commit();

                videoList.setVisibility(View.GONE);
                toolbar.setVisibility(View.GONE);
                videoChatDivider.setVisibility(View.VISIBLE);
                MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

                youTubePlayer.loadVideo(currentVideo);
            }else if(msg.equals("/pause$") && message.getSenderId().equals(id) && youTubePlayer != null){
                youTubePlayer.pause();
            }else if(msg.equals("/unpause$") && message.getSenderId().equals(id) && youTubePlayer != null){
                youTubePlayer.play();
            }else if(msg.startsWith("/seek$") && message.getSenderId().equals(id) && youTubePlayer != null){
                youTubePlayer.seekToMillis(Integer.parseInt(msg.substring(6)));
            }else if(msg.startsWith("sessionend-")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(name + " has left the session");

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                        user.setSessionStatus(false);
                        user.saveInBackground();

                        MainActivity.super.onBackPressed();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }else if(msg.startsWith("addtoplaylist")) {
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(message.getTextBody().split("------")));
                String title = msgSplit.get(1);
                String thumbnail = msgSplit.get(2);
                String id = msgSplit.get(3);

                WeTubeApplication.getSharedDataSource().getPlaylist().add(new PlaylistItem(title, thumbnail, id));
                playlistItemAdapter.notifyDataSetChanged();
            }else if(msg.startsWith("linkedvideo------")) {
                WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(message.getTextBody(), MessageItem.INCOMING_MSG));
                messageItemAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
            }else if(msg.startsWith("playlistnext")) {
                youTubePlayer.loadVideos(playlistIds, ++playlistIndex, 0);
            }else if(msg.startsWith("playlistprev")){
                youTubePlayer.loadVideos(playlistIds, --playlistIndex, 0);
            }else if(message.getSenderId().equals(id)) {
                if(isFullscreen || !isPortrait){
                    Toast.makeText(MainActivity.this, message.getTextBody(), Toast.LENGTH_SHORT).show();
                }

                WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(message.getTextBody(), MessageItem.INCOMING_MSG));
                messageItemAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
            }
        }






    */




