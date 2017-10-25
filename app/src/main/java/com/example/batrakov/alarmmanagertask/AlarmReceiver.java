package com.example.batrakov.alarmmanagertask;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by batrakov on 25.10.17.
 */

public class AlarmReceiver extends WakefulBroadcastReceiver {

    public static final int NOTIFICATION_ID = 0;
    Messenger mMessengerOutgoing;

    @Override
    public void onReceive(Context context, Intent intent) {
        mMessengerOutgoing = intent.getParcelableExtra(MainActivity.LINK_TO_MAIN_ACTIVITY);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("AlarmManager notification")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText(intent.getStringExtra(EditNoteActivity.LABEL))
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
        Message msg = Message.obtain();
        msg.what = MainActivity.ALARM_MANAGER_CLOCK_DONE;
        try {
            mMessengerOutgoing.send(msg);
        } catch (RemoteException aE) {
            aE.printStackTrace();
        }
    }
}
