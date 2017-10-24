package com.example.batrakov.alarmmanagertask;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.AlarmManagerCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by batrakov on 24.10.17.
 */

public class EditNoteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    Toolbar mToolbar;
    TextView mTimePicker;
    Spinner mIntervalChoice;
    Button mConfirmButton;
    Button mCancelButton;
    CheckBox mRepeatableCheckBox;
    EditText mAlarmLabel;
    PendingIntent mAlarmIntent;
    int mRepeatInterval;
    Handler mHandler;

    private static int sTimeHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private static int sTimeMinute = Calendar.getInstance().get(Calendar.MINUTE);

    public static final String TIME_HOUR = "hour";
    public static final String TIME_MINUTE = "minute";

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_alarm_ativity);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTimePicker = (TextView) findViewById(R.id.trigger_time);
        mIntervalChoice = (Spinner) findViewById(R.id.interval_repeat);
        mIntervalChoice.setOnItemSelectedListener(this);
        mConfirmButton = (Button) findViewById(R.id.confirm_button);
        mCancelButton = (Button) findViewById(R.id.cancel_button);
        mRepeatableCheckBox = (CheckBox) findViewById(R.id.checkbox_repeat);
        mAlarmLabel = (EditText) findViewById(R.id.alarm_label);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sec_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mIntervalChoice.setAdapter(adapter);

        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm", Locale.ENGLISH);
        mTimePicker.setText(sdf.format(currentTime.getTime()));
        mTimePicker.setTextColor(getResources().getColorStateList(R.color.text_view_colors, null));
        mTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment(new TimePickerHandler());
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchAlarm();
            }
        });
    }

    class TimePickerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            sTimeHour = bundle.getInt(TIME_HOUR);
            sTimeMinute = bundle.getInt(TIME_MINUTE);
            super.handleMessage(msg);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Matcher matcher = Pattern.compile("\\d+").matcher((String) parent.getItemAtPosition(position));
        String interval;
        if (matcher.find()) {
            interval = matcher.group();
            mRepeatInterval = Integer.parseInt(interval);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        Handler mHandler;

        public TimePickerFragment (Handler aHandler) {
            mHandler = aHandler;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            Bundle bundle = new Bundle();
            bundle.putInt(TIME_HOUR, hourOfDay);
            bundle.putInt(TIME_MINUTE, minute);
            TextView textView = getActivity().findViewById(R.id.trigger_time);
            String hourStr = String.valueOf(hourOfDay);
            String minuteStr = String.valueOf(minute);
            String targetTime;
            if (textView != null) {
                if (hourOfDay <= 9) {
                    hourStr = "0" + hourStr;
                }
                if (minute <= 9) {
                    minuteStr = "0" + minuteStr;
                }
                targetTime = hourStr + ":" + minuteStr;
                textView.setText(targetTime);
            }
            Message msg = new Message();
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
    }

    private void launchAlarm() {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        mAlarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, sTimeHour);
        calendar.set(Calendar.MINUTE, sTimeMinute);

        System.out.println(sTimeHour);
        System.out.println(sTimeMinute);
        System.out.println(calendar.getTimeInMillis());

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), mAlarmIntent);
    }
}
