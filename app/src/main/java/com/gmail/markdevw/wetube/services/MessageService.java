package com.gmail.markdevw.wetube.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessageService extends Service implements SinchClientListener {

    private static final String APP_KEY = "1dd42fb4-7483-4acf-baaf-8a17985ee947";
    private static final String APP_SECRET = "axavTzdxn0iRPleS/8fTHA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";

    private final MessageServiceInterface mServiceInterface = new MessageServiceInterface();
    private SinchClient mSinchClient = null;
    private MessageClient mMessageClient = null;
    private LocalBroadcastManager mBroadcaster;
    private Intent mBroadcastIntent = new Intent("com.gmail.markdevw.wetube.activities.UsersActivity");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String currentUserId = ParseUser.getCurrentUser().getObjectId();

        if (currentUserId != null && !isSinchClientStarted()) {
            startSinchClient(currentUserId);
        }

        mBroadcaster = LocalBroadcastManager.getInstance(this);

        return super.onStartCommand(intent, flags, startId);
    }

    public void startSinchClient(String username) {
        mSinchClient = Sinch.getSinchClientBuilder().context(this).userId(username).applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET).environmentHost(ENVIRONMENT).build();

        mSinchClient.addSinchClientListener(this);

        mSinchClient.setSupportMessaging(true);
        mSinchClient.setSupportActiveConnectionInBackground(true);

        mSinchClient.checkManifest();
        mSinchClient.start();
    }

    private boolean isSinchClientStarted() {
        return mSinchClient != null && mSinchClient.isStarted();
    }

    @Override
    public void onClientFailed(SinchClient client, SinchError error) {
        mBroadcastIntent.putExtra("success", false);
        mBroadcaster.sendBroadcast(mBroadcastIntent);

        mSinchClient = null;
    }

    @Override
    public void onClientStarted(SinchClient client) {
        mBroadcastIntent.putExtra("success", true);
        mBroadcaster.sendBroadcast(mBroadcastIntent);

        client.startListeningOnActiveConnection();
        mMessageClient = client.getMessageClient();
    }

    @Override
    public void onClientStopped(SinchClient client) {
        mSinchClient = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mServiceInterface;
    }

    @Override
    public void onLogMessage(int level, String area, String message) {
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration clientRegistration) {
    }

    public void sendMessage(String recipientUserId, String textBody) {
        if (mMessageClient != null) {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            mMessageClient.send(message);
        }
    }

    public void addMessageClientListener(MessageClientListener listener) {
        if (mMessageClient != null) {
            mMessageClient.addMessageClientListener(listener);
        }
    }

    public void removeMessageClientListener(MessageClientListener listener) {
        if (mMessageClient != null) {
            mMessageClient.removeMessageClientListener(listener);
        }
    }

    @Override
    public void onDestroy() {
        mSinchClient.stopListeningOnActiveConnection();
        mSinchClient.terminate();
    }

    public class MessageServiceInterface extends Binder {
        public void sendMessage(String recipientUserId, String textBody) {
            MessageService.this.sendMessage(recipientUserId, textBody);
        }

        public void addMessageClientListener(MessageClientListener listener) {
            MessageService.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener) {
            MessageService.this.removeMessageClientListener(listener);
        }

        public boolean isSinchClientStarted() {
            return MessageService.this.isSinchClientStarted();
        }
    }
}
