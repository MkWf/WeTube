package com.gmail.markdevw.wetube;

import android.app.Application;

import com.gmail.markdevw.wetube.api.DataSource;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.models.WeTubeUser;

/**
 * Created by Mark on 3/26/2015.
 */
public class WeTubeApplication extends Application {

    public static WeTubeApplication getSharedInstance() {
        return sharedInstance;
    }

    public static DataSource getSharedDataSource() {
        return WeTubeApplication.getSharedInstance().getDataSource();
    }

    private static WeTubeApplication sharedInstance;
    private DataSource dataSource;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedInstance = this;
        dataSource = new DataSource(this);

        ParseObject.registerSubclass(WeTubeUser.class);
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, getResources().getString(R.string.parse_app_id), getResources().getString(R.string.parse_client_key));
        ParseInstallation.getCurrentInstallation().saveInBackground();

        /*ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });*/
    }

    public DataSource getDataSource() {
        return dataSource;
    }


}