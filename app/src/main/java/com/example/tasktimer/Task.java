package com.example.tasktimer;

import java.io.Serializable;

class Task implements Serializable {
    public static final long serialVersionUid = 06042020l;

    private long m_Id;
    private final String mName;
    private final String mDescription;
    private final int mSortOrder;


    public Task(long id, String mName, String mDescription, int mSortOrder) {
        m_Id = id;
        this.mName = mName;
        this.mDescription = mDescription;
        this.mSortOrder = mSortOrder;
    }

    public String getmName() {
        return mName;
    }

    public String getmDescription() {
        return mDescription;
    }

    public int getmSortOrder() {
        return mSortOrder;
    }

    public long getId() {
        return m_Id;
    }

    public void setId(long id) {
        this.m_Id = id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "m_Id=" + m_Id +
                ", mName='" + mName + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mSortOrder=" + mSortOrder +
                '}';
    }
}
