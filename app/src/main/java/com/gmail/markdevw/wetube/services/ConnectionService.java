package com.gmail.markdevw.wetube.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.activities.UsersActivity;
import com.gmail.markdevw.wetube.api.DataSource;
import com.parse.FindCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.models.WeTubeUser;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by Mark on 5/19/2015.
 */
public class ConnectionService extends Service {

    private static final int MAX_MISSES = 2;
    private static final long PERIOD = 5;

    private Timer mTimer;
    private Date mDate;
    private int mTimesYouMissed;
    private long mTime;
    private int mTimesTheyMissed;
    private Subscription mSubscription;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSubscription = Observable.interval(0, PERIOD, TimeUnit.SECONDS, Schedulers.io())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onCompleted() {
                        //Do Nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(getClass().getSimpleName(), e.getLocalizedMessage());
                    }

                    @Override
                    public void onNext(Long aLong) {
                        try {
                            updateUserLastSeen();
                            checkSessionPartnerStatus();
                        }catch(IOException e) {
                            mTimesYouMissed++;
                            if (mTimesYouMissed >= MAX_MISSES) {
                                clearSavedLoginData();
                                returnUserToLoginScreen();
                            }
                        }
                    }
                });

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * While in a session with another user, user1 will ping user2's lastSeen status and vice versa.
     *
     * If user1 notices that user2's lastSeen hasn't been updated for more than 10 seconds, we
     * assume the that user2 is no longer connected and automatically exit the session
     */
    public void checkSessionPartnerStatus() {
        if(WeTubeApplication.getSharedDataSource().getCurrentRecipient() != null){
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereEqualTo("objectId", WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId());
            query.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> users, ParseException e) {
                    if(e == null){
                        WeTubeUser user = (WeTubeUser) users.get(0);
                        long timeCheck = user.getLong("lastSeen");
                        if(mTime == timeCheck){
                            mTimesTheyMissed++;
                            if(mTimesTheyMissed == MAX_MISSES){
                                returnUserToUsersActivity();
                            }
                        }else{
                            mTime = timeCheck;
                            mTimesTheyMissed = 0;
                        }
                    }
                }
            });
        }
    }

    /**
     * Sends the user who didn't lose connection back to UsersActivity.
     *
     * When two users are in a session and user1 loses connection, user2 will be sent back to
     * UsersActivity.
     */
    public void returnUserToUsersActivity() {
        DataSource ds = WeTubeApplication.getSharedDataSource();
        ds.getMainActivity().finish();
        ((UsersActivity) ds.getUsersActivity()).setSessionConnEnd(true);
    }

    /**
     * If the user loses connection for more than MAX_MISSES, the user is returned back to the
     * login screen.
     */
    public void returnUserToLoginScreen() {
        if(WeTubeApplication.getSharedDataSource().getMainActivity() != null){
            WeTubeApplication.getSharedDataSource().getMainActivity().finish();
        }

        Intent i = new Intent(WeTubeApplication.getSharedInstance(), UsersActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra("connloss", 1);
        WeTubeApplication.getSharedDataSource().getUsersActivity().moveTaskToBack(true);
        WeTubeApplication.getSharedDataSource().getUsersActivity().finish();
        startActivity(i);
    }

    /**
     * Clears the user's login data prior to returning to the Login screen.
     */
    public void clearSavedLoginData() {
        SharedPreferences sharedpreferences = getSharedPreferences
                ("MyPrefs", Context.MODE_PRIVATE);
        sharedpreferences.edit()
                .clear()
                .commit();
    }

    /**
     * Updates the user's lastSeen column in Parse.
     *
     * Every 5 seconds this method is called to determine is the user still has a connection
     * If the user is no longer connected an IOException gets thrown, otherwise the user's
     * lastSeen includes the latest time.
     *
     * @throws IOException Alerts us when the user has lost connection
     */
    public void updateUserLastSeen() throws IOException {
        URL url = new URL("http://m.google.com");
        url.openStream();

        final String userId = ParseUser.getCurrentUser().getObjectId();
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("ms", new Date().getTime());
        ParseCloud.callFunctionInBackground("updateLastSeen", params);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mSubscription.unsubscribe();
    }
}
