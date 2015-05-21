package com.gmail.markdevw.wetube.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Mark on 5/20/2015.
 */
public class NetWatcher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            if (info.isConnected()) {

            }
            else {
                /*Toast.makeText(WeTubeApplication.getSharedInstance(), "Lost connection to service", Toast.LENGTH_LONG).show();
                SharedPreferences sharedpreferences = context.getSharedPreferences
                        ("MyPrefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.clear();
                editor.commit();

                Intent mStartActivity = new Intent(WeTubeApplication.getSharedInstance(), UsersActivity.class);
                int mPendingIntentId = 123456;
                PendingIntent mPendingIntent = PendingIntent.getActivity(WeTubeApplication.getSharedInstance(), mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) WeTubeApplication.getSharedInstance().getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                System.exit(0);*/
            }
        }
    }
}
