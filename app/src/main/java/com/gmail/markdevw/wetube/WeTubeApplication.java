package com.gmail.markdevw.wetube;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by Mark on 3/26/2015.
 */
public class WeTubeApplication extends Application {

    public static WeTubeApplication getSharedInstance() {
        return sharedInstance;
    }

//    public static DataSource getSharedDataSource() {
//        return WeTubeApplication.getSharedInstance().getDataSource();
//    }

    private static WeTubeApplication sharedInstance;
    //private DataSource dataSource;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedInstance = this;
        //dataSource = new DataSource(this);
        Firebase.setAndroidContext(this);
    }

//    public DataSource getDataSource() {
//        return dataSource;
//    }
}