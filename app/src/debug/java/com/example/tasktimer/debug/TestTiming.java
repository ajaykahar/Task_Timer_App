package com.example.tasktimer.debug;

public class TestTiming {
    long taskId;
    long starTime;
    long duration;

    public TestTiming(long taskId, long starTime, long duration) {
        this.taskId = taskId;
        this.starTime = starTime/1000;
        this.duration = duration;
    }
}
