package com.example.batrakov.alarmmanagertask;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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
 * Activity that provide to create new Alarm and Job clocks and edit Alarm clock.
 * Include TimePickerFragment. Build new Alarm and send it to MainActivity.
 * Child of MainActivity.
 */
public class EditNoteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private CheckBox mRepeatableCheckBox;
    private EditText mAlarmLabel;
    private int mRepeatInterval;

    private static int sTimeHour;
    private static int sTimeMinute;

    private int mId;

    /**
     * Tag for taken hour.
     */
    public static final String TIME_HOUR = "hour";

    /**
     * Tag for taken minute.
     */
    public static final String TIME_MINUTE = "minute";

    /**
     * Tag for entered clock label.
     */
    public static final String LABEL = "label";

    /**
     * Tag for built alarm.
     */
    public static final String ALARM = "alarm";

    private static final int BORDER_FOR_CONCAT_TIME = 9;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.edit_alarm_ativity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView timePicker = (TextView) findViewById(R.id.trigger_time);
        Spinner intervalChoice = (Spinner) findViewById(R.id.interval_repeat);
        intervalChoice.setOnItemSelectedListener(this);
        Button confirmButton = (Button) findViewById(R.id.confirm_button);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        mRepeatableCheckBox = (CheckBox) findViewById(R.id.checkbox_repeat);
        mAlarmLabel = (EditText) findViewById(R.id.alarm_label);

        if (getIntent().hasExtra(MainActivity.CHANGE_ELEMENT)) {
            Alarm changeableAlarm = (Alarm) getIntent().getSerializableExtra(MainActivity.CHANGE_ELEMENT);
            sTimeHour = changeableAlarm.getTargetHour();
            sTimeMinute = changeableAlarm.getTargetMinute();
            timePicker.setText(changeableAlarm.getTimeString());
            if (changeableAlarm.isRepeatable()) {
                mRepeatableCheckBox.setChecked(true);
            }
            mAlarmLabel.setText(changeableAlarm.getLabel());
        } else {
            sTimeHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            sTimeMinute = Calendar.getInstance().get(Calendar.MINUTE);
            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("kk:mm", Locale.ENGLISH);
            timePicker.setText(sdf.format(currentTime.getTime()));
        }


        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (getIntent().getAction().equals(MainActivity.ALARM_MANAGER)) {
                getSupportActionBar().setTitle(getString(R.string.alarm_manager_header));
            } else {
                getSupportActionBar().setTitle(getString(R.string.job_scheduler_header));
            }

        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sec_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalChoice.setAdapter(adapter);

        timePicker.setTextColor(getResources().getColorStateList(R.color.text_view_colors, null));
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {
                DialogFragment newFragment = new TimePickerFragment(new TimePickerHandler());
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {
                onBackPressed();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View aView) {
                buildAlarm();
            }
        });


    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    /**
     * Allow to get taken time from TimePicker dialog fragment.
     */
    private static class TimePickerHandler extends Handler {
        @Override
        public void handleMessage(Message aMsg) {
            Bundle bundle = aMsg.getData();
            sTimeHour = bundle.getInt(TIME_HOUR);
            sTimeMinute = bundle.getInt(TIME_MINUTE);
            super.handleMessage(aMsg);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> aParent, View aView, int aPosition, long aId) {
        Matcher matcher = Pattern.compile("\\d+").matcher((String) aParent.getItemAtPosition(aPosition));
        String interval;
        if (matcher.find()) {
            interval = matcher.group();
            mRepeatInterval = Integer.parseInt(interval);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> aParent) {
    }

    /**
     * Allow to get trigger time from cute round clock UI.
     */
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private Handler mHandler;

        /**
         * Constructor.
         *
         * @param aHandler handler that allow to send data to external Activity.
         */
        public TimePickerFragment(Handler aHandler) {
            mHandler = aHandler;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle aSavedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        /**
         * Collects and send time data.
         *
         * @param aView TimePicker View.
         * @param aHourOfDay taken hour.
         * @param aMinute  taken minute.
         */
        public void onTimeSet(TimePicker aView, int aHourOfDay, int aMinute) {
            Bundle bundle = new Bundle();
            bundle.putInt(TIME_HOUR, aHourOfDay);
            bundle.putInt(TIME_MINUTE, aMinute);
            TextView textView = getActivity().findViewById(R.id.trigger_time);
            String hourStr = String.valueOf(aHourOfDay);
            String minuteStr = String.valueOf(aMinute);
            String targetTime;
            if (textView != null) {
                if (aHourOfDay <= BORDER_FOR_CONCAT_TIME) {
                    hourStr = "0" + hourStr;
                }
                if (aMinute <= BORDER_FOR_CONCAT_TIME) {
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

    /**
     * Assemble data to {@link Alarm} object and send it to MainActivity.
     */
    private void buildAlarm() {
            Intent dataToMainActivity = new Intent().putExtra(ALARM, new Alarm(mRepeatableCheckBox.isChecked(),
                            mRepeatInterval,
                            sTimeHour,
                            sTimeMinute,
                            String.valueOf(mAlarmLabel.getText())));
            setResult(RESULT_OK, dataToMainActivity);
            finish();
    }
}
