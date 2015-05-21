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
    private final int MAX_COUNT = 1;
    private int count = 0;


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
