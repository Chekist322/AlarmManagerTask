package com.example.batrakov.alarmmanagertask;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

/**
 * Created by batrakov on 25.10.17.
 */

public class JobSchedulerService extends JobService {
    private static final int NOTIFICATION_ID = 1;

    @Override
    public boolean onStartJob(JobParameters params) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Scheduled Notification")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText("test")
                .setSmallIcon(R.mipmap.ic_launcher);

        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, builder.build());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
