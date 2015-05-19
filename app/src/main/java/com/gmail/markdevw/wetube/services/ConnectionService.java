package com.gmail.markdevw.wetube.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseUser;

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

    @Override
    public IBinder onBind(Intent intent) {
        timer = new Timer();

        task = new TimerTask() {
            @Override
            public void run() {
                date = new Date();
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("userId", userId);
                params.put("ms", date.getTime());
                ParseCloud.callFunctionInBackground("updateLastSeen", params, new FunctionCallback<String>() {
                    @Override
                    public void done(String userList, com.parse.ParseException e) {
                        if(e == null){

                        }
                    }
                });
            }
        };

        timer.schedule(task, 0, 10000);
        return null;
    }
}
