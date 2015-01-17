package net.kazhik.gambarumeter.entity;

/**
 * Created by kazhik on 15/01/12.
 */
public class HeartRateDetail {
    private long timestamp;
    private int heartRate;
    private int stepCount;

    public long getTimestamp() {
        return timestamp;
    }

    public HeartRateDetail setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public HeartRateDetail setHeartRate(int heartRate) {
        this.heartRate = heartRate;
        return this;
    }

    public int getStepCount() {
        return stepCount;
    }

    public HeartRateDetail setStepCount(int stepCount) {
        this.stepCount = stepCount;
        return this;
    }
}
