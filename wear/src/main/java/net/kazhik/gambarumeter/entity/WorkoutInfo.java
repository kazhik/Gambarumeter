package net.kazhik.gambarumeter.entity;

/**
 * Created by kazhik on 14/11/16.
 */
public class WorkoutInfo {
    private long startTime;
    private long stopTime;
    private int stepCount;
    private float distance;
    private int heartRate;

    public WorkoutInfo(long startTime, long stopTime, int stepCount, float distance, int heartRate) {
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.stepCount = stepCount;
        this.distance = distance;
        this.heartRate = heartRate;
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

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public int getHeartRate() {
        return this.heartRate;
    }
    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

}
