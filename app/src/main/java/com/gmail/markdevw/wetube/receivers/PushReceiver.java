package com.gmail.markdevw.wetube.receivers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.gmail.markdevw.wetube.WeTubeApplication;
import com.gmail.markdevw.wetube.activities.MainActivity;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mark on 4/4/2015.
 */
public class PushReceiver extends ParsePushBroadcastReceiver {
    @Override
    public void onPushReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();
        String jsonData = extras.getString( "com.parse.Data" );
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            String userId = jsonObject.getString("alert");
           // WeTubeApplication.getSharedDataSource().setCurrentRecipient(userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(WeTubeApplication.getSharedInstance(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
