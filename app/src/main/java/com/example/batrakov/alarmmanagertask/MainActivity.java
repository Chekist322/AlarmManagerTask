package com.example.batrakov.alarmmanagertask;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Main application Activity. Represent single AlarmManager alarm clock and RecyclerView list of
 * JobScheduler alarm clocks. Include handler to communicate with AlarmReceiver and JobSchedulerService.
 * Contain RecyclerView adapter and holder which allows to show ArrayList of JobScheduler  Alarms as list.
 * Launch AlarmManager and JobScheduler alarms.
 */
public class MainActivity extends AppCompatActivity {

    private TextView mAlarmHeader;
    private TextView mAlarmLabel;
    private TextView mAlarmTime;
    private CheckBox mAlarmRepeatable;
    private Button mAlarmCancel;
    private View mAlarmData;
    private Alarm mAlarmManagerAlarmClock;
    private PendingIntent mAlarmIntent;
    private AlarmManager mAlarmManager;
    private AlarmHandler mAlarmHandler;

    private TextView mJobSchedulerHeader;
    private ArrayList<Alarm> mJobSchedulerAlarmClocks;
    private ListAdapter mListAdapter;


    /**
     * Tag means that EditNoteActivity was started for creating AlarmManager clock.
     */
    public static final String ALARM_MANAGER = "alarm manager";

    /**
     * Tag means that EditNoteActivity was started for creating JobScheduler clock.
     */
    public static final String JOB_SCHEDULER = "job scheduler";

    /**
     * Tag for clock tah need to be changed.
     */
    public static final String CHANGE_ELEMENT = "change";

    /**
     * Tag for MainActivity Messenger.
     */
    public static final String LINK_TO_MAIN_ACTIVITY = "link";

    /**
     * Flag for Job repeatable state.
     */
    public static final String IS_JOB_REPEATABLE = "repeat";

    /**
     * Tag for Job label.
     */
    public static final String LABEL_TO_JOB_NOTIFICATION = "label for job";

    /**
     * Tag for Job trigger hour.
     */
    public static final String HOUR_TO_JOB_NOTIFICATION = "hour for job";

    /**
     * Tag got Job trigger minute.
     */
    public static final String MINUTE_TO_JOB_NOTIFICATION = "minute for job";

    /**
     * Tag for Job repeat interval.
     */
    public static final String INTERVAL_TO_JOB_NOTIFICATION = "interval for job";

    /**
     * Tag that means start {@link EditNoteActivity} with request code for AlarmManager.
     */
    public static final int START_FOR_RESULT_ALARM_MANAGER_ELEMENT = 0;

    /**
     * Tag that means start {@link EditNoteActivity} with request code for JobScheduler.
     */
    public static final int START_FOR_RESULT_JOB_SCHEDULER_ELEMENT = 1;

    /**
     * Tag for incoming messages means that alarm clock finished.
     */
    public static final int ALARM_MANAGER_CLOCK_DONE = 2;

    /**
     * Tag for incoming messages means that JobScheduler clock finished.
     */
    public static final int JOB_SCHEDULER_CLOCK_DONE = 3;

    private static final int SECOND_TO_MILLISECOND_MULTIPLIER = 1000;

    @Override
    protected void onCreate(Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.list_activity);
        mJobSchedulerAlarmClocks = new ArrayList<>();
        mAlarmHandler = new AlarmHandler(this);

