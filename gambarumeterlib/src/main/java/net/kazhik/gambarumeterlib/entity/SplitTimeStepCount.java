package net.kazhik.gambarumeterlib.entity;

/**
 * Created by kazhik on 15/12/17.
 */
public class SplitTimeStepCount extends SplitTime {
    private int stepCount;
    private int heartRate;

    public SplitTimeStepCount(long timestamp, float distance, int stepCount) {
        super(timestamp, distance);
        this.stepCount = stepCount;
        this.heartRate = 0;
    }

    public SplitTimeStepCount(long timestamp, float distance, int stepCount, int heartRate) {
        super(timestamp, distance);
        this.stepCount = stepCount;
        this.heartRate = heartRate;
    }

    public int getStepCount() {
        return stepCount;
    }

    public SplitTimeStepCount setStepCount(int stepCount) {
        this.stepCount = stepCount;
        return this;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}
