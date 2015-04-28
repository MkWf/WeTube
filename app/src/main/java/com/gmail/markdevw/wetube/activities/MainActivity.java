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
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.gmail.markdevw.wetube.adapters.VideoItemAdapter;
import com.gmail.markdevw.wetube.api.model.MessageItem;
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

import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by Mark on 3/24/2015.
 */

public class MainActivity extends ActionBarActivity implements VideoListFragment.Delegate, YouTubePlayer.OnInitializedListener,
        YouTubePlayer.OnFullscreenListener, View.OnClickListener,
        YouTubePlayer.PlaybackEventListener{

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
    private MessageItemAdapter messageItemAdapter;
    private EditText messageField;
    private Button sendMessage;
    private FrameLayout videoList;
    private View videoChatDivider;
    private String name = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName();
    private String id = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId();

    private final int MESSAGE = 0;
    private final int VIDEO_START = 1;
    private final int VIDEO_PAUSE = 2;
    private final int VIDEO_UNPAUSE = 3;
    private final int VIDEO_SEEK = 4;
    private final int VIDEO_BUFFER = 5;

    private int messageType;
    private boolean isPaused = false;

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

        recyclerView = (RecyclerView) findViewById(R.id.rv_activity_main);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(messageItemAdapter);

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
        currentVideo = videoItem.getId();

        messageType = VIDEO_START;

        getFragmentManager()
                .beginTransaction()
                .hide(getFragmentManager().findFragmentById(R.id.fl_activity_video_list))
                .show(playerFragment)
                .addToBackStack(null)
                .commit();

        videoList.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        videoChatDivider.setVisibility(View.VISIBLE);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        messageService.sendMessage(id, "/video$" + videoItem.getId());

    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() > 0){
            getFragmentManager().popBackStack();
            videoList.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            videoChatDivider.setVisibility(View.GONE);
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Are you sure you want to leave this session?");

            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    messageService.sendMessage(id,
                            "sessionend-" + ParseUser.getCurrentUser().getUsername() + "-" + ParseUser.getCurrentUser().getObjectId());
                    dialog.dismiss();
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
        this.youTubePlayer.setPlaybackEventListener(this);
        if (!b && currentVideo != null) {
            this.youTubePlayer.cueVideo(currentVideo);
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
        } else if (isPortrait) {
            setLayoutSize(playerFragment.getView(), MATCH_PARENT, WRAP_CONTENT);
            chatbar.setVisibility(View.VISIBLE);
        } else {
            int screenWidth = dpToPx(getResources().getConfiguration().screenWidthDp);
            setLayoutSize(playerFragment.getView(), MATCH_PARENT, MATCH_PARENT);
            chatbar.setVisibility(View.GONE);
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
            messageService.sendMessage(id, messageField.getText().toString());
            messageType = MESSAGE;
            WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(messageField.getText().toString(), MessageItem.OUTGOING_MSG));
            messageItemAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
            messageField.setText("");
        }
    }

    @Override
    public void onPlaying() {
        if(isPaused){
            isPaused = false;
            messageType = VIDEO_UNPAUSE;
            messageService.sendMessage(id, "/unpause$");
        }
    }

    @Override
    public void onPaused() {
        messageType = VIDEO_PAUSE;
        isPaused = true;
        messageService.sendMessage(id, "/pause$");
    }

    @Override
    public void onStopped() {

    }

    @Override
    public void onBuffering(boolean b) {
       if(b){
            messageType = VIDEO_BUFFER;
            messageService.sendMessage(id, "/pause$");
        }else{
            messageType = VIDEO_BUFFER;
            messageService.sendMessage(id, "/unpause$");
        }
    }

    @Override
    public void onSeekTo(int i) {
        messageType = VIDEO_SEEK;
        messageService.sendMessage(id, "/seek$" + i);
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
        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            Toast.makeText(MainActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
        }
        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            String msg = message.getTextBody();
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
            }else if(msg.startsWith("sessionend-")){
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
            }else if(message.getSenderId().equals(id)) {
                if(isFullscreen || !isPortrait){
                    Toast.makeText(MainActivity.this, message.getTextBody(), Toast.LENGTH_SHORT).show();
                }

                WeTubeApplication.getSharedDataSource().getMessages().add(new MessageItem(message.getTextBody(), MessageItem.INCOMING_MSG));
                messageItemAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(WeTubeApplication.getSharedDataSource().getMessages().size() - 1);
            }
        }
        @Override
        public void onMessageSent(MessageClient client, Message message, String recipientId) {

        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
            if(messageType == VIDEO_START){
                youTubePlayer.loadVideo(currentVideo);
            }
            if(messageType == VIDEO_PAUSE){
                youTubePlayer.pause();
            }
        }

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }



}
