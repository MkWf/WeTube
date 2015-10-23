package com.gmail.markdevw.wetube.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.activities.UsersActivity;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mark on 5/19/2015.
 */
public class ConnectionService extends Service {
    private Timer timer;
    private TimerTask task;
    private Date date;
    private String userId = ParseUser.getCurrentUser().getObjectId();
    private final int MAX_COUNT = 2;
    private int count = 0;
    private long time = 0;
    private int timesMissed = 0;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();

        task = new TimerTask() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://m.google.com");
                    InputStream stream = url.openStream();
                    int a = stream.available();

                    date = new Date();
                    HashMap<String, Object> params = new HashMap<String, Object>();
                    params.put("userId", userId);
                    params.put("ms", date.getTime());
                    ParseCloud.callFunctionInBackground("updateLastSeen", params, new FunctionCallback<String>() {
                        @Override
                        public void done(String userList, com.parse.ParseException e) {

                        }
                    });

//                    if(WeTubeApplication.getSharedDataSource().getCurrentRecipient() != null){
//                        ParseQuery<ParseUser> query = ParseUser.getQuery();
//                        query.whereEqualTo("objectId", WeTubeApplication.getSharedDataSource().getCurrentRecipient().getId());
//                        query.findInBackground(new FindCallback<ParseUser>() {
//                            @Override
//                            public void done(List<ParseUser> users, ParseException e) {
//                                WeTubeUser user = (WeTubeUser) users.get(0);
//                                long timeCheck = user.getLong("lastSeen");
//                                if(time == timeCheck){
//                                    timesMissed++;
//                                    if(timesMissed == 2){
//                                        String name = WeTubeApplication.getSharedDataSource().getCurrentRecipient().getName();
//                                        WeTubeApplication.getSharedDataSource().getMainActivity().finish();
//                                        UsersActivity ua = (UsersActivity) WeTubeApplication.getSharedDataSource().getUsersActivity();
//                                        ua.sessionEndedDialog(name);
//                                    }
//                                }else{
//                                    time = timeCheck;
//                                    timesMissed = 0;
//                                }
//
//                            }
//                        });
//                    }
                }catch(IOException e) {
                    if (count == 0) {
                        count++;
                        SharedPreferences sharedpreferences = getSharedPreferences
                                ("MyPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.clear();
                        editor.commit();

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
                }
                    /*Intent mStartActivity = new Intent(WeTubeApplication.getSharedInstance(), UsersActivity.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(WeTubeApplication.getSharedInstance(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager) WeTubeApplication.getSharedInstance().getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 6000, mPendingIntent);
                    System.exit(0);*/

                /*date = new Date();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("userId", userId);
                params.put("ms", date.getTime());
                ParseCloud.callFunctionInBackground("updateLastSeen", params, new FunctionCallback<String>() {
                    @Override
                    public void done(String userList, com.parse.ParseException e) {
                        Toast.makeText(WeTubeApplication.getSharedInstance(), "Inside Call", Toast.LENGTH_LONG).show();
                        if (e == null) {
                            Toast.makeText(WeTubeApplication.getSharedInstance(), "Good", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(WeTubeApplication.getSharedInstance(), "Lost connection to service", Toast.LENGTH_LONG).show();
                            SharedPreferences sharedpreferences = getSharedPreferences
                                    ("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.clear();
                            editor.commit();

                            Intent mStartActivity = new Intent(WeTubeApplication.getSharedInstance(), UsersActivity.class);
                            int mPendingIntentId = 123456;
                            PendingIntent mPendingIntent = PendingIntent.getActivity(WeTubeApplication.getSharedInstance(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager mgr = (AlarmManager) WeTubeApplication.getSharedInstance().getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                            System.exit(0);
                        }
                    }
                });*/
                /*Runtime runtime = Runtime.getRuntime();
                try {
                    Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                    int exitValue = ipProcess.waitFor();
                    if(exitValue == 1){

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                    }
                };

        timer.scheduleAtFixedRate(task, 0, 5000);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        timer.purge();
        timer.cancel();
    }
}
