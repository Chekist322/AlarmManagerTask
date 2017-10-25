package com.example.batrakov.alarmmanagertask;

import java.io.Serializable;

/**
 * Created by batrakov on 25.10.17.
 */

public class Alarm implements Serializable {

    private boolean mRepeatable;
    private int mInterval;
    private int mTargetHour;
    private int mTargetMinute;
    private int mId;
    private String mLabel;

    public Alarm(boolean aRepeatable, int aInterval, int aTargetHour, int aTargetMinute, String aLabel) {
        mRepeatable = aRepeatable;
        mInterval = aInterval;
        mTargetHour = aTargetHour;
        mTargetMinute = aTargetMinute;
        mLabel = aLabel;
    }

    public boolean isRepeatable() {
        return mRepeatable;
    }

    public int getInterval() {
        return mInterval;
    }

    public int getTargetHour() {
        return mTargetHour;
    }

    public int getTargetMinute() {
        return mTargetMinute;
    }

    public String getTimeString() {
        String targetTime;
        String hourStr = String.valueOf(mTargetHour);
        String minuteStr = String.valueOf(mTargetMinute);
        if (mTargetHour <= 9) {
            hourStr = "0" + hourStr;
        }
        if (mTargetMinute <= 9) {
            minuteStr = "0" + minuteStr;
        }
        targetTime = hourStr + ":" + minuteStr;
        return targetTime;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String aLabel) {
        mLabel = aLabel;
    }

    public int getId() {
        return mId;
    }

    public void setId(int aId) {
        mId = aId;
    }
}
