package com.example.batrakov.alarmmanagertask;

import android.app.AlarmManager;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
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
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ImageButton mAddAlarmManagerElement;
    ImageButton mAddJobSchedulerElement;
    TextView mAlarmHeader;
    TextView mAlarmLabel;
    TextView mAlarmTime;
    CheckBox mAlarmRepeatable;
    Button mAlarmCancel;
    View mAlarmData;
    View mAlarmClockView;
    int mAlarmRepeat;
    Alarm mAlarmManagerAlarmClock;
    PendingIntent mAlarmIntent;
    AlarmManager mAlarmManager;
    AlarmHandler mAlarmHandler;

    TextView mJobSchedulerHeader;
    ArrayList<Alarm> mJobSchedulerAlarmClocks;
    RecyclerView mRecyclerView;
    ListAdapter mListAdapter;
    Intent mStartJobServiceIntent;

    public static final String EDIT_ALARM_MANAGER_ELEMENT = "alarm manager edit";
    public static final String EDIT_JOB_SCHEDULER_ELEMENT = "job scheduler edit";
    public static final String CHANGE_ELEMENT = "change";
    public static final String LINK_TO_MAIN_ACTIVITY = "link";

    public static final int START_FOR_RESULT_ALARM_MANAGER_NOTE = 0;
    public static final int START_FOR_RESULT_JOB_SCHEDULER_NOTE = 1;

    public static final int ALARM_MANAGER_CLOCK_DONE = 2;

    @Override
    protected void onResume() {
        System.out.println(mAlarmIntent == null);
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_activity);
        mJobSchedulerAlarmClocks = new ArrayList<>();
        mAlarmHandler = new AlarmHandler(this);

        mAddAlarmManagerElement = (ImageButton) findViewById(R.id.add_alarm_manager);
        mAddJobSchedulerElement = (ImageButton) findViewById(R.id.add_job_scheduller);
        mAlarmData = findViewById(R.id.alarm_data);
        mAlarmLabel = (TextView) findViewById(R.id.label_alarm_manager);
        mAlarmHeader = (TextView) findViewById(R.id.alarm_manager_header);
        mAlarmTime = (TextView) findViewById(R.id.trigger_time_view);
        mAlarmCancel = (Button) findViewById(R.id.cancel_view);
        mAlarmRepeatable = (CheckBox) findViewById(R.id.repeatable_view);
        mRecyclerView = (RecyclerView) findViewById(R.id.list);
        mJobSchedulerHeader = (TextView) findViewById(R.id.job_sheduler_header);
        mAlarmClockView = findViewById(R.id.alaarm_manager_element);

        mListAdapter = new ListAdapter(mJobSchedulerAlarmClocks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mListAdapter);
        mListAdapter.replaceData(mJobSchedulerAlarmClocks);


        mAddAlarmManagerElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startEditActivity = new Intent(getApplicationContext(), EditNoteActivity.class);
                startActivityForResult(startEditActivity, START_FOR_RESULT_ALARM_MANAGER_NOTE);
            }
        });

        mAddJobSchedulerElement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startEditActivity = new Intent(getApplicationContext(), EditNoteActivity.class);
                startActivityForResult(startEditActivity, START_FOR_RESULT_JOB_SCHEDULER_NOTE);
            }
        });

        mAlarmClockView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startEditActivity = new Intent(getApplicationContext(), EditNoteActivity.class);
                startEditActivity.putExtra(CHANGE_ELEMENT, mAlarmManagerAlarmClock);
                startActivityForResult(startEditActivity, START_FOR_RESULT_ALARM_MANAGER_NOTE);
            }
        });

        mStartJobServiceIntent = new Intent(this, JobSchedulerService.class);
        startService(mStartJobServiceIntent);
    }

    private static class AlarmHandler extends Handler {

        private WeakReference<MainActivity> mActivity;

        AlarmHandler(MainActivity aMainActivity) {
            super(/* default looper */);
            this.mActivity = new WeakReference<>(aMainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            final MainActivity mainActivity = mActivity.get();
            if (mainActivity == null) {
                return;
            }
            switch (msg.what) {
                case ALARM_MANAGER_CLOCK_DONE:
                    mainActivity.mAlarmCancel.setText(mainActivity.getString(R.string.delete));
                    mainActivity.mAlarmCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mainActivity.mAlarmData.setVisibility(View.GONE);
                            mainActivity.mAlarmLabel.setVisibility(View.GONE);
                            mainActivity.mAlarmHeader.setVisibility(View.GONE);
                        }
                    });
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case START_FOR_RESULT_ALARM_MANAGER_NOTE:
                if (resultCode == RESULT_OK) {
                    mAlarmData.setVisibility(View.VISIBLE);
                    mAlarmLabel.setVisibility(View.VISIBLE);
                    mAlarmHeader.setVisibility(View.VISIBLE);
                    mAlarmCancel.setText(getString(R.string.cancel));

                    mAlarmCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAlarmManager.cancel(mAlarmIntent);
                            mAlarmHandler.sendEmptyMessage(ALARM_MANAGER_CLOCK_DONE);
                        }
                    });

                    mAlarmManagerAlarmClock = (Alarm) data.getSerializableExtra(EditNoteActivity.ALARM);
                    mAlarmRepeatable.setChecked(mAlarmManagerAlarmClock.isRepeatable());
                    if (mAlarmRepeatable.isChecked()) {
                        mAlarmRepeat = mAlarmManagerAlarmClock.getInterval();
                        String targetRepeat = "Repeatable" + ": interval is " + String.valueOf(mAlarmRepeat) + "sec";
                        mAlarmRepeatable.setText(targetRepeat);
                    } else {
                        String targetRepeat = "Without repeat";
                        mAlarmRepeatable.setText(targetRepeat);
                    }
                    if (mAlarmManagerAlarmClock.getLabel().equals("")) {
                        mAlarmManagerAlarmClock.setLabel("no label");
                    }
                    mAlarmLabel.setText(mAlarmManagerAlarmClock.getLabel());
                    mAlarmTime.setText(mAlarmManagerAlarmClock.getTimeString());
                    startAlarmManagerAlarmClock(mAlarmManagerAlarmClock);
                }
                break;
            case START_FOR_RESULT_JOB_SCHEDULER_NOTE:
                if (resultCode == RESULT_OK) {
                    startJobSchedulerAlarmClock((Alarm) data.getSerializableExtra(EditNoteActivity.ALARM));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startAlarmManagerAlarmClock(Alarm aAlarm) {

        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class).putExtra(EditNoteActivity.LABEL, aAlarm.getLabel());

        Messenger messengerIncoming = new Messenger(mAlarmHandler);
        intent.putExtra(LINK_TO_MAIN_ACTIVITY, messengerIncoming);

        mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, aAlarm.getTargetHour());
        calendar.set(Calendar.MINUTE, aAlarm.getTargetMinute());
        if (aAlarm.isRepeatable()) {
            mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), aAlarm.getInterval() * 1000, mAlarmIntent);
        } else {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), mAlarmIntent);
        }
    }

    private void startJobSchedulerAlarmClock(Alarm aAlarm) {
        Calendar calendar = Calendar.getInstance();
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(Calendar.HOUR_OF_DAY, aAlarm.getTargetHour());
        targetCalendar.set(Calendar.MINUTE, aAlarm.getTargetMinute());
        ComponentName jobSchedulerComponentName = new ComponentName(this, JobSchedulerService.class);
        JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(mJobSchedulerAlarmClocks.size(), jobSchedulerComponentName)
                .setPersisted(true)
                .setBackoffCriteria(1000, JobInfo.BACKOFF_POLICY_LINEAR)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .setMinimumLatency(targetCalendar.getTimeInMillis() - calendar.getTimeInMillis())
                .build();
        jobScheduler.schedule(jobInfo);

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
        }

        /**
         * View filling.
         *
         * @param aAlarm alarm from list
         */
        void bindView(Alarm aAlarm) {
            mLabel.setText(aAlarm.getLabel());
            mTime.setText(aAlarm.getTimeString());
            if (aAlarm.isRepeatable()) {
                mRepeatable.setChecked(true);
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

        @Override
        public ListHolder onCreateViewHolder(ViewGroup aParent, int aViewType) {
            View rowView = LayoutInflater.from(aParent.getContext()).inflate(R.layout.list_item, aParent, false);
            return new ListHolder(rowView);
        }

        @Override
        public void onBindViewHolder(ListHolder aHolder, int aPosition) {
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
