package com.example.batrakov.alarmmanagertask;

import java.io.Serializable;

/**
 * Represents single Alarm clock. Contains full alarm information.
 */
class Alarm implements Serializable {

    private boolean mDone;
    private boolean mRepeatable;
    private int mInterval;
    private int mTargetHour;
    private int mTargetMinute;
    private String mLabel;
    private int mJobId;

    private static final int BORDER_FOR_CONCAT_TIME = 9;

    /**
     * Constructor.
     *
     * @param aRepeatable flag means that alarm is repeatable or not
     * @param aTargetHour trigger hour
     * @param aTargetMinute trigger minute
     * @param aLabel label for alarm clock
     */
    Alarm(boolean aRepeatable, int aTargetHour, int aTargetMinute, String aLabel) {
        mRepeatable = aRepeatable;
        mInterval = 60;
        mTargetHour = aTargetHour;
        mTargetMinute = aTargetMinute;
        mLabel = aLabel;
        if (mLabel.equals("")) {
            mLabel = "no label";
        }
        mDone = false;
    }

    /**
     * Is current alarm clock repeatable.
     *
     * @return {@code true} if repeatable
     */
    boolean isRepeatable() {
        return mRepeatable;
    }

    /**
     * Get repeat interval.
     *
     * @return repeat interval
     */
    int getInterval() {
        return mInterval;
    }

    /**
     * Get target hour.
     *
     * @return trigger hour
     */
    int getTargetHour() {
        return mTargetHour;
    }

    /**
     * Get target minute.
     *
     * @return trigger minute
     */
    int getTargetMinute() {
        return mTargetMinute;
    }

    /**
     * Get time in string.
     *
     * @return trigger minutes and hours in representative String.
     */
    String getTimeString() {
        String targetTime;
        String hourStr = String.valueOf(mTargetHour);
        String minuteStr = String.valueOf(mTargetMinute);
        if (mTargetHour <= BORDER_FOR_CONCAT_TIME) {
            hourStr = "0" + hourStr;
        }
        if (mTargetMinute <= BORDER_FOR_CONCAT_TIME) {
            minuteStr = "0" + minuteStr;
        }
        targetTime = hourStr + ":" + minuteStr;
        return targetTime;
    }

    /**
     * Get label.
     *
     * @return label for alarm clock.
     */
    String getLabel() {
        return mLabel;
    }

    /**
     * Set label.
     *
     * @param aLabel target label.
     */
    void setLabel(String aLabel) {
        mLabel = aLabel;
    }

    /**
     * Is current clock finished.
     *
     * @return {@code true} if finished.
     */
    boolean isDone() {
        return mDone;
    }

    /**
     * Set clock state.
     *
     * @param aDone target state.
     */
    void setDone(boolean aDone) {
        mDone = aDone;
    }

    /**
     * Get job id.
     *
     * @return JobInfo id.
     */
    int getJobId() {
        return mJobId;
    }

    /**
     * Set job id.
     *
     * @param aJobId target JobInfo id.
     */
    void setJobId(int aJobId) {
        mJobId = aJobId;
    }
}
