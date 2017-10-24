package com.example.batrakov.alarmmanagertask;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by batrakov on 24.10.17.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "AlarmReceiver";

    public static final String NOTIFICATION = "notification";
    public static final int NOTIFICATION_ID = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: ");
        System.out.println("kek");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("Scheduled Notification")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText("test")
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
    }
}
