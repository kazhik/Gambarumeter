package net.kazhik.gambarumeterlib.entity;

/**
 * Created by kazhik on 15/12/17.
 */
public class SplitTimeStepCount extends SplitTime {
    private int stepCount;

    public SplitTimeStepCount(long timestamp, float distance, int stepCount) {
        super(timestamp, distance);
        this.stepCount = stepCount;
    }

    public SplitTimeStepCount(long timestamp, float distance) {
        super(timestamp, distance);
    }

    public int getStepCount() {
        return stepCount;
    }

    public SplitTimeStepCount setStepCount(int stepCount) {
        this.stepCount = stepCount;
        return this;
    }
}
