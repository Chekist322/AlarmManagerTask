package com.example.batrakov.alarmmanagertask;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;


/**
 * Serves new Jobs from MainActivity and send their state to MainActivity by Handler.
 * Build Notification for each job.
 */
public class JobSchedulerService extends JobService {

    private Messenger mMessengerOutgoing;

    @Override
    public int onStartCommand(Intent aIntent, int aFlags, int aStartId) {
        mMessengerOutgoing = aIntent.getParcelableExtra(MainActivity.LINK_TO_MAIN_ACTIVITY);
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters aParams) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Job scheduler Notification")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentText(aParams.getExtras().getString(MainActivity.LABEL_TO_JOB_NOTIFICATION))
                .setSmallIcon(R.mipmap.ic_launcher);
        int jobId = aParams.getJobId();
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(jobId, builder.build());
        Message msg = Message.obtain();
        msg.what = MainActivity.JOB_SCHEDULER_CLOCK_DONE;
        msg.arg1 = jobId;
        try {
            mMessengerOutgoing.send(msg);
            jobFinished(aParams, false);
        } catch (RemoteException aE) {
            aE.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters aParams) {
        return true;
    }
}
