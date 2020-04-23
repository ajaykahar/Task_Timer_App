package com.example.tasktimer;

import android.util.Log;

import java.io.Serializable;
import java.util.Date;

class Timing implements Serializable {
    private static final long serialVersionUID= 14042020L;
    private static final String TAG = Timing.class.getSimpleName();

    private long m_Id;
    private Task mTask;
    private long mStartTime;
    private long mDuration;

    public Timing(Task task) {
        mTask = task;
        // Initialize the start time to now and duration to zero for a new object;
        Date currentTime = new Date();
        mStartTime = currentTime.getTime() / 1000;   // we are only tracking whole seconds, not milliseconds
        mDuration=0;
    }

    long getId() {
        return m_Id;
    }

    void setId(long id) {
        m_Id = id;
    }

    Task getTask() {
        return mTask;
    }

    void setTask(Task mTask) {
        this.mTask = mTask;
    }

    long getStartTime() {
        return mStartTime;
    }

    void setStartTime(long StartTime) {
        mStartTime = StartTime;
    }

    long getmDuration() {
        return mDuration;
    }

    void setDuration() {
        // calculate teh duration from mStartTime to dateTime
        Date currentTime = new Date();
        mDuration = (currentTime.getTime() / 1000) - mStartTime;
        Log.d(TAG, "setDuration: "+mTask.getId()+" - Start time: "+mStartTime+" | Duration: "+mDuration);
    }
}
