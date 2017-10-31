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
 * Alarm Receiver class that allow to receive new alarm clock from AlarmManager.
 * Send clock state to MainActivity. Build Notification about clock.
 */
public class AlarmReceiver extends WakefulBroadcastReceiver {

    /**
     * Alarm clock Notification id.
     */
    public static final int NOTIFICATION_ID = 0;

    @Override
    public void onReceive(Context aContext, Intent aIntent) {
        Messenger messengerOutgoing = aIntent.getParcelableExtra(MainActivity.LINK_TO_MAIN_ACTIVITY);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(aContext);
        builder.setContentTitle("AlarmManager notification")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText(aIntent.getStringExtra(EditNoteActivity.LABEL))
                .setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager manager = (NotificationManager) aContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());

        if (!aIntent.getBooleanExtra(MainActivity.IS_JOB_REPEATABLE, false)) {
            Message msg = Message.obtain();
            msg.what = MainActivity.ALARM_MANAGER_CLOCK_DONE;
            try {
                messengerOutgoing.send(msg);
            } catch (RemoteException aE) {
                aE.printStackTrace();
            }
        }
    }
}
