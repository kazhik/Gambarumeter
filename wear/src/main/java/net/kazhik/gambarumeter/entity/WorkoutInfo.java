package net.kazhik.gambarumeter.entity;

/**
 * Created by kazhik on 14/11/16.
 */
public class WorkoutInfo {
    private long startTime;
    private long stopTime;
    private int stepCount;

    public WorkoutInfo(long startTime, long stopTime, int stepCount) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.stepCount = stepCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }
}
