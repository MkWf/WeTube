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
import android.widget.TextView;
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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * Created by Mark on 3/24/2015.
 */

public class MainActivity extends ActionBarActivity implements VideoListFragment.Delegate, YouTubePlayer.OnInitializedListener,
        YouTubePlayer.OnFullscreenListener, View.OnClickListener,
        YouTubePlayer.PlaybackEventListener, MessageItemAdapter.Delegate, YouTubePlayer.PlaylistEventListener, PlaylistItemAdapter.Delegate, DrawerLayout.DrawerListener, YouTubePlayer.PlayerStateChangeListener, DialogInterface.OnDismissListener {

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
    private String name;
    private String id;
    private List<String> playlistIds = new ArrayList<>();
    private int playlistIndex = 0;
    private String msgSplitter = "=-=-=";
    private HashMap<String, String> messages = new HashMap<String, String>();
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private PlaylistItem lastClickedPlaylistItem = new PlaylistItem();
    private TextView playlistSize;
    private boolean isBlocking = false;
    private boolean isFirstMessage = true;
    private AlertDialog dialog;
    private Queue<Message> messageQueue = new LinkedBlockingQueue<>();

    private boolean isPaused = false;
    private boolean hasYourVideoEnded = false;
    private boolean hasTheirVideoEnded = false;
    private boolean hasVideoStarted = false;
    private int currentIndex = 0;
    private final int MAX_PLAYLIST_SIZE = 50;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WeTubeApplication.getSharedDataSource().setMainActivity(this);
        WeTubeApplication.getSharedDataSource().setVideoActivity(true);

        name = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName();
        id = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId();

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

        playlistSize = (TextView) findViewById(R.id.playlist_size);


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
                    messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "Started search for " + search.toUpperCase() + "...");
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
                        toolbar.setTitle("Page: " + WeTubeApplication.getSharedDataSource().getCurrentPage() + "   User: " + name);
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

        if(!isMatchFound && WeTubeApplication.getSharedDataSource().getPlaylist().size() < MAX_PLAYLIST_SIZE){
            if(WeTubeApplication.getSharedDataSource().isSessionController()){
                messageService.sendMessage(id, msgSplitter + "addtoplaylist" + msgSplitter + videoItem.getTitle() + msgSplitter + videoItem.getThumbnailURL() + msgSplitter
                                               + videoItem.getId());
            }else{
                String message = msgSplitter + "linkedvideo" + msgSplitter + videoItem.getTitle() + msgSplitter + videoItem.getThumbnailURL() + msgSplitter
                                    + videoItem.getId();
                messageService.sendMessage(id, message);
            }
        }else{
            Toast.makeText(this, "Max playlist size reached (50)", Toast.LENGTH_SHORT).show();
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
                messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), msgSplitter + "addtoplaylist" + msgSplitter + title + msgSplitter + thumbnail + msgSplitter + id);
            }else{
                Toast.makeText(this, "Only the controller can add videos to the playlist from chat", Toast.LENGTH_SHORT).show();
            }
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
                    messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            msgSplitter + "playlistexit" + msgSplitter + ParseUser.getCurrentUser().getUsername() + msgSplitter + ParseUser.getCurrentUser().getObjectId());
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
                    messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(),
                            msgSplitter + "sessionend" + msgSplitter + ParseUser.getCurrentUser().getUsername() + msgSplitter + ParseUser.getCurrentUser().getObjectId());
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
        this.youTubePlayer = youTubePlayer;
        //this.youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION);
        this.youTubePlayer.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        this.youTubePlayer.setOnFullscreenListener(this);
        this.youTubePlayer.setPlaybackEventListener(this);
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
        layout();
    }

    private void layout() {
        isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if (isPortrait) {
            isFullscreen = false;
            setLayoutSize(playerFragment.getView(), MATCH_PARENT, WRAP_CONTENT);
            chatbar.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        } else {
            isFullscreen = true;
            setLayoutSize(playerFragment.getView(), MATCH_PARENT, MATCH_PARENT);
            chatbar.setVisibility(View.GONE);
            toolbar.setVisibility(View.GONE);
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
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);

        WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
        user.setSessionStatus(false);
        user.saveInBackground();

        try{
            youTubePlayer.release();
        }catch(NullPointerException e){

        }

        WeTubeApplication.getSharedDataSource().setCurrentRecipient(null);
        WeTubeApplication.getSharedDataSource().setMainActivity(null);
        WeTubeApplication.getSharedDataSource().getPlaylist().clear();
        WeTubeApplication.getSharedDataSource().getMessages().clear();
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
                    messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), msgSplitter + "playliststart");
                }
                break;
            case R.id.action_pass_control:
                messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), msgSplitter + "passcontroller");
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
    public void onPlayListItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem, int index, View itemView) {
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), msgSplitter + "playlistindex"
                    + msgSplitter + String.valueOf(index));
        }
    }

    @Override
    public void onDeleteItemClicked(PlaylistItemAdapter itemAdapter, PlaylistItem playlistItem) {
        int index = WeTubeApplication.getSharedDataSource().getPlaylist().indexOf(playlistItem);
        playlistItem.setToBeDeleted(true);
        messageService.sendMessage(id, msgSplitter + "deleteitemplaylist" + msgSplitter + String.valueOf(index));
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
        hasVideoStarted = true;
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
        }
    }

    @Override
    public void onVideoEnded() {
       // messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "videoend");
       // messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "videoended");
    }

    @Override
    public void onError(YouTubePlayer.ErrorReason errorReason) {
        WeTubeApplication.getSharedDataSource();
    }

    @Override
    public void onPlaying() {
      /*  if(WeTubeApplication.getSharedDataSource().isSessionController() && isPaused && !hasVideoStarted){
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "play" + msgSplitter);
            isPaused = false;
        }else if(hasVideoStarted){
            hasVideoStarted = false;
            youTubePlayer.pause();
        }*/
        if(hasVideoStarted){
            youTubePlayer.pause();
            hasVideoStarted = false;

            if(!WeTubeApplication.getSharedDataSource().isSessionController()){
                messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), msgSplitter + "videostart");
            }
        }else if(WeTubeApplication.getSharedDataSource().isSessionController()) {
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), msgSplitter + "play" + msgSplitter);
            //isPaused = false;
        }
    }

    @Override
    public void onPaused() {
       if(WeTubeApplication.getSharedDataSource().isSessionController()){
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), msgSplitter + "pause" + msgSplitter);
        }
    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onBuffering(boolean b) {
      //  if(b){
     //       messageType = VIDEO_BUFFER;
      //      messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "/pause$");
     ///   }else{
      //      messageType = VIDEO_BUFFER;
      //      messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "/unpause$");
     //   }
    }

    @Override
    public void onSeekTo(int i) {
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), msgSplitter + "seek" + msgSplitter + String.valueOf(i));
        }
    }

    @Override
    public void onPrevious() {
        if(WeTubeApplication.getSharedDataSource().isSessionController()){
            messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId()
                    ,msgSplitter + "playlistprev");
        }
    }

    @Override
    public void onNext() {
       if(WeTubeApplication.getSharedDataSource().isSessionController() && !hasYourVideoEnded){
           messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId()
                                ,msgSplitter + "playlistnext");
       }
    }

    @Override
    public void onPlaylistEnded() {

    }

    public void clearDialogsById(String id){

        Message msgToRemove;

        for(Message message : messageQueue) {
            ArrayList<String> msg = new ArrayList<String>(Arrays.asList(message.getTextBody().split(msgSplitter)));
            if(msg.get(3).equals(id)){
                messageQueue.remove(message);
            }
        }
        isBlocking = false;
        if(!messageQueue.isEmpty()){
            showNextMessage();
        }
    }

    public void showNextMessage() {
        if (messageQueue != null && messageQueue.size() > 0) {
            Message message = messageQueue.poll();
            ArrayList<String> msg = new ArrayList<String>(Arrays.asList(message.getTextBody().split(msgSplitter)));

            if (msg.get(1).equals("friendadd")) {
                final String name = msg.get(2);
                final String id = msg.get(3);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Friend request from " + name);

                if(WeTubeApplication.getSharedDataSource().getFriendsSize() == WeTubeApplication.getSharedDataSource().getMaxFriends()){
                    builder.setNegativeButton("Friends list is full", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            messageService.sendMessage(id, msgSplitter + "friendfull" + msgSplitter + ParseUser.getCurrentUser().getUsername());
                            dialog.cancel();
                        }
                    });
                    builder.setNeutralButton("Block User", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isBlocking = true;
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Are you sure you want to block " + name + " ?");

                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Blocked block = new Blocked(ParseUser.getCurrentUser().getObjectId(), id);
                                    block.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            messageService.sendMessage(id, msgSplitter + "blockuser" + msgSplitter + ParseUser.getCurrentUser().getUsername() + msgSplitter
                                                    + ParseUser.getCurrentUser().getObjectId());
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
                            dialog = builder.create();
                            dialog.show();
                        }
                    });
                    builder.setCancelable(false);
                    dialog = builder.create();
                    dialog.show();
                }else{
                    builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                            messageService.sendMessage(id, msgSplitter + "friendaccept" + msgSplitter + user.getUsername() + msgSplitter + user.getObjectId());
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            messageService.sendMessage(id, msgSplitter + "frienddecline" + msgSplitter + ParseUser.getCurrentUser().getUsername());
                            dialog.cancel();
                        }
                    });
                    builder.setNeutralButton("Block User", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            isBlocking = true;
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Are you sure you want to block " + name + " ?");

                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Blocked block = new Blocked(ParseUser.getCurrentUser().getObjectId(), id);
                                    block.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            messageService.sendMessage(id, msgSplitter + "blockuser" + msgSplitter + ParseUser.getCurrentUser().getUsername() + msgSplitter
                                                    + ParseUser.getCurrentUser().getObjectId());
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
                            dialog = builder.create();
                            dialog.show();
                        }
                    });
                }
                builder.setCancelable(false);
                dialog = builder.create();
                dialog.show();
            }
            if (dialog != null) {
                dialog.setOnDismissListener(this);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {

        if(!messageQueue.isEmpty() && !isBlocking){
            showNextMessage();
        }
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
            if(msg.startsWith(msgSplitter + "addtoplaylist")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to add " + title + " to playlist", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(msgSplitter + "linkedvideo")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                String title = msgSplit.get(1);
                Toast.makeText(MainActivity.this, "Failed to add " + title + " to chat", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(msgSplitter + "deleteitemplaylist")){
                ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                String title = msgSplit.get(1);
                for(int i = 0; i<WeTubeApplication.getSharedDataSource().getPlaylist().size(); i++){
                    if(title.equals(WeTubeApplication.getSharedDataSource().getPlaylist().get(i).getTitle())){
                        WeTubeApplication.getSharedDataSource().getPlaylist().get(i).setToBeDeleted(false);
                        break;
                    }
                }
                Toast.makeText(MainActivity.this, "Failed to delete " + title + " from playlist", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(msgSplitter + "passcontroller")){
                Toast.makeText(MainActivity.this, "Failed to pass controls to "
                        + WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName(), Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(msgSplitter + "playlistindex")){
                Toast.makeText(MainActivity.this, "Failed to start video", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(msgSplitter + "playliststart")){
                Toast.makeText(MainActivity.this, "Failed to start playlist", Toast.LENGTH_LONG).show();
            }else if(msg.startsWith(msgSplitter + "playlistnext")) {
                Toast.makeText(MainActivity.this, "Failed to play next video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(msgSplitter + "playlistprev")){
                Toast.makeText(MainActivity.this, "Failed to play previous video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(msgSplitter + "pause")) {
                youTubePlayer.play();
                Toast.makeText(MainActivity.this, "Failed to pause video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(msgSplitter + "play")){
                youTubePlayer.pause();
                Toast.makeText(MainActivity.this, "Failed to play video", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(msgSplitter + "seek")) {
                Toast.makeText(MainActivity.this, "Failed to seek to new position", Toast.LENGTH_SHORT).show();
            }else if(msg.startsWith(msgSplitter + "videostart")){
                messageService.sendMessage(WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId(), "videostart");
                Toast.makeText(MainActivity.this, "Failed to initiate video play", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
            }
            messages.remove(failureInfo.getMessageId());
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            String msg = message.getTextBody();
            if(msg.startsWith(msgSplitter + "friendadd")){
                messageQueue.add(message);
                if(!isFirstMessage){
                    if(dialog != null && !dialog.isShowing() && !messageQueue.isEmpty()){
                        showNextMessage();
                    }
                }else{
                    isFirstMessage = false;
                    showNextMessage();
                }
            }else{
                if (msg.startsWith(msgSplitter + "addtoplaylist")) {
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(message.getTextBody().split(msgSplitter)));
                    String title = msgSplit.get(2);
                    String thumbnail = msgSplit.get(3);
                    String id = msgSplit.get(4);

                    WeTubeApplication.getSharedDataSource().getPlaylist().add(new PlaylistItem(title, thumbnail, id, WeTubeApplication.getSharedDataSource().getPlaylist().size() + 1));
                    playlistSize.setText(currentIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    playlistItemAdapter.notifyDataSetChanged();
                } else if (msg.startsWith(msgSplitter + "linkedvideo")) {
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(message.getTextBody(), MessageItem.INCOMING_MSG));
                    messageItemAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
                } else if (msg.startsWith(msgSplitter + "deleteitemplaylist")) {
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                    String index = msgSplit.get(2);
                    int i = Integer.parseInt(index);
                    if(WeTubeApplication.getSharedDataSource().getPlaylist().size() > 0){
                        WeTubeApplication.getSharedDataSource().getPlaylist().remove(i);
                        List<PlaylistItem> list = WeTubeApplication.getSharedDataSource().getPlaylist();
                        for(int j = 0; j<list.size(); j++){
                            list.get(j).setIndex(j+1);
                        }
                    }
                    if(playlistIds.size() > 0){
                        playlistIds.remove(Integer.parseInt(index));
                    }
                    playlistItemAdapter.notifyDataSetChanged();

                    int item = currentIndex + 1;

                    if(i < currentIndex && currentIndex != 0){
                        --currentIndex;
                        playlistSize.setText(item + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else if(i > currentIndex && currentIndex != 0){
                        playlistSize.setText(item + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else{
                        currentIndex = 0;
                        playlistSize.setText(currentIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }
                } else if(msg.startsWith(msgSplitter + "passcontroller")){
                    WeTubeApplication.getSharedDataSource().setSessionController(true);
                    invalidateOptionsMenu();
                    playlistItemAdapter.notifyDataSetChanged();
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    Toast.makeText(MainActivity.this, name + " has given you control", Toast.LENGTH_SHORT).show();
                } else if(msg.startsWith(msgSplitter + "playliststart")){
                    WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(false);
                    currentIndex = 0;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(true);
                    playlistItemAdapter.notifyDataSetChanged();

                    currentIndex = 0;
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

                    String index = String.valueOf(currentIndex + 1);
                    playlistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());

                    youTubePlayer.loadVideos(playlistIds);
                }else if(msg.startsWith(msgSplitter + "playlistnext")){
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(false);
                    currentIndex++;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(true);
                    playlistItemAdapter.notifyDataSetChanged();

                    String index = String.valueOf(currentIndex + 1);
                    playlistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    youTubePlayer.next();
                }else if(msg.startsWith(msgSplitter + "playlistprev")) {
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(false);
                    currentIndex--;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(true);
                    playlistItemAdapter.notifyDataSetChanged();

                    String index = String.valueOf(currentIndex + 1);
                    playlistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());

                    youTubePlayer.previous();
                }else if(msg.startsWith(msgSplitter + "playlistindex")) {
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                    String index = msgSplit.get(2);
                    int video = Integer.parseInt(index);

                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(false);
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(video).setSelected(true);
                    playlistItemAdapter.notifyDataSetChanged();

                    currentIndex = video;

                    String indexItem = String.valueOf(currentIndex + 1);
                    playlistSize.setText(indexItem + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());

                    if (playerFragment.isVisible()) {
                        youTubePlayer.loadVideos(playlistIds, video, 0);
                    } else {
                        WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
                        playlistIds.clear();
                        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                        for (int i = 0; i < videos.size(); i++) {
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

                        youTubePlayer.loadVideos(playlistIds, video, 0);
                    }
                }else if(msg.startsWith(msgSplitter + "playlistexit")){
                    getFragmentManager().popBackStack();

                    videoList.setVisibility(View.VISIBLE);
                    toolbar.setVisibility(View.VISIBLE);
                    videoChatDivider.setVisibility(View.GONE);
                    MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    currentIndex = 0;

                    WeTubeApplication.getSharedDataSource().setPlayerVisible(false);
                    playlistItemAdapter.notifyDataSetChanged();

                    playlistSize.setText(currentIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(msgSplitter + "pause")) {
                    try{
                        youTubePlayer.pause();
                    }catch(NullPointerException e){

                    }

                }else if(msg.startsWith(msgSplitter + "play")) {
                    youTubePlayer.play();
                }else if(msg.startsWith(msgSplitter + "seek")) {
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                    int seek = Integer.parseInt(msgSplit.get(2));
                    youTubePlayer.seekToMillis(seek);
                }else if(msg.startsWith(msgSplitter + "videostart")){
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                }else if(msg.startsWith((msgSplitter + "videoended"))) {
                    hasTheirVideoEnded = true;
                    if (WeTubeApplication.getSharedDataSource().isSessionController() && hasYourVideoEnded) {
                        youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                    }
                }else if(msg.startsWith(msgSplitter + "sessionend")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(name + " has left the session");

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                            user.setSessionStatus(false);
                            user.saveInBackground();

                            youTubePlayer.release();
                            MainActivity.super.onBackPressed();
                        }
                    });
                    builder.setCancelable(false);
                    builder.show();
                }else{
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(msg, MessageItem.INCOMING_MSG));
                    messageItemAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);

                    if(isFullscreen){
                        Toast.makeText(WeTubeApplication.getSharedInstance(), msg, Toast.LENGTH_SHORT).show();
                    }
                }
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
                if(msg.startsWith(msgSplitter + "addtoplaylist")){
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                    String title = msgSplit.get(2);
                    String thumbnail = msgSplit.get(3);
                    String id = msgSplit.get(4);

                    WeTubeApplication.getSharedDataSource().getPlaylist().add(new PlaylistItem(title, thumbnail, id, WeTubeApplication.getSharedDataSource().getPlaylist().size() + 1));
                    playlistSize.setText(currentIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    playlistItemAdapter.notifyDataSetChanged();
                }else if(msg.startsWith(msgSplitter + "linkedvideo")){
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(msg, MessageItem.OUTGOING_MSG));
                    messageItemAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
                }else if(msg.startsWith(msgSplitter + "deleteitemplaylist")){
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                    String index = msgSplit.get(2);
                    int i = Integer.parseInt(index);
                    if(WeTubeApplication.getSharedDataSource().getPlaylist().size() > 0){
                        WeTubeApplication.getSharedDataSource().getPlaylist().remove(i);
                        List<PlaylistItem> list = WeTubeApplication.getSharedDataSource().getPlaylist();
                        for(int j = 0; j<list.size(); j++){
                            list.get(j).setIndex(j+1);
                        }
                    }
                    if(playlistIds.size() > 0){
                        playlistIds.remove(Integer.parseInt(index));
                    }
                    playlistItemAdapter.notifyDataSetChanged();

                    int item = currentIndex + 1;
                    if(i < currentIndex && currentIndex != 0){
                        --currentIndex;
                        playlistSize.setText(item + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else if(i > currentIndex && currentIndex != 0){
                        playlistSize.setText(item + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }else{
                        currentIndex = 0;
                        playlistSize.setText(currentIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    }
                }else if(msg.startsWith(msgSplitter + "passcontroller")){
                    WeTubeApplication.getSharedDataSource().setSessionController(false);
                    invalidateOptionsMenu();
                    playlistItemAdapter.notifyDataSetChanged();
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    Toast.makeText(MainActivity.this, "You have given control to " + name, Toast.LENGTH_SHORT).show();
                }else if(msg.startsWith(msgSplitter + "playliststart")) {
                    WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(false);
                    currentIndex = 0;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(true);
                    playlistItemAdapter.notifyDataSetChanged();

                    playlistIds.clear();
                    List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                    for (int i = 0; i < videos.size(); i++) {
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

                    String index = String.valueOf(currentIndex + 1);
                    playlistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    youTubePlayer.loadVideos(playlistIds);
                }else if(msg.startsWith(msgSplitter + "playlistindex")) {
                    ArrayList<String> msgSplit = new ArrayList<String>(Arrays.asList(msg.split(msgSplitter)));
                    String index = msgSplit.get(2);
                    int video = Integer.parseInt(index);

                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(false);
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(video).setSelected(true);
                    playlistItemAdapter.notifyDataSetChanged();
                   // playListRecyclerView.smoothScrollBy(0, itemView.getTop());

                    currentIndex = video;
                    String indexItem = String.valueOf(video + 1);

                    playlistSize.setText(indexItem + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                    if(playerFragment.isVisible()){
                        youTubePlayer.loadVideos(playlistIds, video, 0);
                    }else{
                        WeTubeApplication.getSharedDataSource().setPlayerVisible(true);
                        playlistIds.clear();
                        List<PlaylistItem> videos = WeTubeApplication.getSharedDataSource().getPlaylist();
                        for (int i = 0; i < videos.size(); i++) {
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

                        youTubePlayer.loadVideos(playlistIds, video, 0);
                    }
                }else if(msg.startsWith(msgSplitter + "pause")) {
                    isPaused = true;
                }else if(msg.startsWith(msgSplitter + "playlistprev")){
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(false);
                    currentIndex--;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(true);
                    playlistItemAdapter.notifyDataSetChanged();

                    String index = String.valueOf(currentIndex + 1);
                    playlistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(msgSplitter + "playlistnext")) {
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(false);
                    currentIndex++;
                    WeTubeApplication.getSharedDataSource().getPlaylist().get(currentIndex).setSelected(true);
                    playlistItemAdapter.notifyDataSetChanged();

                    String index = String.valueOf(currentIndex + 1);
                    playlistSize.setText(index + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(msgSplitter + "playlistexit")) {
                    getFragmentManager().popBackStack();

                    videoList.setVisibility(View.VISIBLE);
                    toolbar.setVisibility(View.VISIBLE);
                    videoChatDivider.setVisibility(View.GONE);
                    MainActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

                    currentIndex = 0;

                    WeTubeApplication.getSharedDataSource().setPlayerVisible(false);
                    playlistItemAdapter.notifyDataSetChanged();

                    playlistSize.setText(currentIndex + "/" + WeTubeApplication.getSharedDataSource().getPlaylist().size());
                }else if(msg.startsWith(msgSplitter + "sessionend")) {
                    WeTubeUser user = (WeTubeUser) ParseUser.getCurrentUser();
                    user.setSessionStatus(false);
                    user.saveInBackground();

                    WeTubeApplication.getSharedDataSource().getPlaylist().clear();
                    WeTubeApplication.getSharedDataSource().getMessages().clear();
                    youTubePlayer.release();
                    MainActivity.super.onBackPressed();
                }else if(msg.startsWith(msgSplitter + "play") || msg.startsWith(msgSplitter + "friendaccept") || msg.startsWith(msgSplitter + "frienddecline")
                        || msg.startsWith(msgSplitter + "sessionaccept") || msg.startsWith(msgSplitter +  "friendfull") || msg.startsWith(msgSplitter + "blockuser")){
                    //do nothing
                }else{
                    WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(msg, MessageItem.OUTGOING_MSG));
                    messageItemAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
                }
                messages.remove(deliveryInfo.getMessageId());
            }
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }
}