        ImageButton addAlarmManagerElement = (ImageButton) findViewById(R.id.add_alarm_manager);
        ImageButton addJobSchedulerElement = (ImageButton) findViewById(R.id.add_job_scheduller);
        mAlarmData = findViewById(R.id.alarm_data);
        mAlarmLabel = (TextView) findViewById(R.id.label_alarm_manager);
        mAlarmHeader = (TextView) findViewById(R.id.alarm_manager_header);
        mAlarmTime = (TextView) findViewById(R.id.trigger_time_view);
        mAlarmCancel = (Button) findViewById(R.id.cancel_view);
        mAlarmRepeatable = (CheckBox) findViewById(R.id.repeatable_view);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.list);
        mJobSchedulerHeader = (TextView) findViewById(R.id.job_sheduler_header);
        View alarmClockView = findViewById(R.id.alaarm_manager_element);

        addAlarmManagerElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {
                Intent startEditActivity = new Intent(getApplicationContext(), EditNoteActivity.class);
                startEditActivity.setAction(ALARM_MANAGER);
                startActivityForResult(startEditActivity, START_FOR_RESULT_ALARM_MANAGER_ELEMENT);
            }
        });

        addJobSchedulerElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {
                Intent startEditActivity = new Intent(getApplicationContext(), EditNoteActivity.class);
                startEditActivity.setAction(JOB_SCHEDULER);
                startActivityForResult(startEditActivity, START_FOR_RESULT_JOB_SCHEDULER_ELEMENT);
            }
        });

        alarmClockView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {
                Intent startEditActivity = new Intent(getApplicationContext(), EditNoteActivity.class);
                startEditActivity.setAction(ALARM_MANAGER);
                startEditActivity.putExtra(CHANGE_ELEMENT, mAlarmManagerAlarmClock);
                startActivityForResult(startEditActivity, START_FOR_RESULT_ALARM_MANAGER_ELEMENT);
            }
        });

        restoreAlarmManagerAlarmClock();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        List<JobInfo> allPendingJobs;
        if (jobScheduler != null) {
            allPendingJobs = jobScheduler.getAllPendingJobs();
            for (int i = 0; i < allPendingJobs.size(); i++) {
                System.out.println(allPendingJobs.size());
                PersistableBundle persistableBundle = new PersistableBundle(allPendingJobs.get(i).getExtras());
                Alarm restoredAlarm = new Alarm(persistableBundle.getBoolean(IS_JOB_REPEATABLE),
                        persistableBundle.getInt(HOUR_TO_JOB_NOTIFICATION),
                        persistableBundle.getInt(MINUTE_TO_JOB_NOTIFICATION),
                        persistableBundle.getString(LABEL_TO_JOB_NOTIFICATION));
                restoredAlarm.setJobId(allPendingJobs.get(i).getId());
                mJobSchedulerAlarmClocks.add(restoredAlarm);
            }
        }

        mListAdapter = new ListAdapter(mJobSchedulerAlarmClocks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mListAdapter);
        mListAdapter.replaceData(mJobSchedulerAlarmClocks);

        Messenger messengerIncoming = new Messenger(mAlarmHandler);
        Intent startJobServiceIntent = new Intent(this, JobSchedulerService.class);
        startJobServiceIntent.putExtra(LINK_TO_MAIN_ACTIVITY, messengerIncoming);
        startService(startJobServiceIntent);
    }

    /**
     * Allow to get messages from {@link JobSchedulerService} and {@link AlarmReceiver}.
     */
    private static class AlarmHandler extends Handler {
        private WeakReference<MainActivity> mActivity;

        /**
         * Constructor.
         *
         * @param aMainActivity link to current MainActivity.
         */
        AlarmHandler(MainActivity aMainActivity) {
            super();
            this.mActivity = new WeakReference<>(aMainActivity);
        }

        @Override
        public void handleMessage(Message aMsg) {
            final MainActivity mainActivity = mActivity.get();
            if (mainActivity == null) {
                return;
            }
            switch (aMsg.what) {
                case ALARM_MANAGER_CLOCK_DONE:
                    mainActivity.mAlarmCancel.setText(mainActivity.getString(R.string.delete));
                    mainActivity.mAlarmCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View aView) {
                            mainActivity.mAlarmData.setVisibility(View.GONE);
                            mainActivity.mAlarmLabel.setVisibility(View.GONE);
                            mainActivity.mAlarmHeader.setVisibility(View.GONE);
                        }
                    });
                    break;
                case JOB_SCHEDULER_CLOCK_DONE:
                    mainActivity.mListAdapter.updateItem(aMsg.arg1);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int aRequestCode, int aResultCode, Intent aData) {
        switch (aRequestCode) {
            case START_FOR_RESULT_ALARM_MANAGER_ELEMENT:
                if (aResultCode == RESULT_OK) {
                    mAlarmData.setVisibility(View.VISIBLE);
                    mAlarmLabel.setVisibility(View.VISIBLE);
                    mAlarmHeader.setVisibility(View.VISIBLE);
                    mAlarmCancel.setText(getString(R.string.cancel));

                    mAlarmCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View aView) {
                            mAlarmManager.cancel(mAlarmIntent);
                            mAlarmHandler.sendEmptyMessage(ALARM_MANAGER_CLOCK_DONE);
                        }
                    });

                    mAlarmManagerAlarmClock = (Alarm) aData.getSerializableExtra(EditNoteActivity.ALARM);
                    mAlarmRepeatable.setChecked(mAlarmManagerAlarmClock.isRepeatable());
                    if (mAlarmRepeatable.isChecked()) {
                        int alarmRepeat = mAlarmManagerAlarmClock.getInterval();
                        String targetRepeat = "Repeatable" + ": interval is " + String.valueOf(alarmRepeat) + "sec";
                        mAlarmRepeatable.setText(targetRepeat);
                    } else {
                        String targetRepeat = "Without repeat";
                        mAlarmRepeatable.setText(targetRepeat);
                    }
                    if (mAlarmManagerAlarmClock.getLabel().equals("")) {
                        mAlarmManagerAlarmClock.setNoLabel();
                    }
                    mAlarmLabel.setText(mAlarmManagerAlarmClock.getLabel());
                    mAlarmTime.setText(mAlarmManagerAlarmClock.getTimeString());
                    startAlarmManagerAlarmClock(mAlarmManagerAlarmClock);
                }
                break;
            case START_FOR_RESULT_JOB_SCHEDULER_ELEMENT:
                if (aResultCode == RESULT_OK) {
                    startJobSchedulerAlarmClock((Alarm) aData.getSerializableExtra(EditNoteActivity.ALARM));
                }
                break;
            default:
                break;
        }
        super.onActivityResult(aRequestCode, aResultCode, aData);
    }

    /**
     * Start AlarmManager clock. Start {@link AlarmReceiver} receiving.
     *
     * @param aAlarm target alarm clock.
     */
    private void startAlarmManagerAlarmClock(Alarm aAlarm) {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class).putExtra(EditNoteActivity.LABEL, aAlarm.getLabel());

        Messenger messengerIncoming = new Messenger(mAlarmHandler);
        intent.putExtra(LINK_TO_MAIN_ACTIVITY, messengerIncoming);
        intent.putExtra(IS_JOB_REPEATABLE, aAlarm.isRepeatable());

        mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, aAlarm.getTargetHour());
        calendar.set(Calendar.MINUTE, aAlarm.getTargetMinute());


        if (aAlarm.isRepeatable()) {
            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    aAlarm.getInterval() * SECOND_TO_MILLISECOND_MULTIPLIER, mAlarmIntent);
        } else {
            AlarmManager.AlarmClockInfo alarmInfo = new AlarmManager
                    .AlarmClockInfo(calendar.getTimeInMillis(), mAlarmIntent);
            mAlarmManager.setAlarmClock(alarmInfo, mAlarmIntent);
        }
    }

    /**
     * Restore last alarm clock from device memory if exist.
     */
    private void restoreAlarmManagerAlarmClock() {
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (mAlarmManager != null) {
            mAlarmIntent = mAlarmManager.getNextAlarmClock().getShowIntent();
            if (mAlarmIntent != null) {
                if (mAlarmIntent.getCreatorPackage() != null) {
                    if (mAlarmIntent.getCreatorPackage().equals("com.example.batrakov.alarmmanagertask")) {
                        mAlarmData.setVisibility(View.VISIBLE);
                        mAlarmLabel.setVisibility(View.VISIBLE);
                        mAlarmHeader.setVisibility(View.VISIBLE);
                        mAlarmCancel.setText(getString(R.string.cancel));

                        mAlarmCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View aView) {
                                mAlarmManager.cancel(mAlarmIntent);
                                mAlarmHandler.sendEmptyMessage(ALARM_MANAGER_CLOCK_DONE);
                            }
                        });

                        mAlarmRepeatable.setChecked(false);
                        String unknown = "unknown (state can't be restored)";
                        mAlarmRepeatable.setText(unknown);
                        mAlarmLabel.setText(unknown);

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(mAlarmManager.getNextAlarmClock().getTriggerTime());
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm", Locale.ENGLISH);

                        mAlarmTime.setText(simpleDateFormat.format(calendar.getTime()));
                    }
                }
            }
        }
    }

    /**
     * Build new JobInfo object and send it to {@link JobSchedulerService}.
     *
     * @param aAlarm target alarm clock.
     */
    private void startJobSchedulerAlarmClock(Alarm aAlarm) {
        Calendar calendar = Calendar.getInstance();
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(Calendar.HOUR_OF_DAY, aAlarm.getTargetHour());
        targetCalendar.set(Calendar.MINUTE, aAlarm.getTargetMinute());
        targetCalendar.set(Calendar.SECOND, 0);
        ComponentName jobSchedulerComponentName = new ComponentName(this, JobSchedulerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(mJobSchedulerAlarmClocks.size(), jobSchedulerComponentName)
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        if (aAlarm.isRepeatable()) {
            System.out.println(aAlarm.getInterval());
            builder.setPeriodic(aAlarm.getInterval() * SECOND_TO_MILLISECOND_MULTIPLIER);
        } else {
            builder.setMinimumLatency(targetCalendar.getTimeInMillis() - calendar.getTimeInMillis());
        }

        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

        PersistableBundle persistableBundle = new PersistableBundle();
        persistableBundle.putBoolean(IS_JOB_REPEATABLE, aAlarm.isRepeatable());
        persistableBundle.putString(LABEL_TO_JOB_NOTIFICATION, aAlarm.getLabel());
        persistableBundle.putInt(HOUR_TO_JOB_NOTIFICATION, aAlarm.getTargetHour());
        persistableBundle.putInt(MINUTE_TO_JOB_NOTIFICATION, aAlarm.getTargetMinute());
        persistableBundle.putInt(INTERVAL_TO_JOB_NOTIFICATION, aAlarm.getInterval());

        builder.setExtras(persistableBundle);

        JobInfo jobInfo = builder.build();
        if (jobScheduler != null) {
            jobScheduler.schedule(jobInfo);
        }
        aAlarm.setJobId(jobInfo.getId());
        mJobSchedulerAlarmClocks.add(aAlarm);
        mListAdapter.replaceData(mJobSchedulerAlarmClocks);
    }

    /**
     * Holder for RecyclerView. Contain single list element.
     */
    private final class ListHolder extends RecyclerView.ViewHolder {

        private TextView mLabel;
        private TextView mTime;
        private CheckBox mRepeatable;
        private View mContainer;
        private Button mCancel;


        /**
         * Constructor.
         *
         * @param aItemView item view
         */
        private ListHolder(View aItemView) {
            super(aItemView);

            mLabel = aItemView.findViewById(R.id.job_label);
            mTime = aItemView.findViewById(R.id.job_time);
            mRepeatable = aItemView.findViewById(R.id.job_repeatable);
            mContainer = aItemView.findViewById(R.id.job_info);
            mCancel = aItemView.findViewById(R.id.job_cancel);
        }

        /**
         * View filling.
         *
         * @param aAlarm alarm from list
         */
        void bindView(final Alarm aAlarm) {
            mLabel.setText(aAlarm.getLabel());
            mTime.setText(aAlarm.getTimeString());

            mRepeatable.setChecked(aAlarm.isRepeatable());
            if (mRepeatable.isChecked()) {
                String targetRepeat = "Repeatable" + ": interval is " + String.valueOf(aAlarm.getInterval()) + "sec";
                mRepeatable.setText(targetRepeat);
            } else {
                String targetRepeat = "Without repeat";
                mRepeatable.setText(targetRepeat);
            }

            if (aAlarm.isDone()) {
                mLabel.setBackground(getDrawable(R.drawable.grey_rect));
                mContainer.setBackground(getDrawable(R.drawable.grey_rect));
                mCancel.setText(getString(R.string.delete));
                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View aView) {
                        mListAdapter.deleteItem(getAdapterPosition());
                    }
                });
            } else {
                mLabel.setBackground(getDrawable(R.drawable.white_rect));
                mContainer.setBackground(getDrawable(R.drawable.white_rect));
                mCancel.setText(getString(R.string.cancel));
                mCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View aView) {
                        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);

                        if (jobScheduler != null) {
                            jobScheduler.cancel(aAlarm.getJobId());
                        }
                        mJobSchedulerAlarmClocks.remove(getAdapterPosition());
                        if (getAdapterPosition() == 0) {
                            mListAdapter.notifyDataSetChanged();
                        } else {
                            mListAdapter.notifyItemRemoved(getAdapterPosition());
                        }

                        if (mJobSchedulerAlarmClocks.isEmpty()) {
                            mJobSchedulerHeader.setVisibility(View.GONE);
                        }
                    }
                });
            }

        }
    }

    /**
     * Adapter for recycler view. Allow to fill and update list.
     */
    private class ListAdapter extends RecyclerView.Adapter<ListHolder> {

        private ArrayList<Alarm> mList;

        /**
         * Constructor.
         *
         * @param aList target list for fill.
         */
        ListAdapter(ArrayList<Alarm> aList) {
            setHasStableIds(true);
            mList = aList;
        }

        /**
         * List updating.
         *
         * @param aList new target list.
         */
        void replaceData(ArrayList<Alarm> aList) {
            if (mList != null) {
                if (!mList.isEmpty()) {
                    mJobSchedulerHeader.setVisibility(View.VISIBLE);
                } else {
                    mJobSchedulerHeader.setVisibility(View.INVISIBLE);
                }
                mList = aList;
                notifyDataSetChanged();
            }
        }

        /**
         * Update item in RecyclerView when single job finished.
         *
         * @param aId target pending job id.
         */
        void updateItem(int aId) {
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            List<JobInfo> allPendingJobs;
            if (jobScheduler != null) {
                allPendingJobs = jobScheduler.getAllPendingJobs();
                int jobListId = 0;
                for (int i = 0; i < allPendingJobs.size(); i++) {
                    if (mList.get(i).getJobId() == aId) {
                        jobListId = i;
                        break;
                    }
                }

                mList.get(jobListId).setDone();
                notifyItemChanged(jobListId);
            }
        }

        /**
         * Delete item from RecyclerView.
         *
         * @param aId item position in list.
         */
        void deleteItem(int aId) {

            mJobSchedulerAlarmClocks.remove(aId);
            if (aId == 0) {
                notifyDataSetChanged();
            } else {
                notifyItemRemoved(aId);
            }
            if (mJobSchedulerAlarmClocks.size() == 0) {
                mJobSchedulerHeader.setVisibility(View.GONE);
            }
        }

        @Override
        public ListHolder onCreateViewHolder(ViewGroup aParent, int aViewType) {
            View rowView = LayoutInflater.from(aParent.getContext()).inflate(R.layout.list_item, aParent, false);
            return new ListHolder(rowView);
        }

        @Override
        public void onBindViewHolder(final ListHolder aHolder, final int aPosition) {

            Alarm alarm = mList.get(aPosition);
            aHolder.bindView(alarm);
        }

        @Override
        public long getItemId(int aIndex) {
            return aIndex;
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

    }
}
