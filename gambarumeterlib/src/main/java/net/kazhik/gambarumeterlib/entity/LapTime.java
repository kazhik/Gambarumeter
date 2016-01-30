package net.kazhik.gambarumeterlib.entity;

/**
 * Created by kazhik on 14/12/01.
 */
public class LapTime extends SplitTimeStepCount {
    private long laptime;
    public LapTime(long timestamp, float distance, long laptime, int stepCount) {
        super(timestamp, distance, stepCount);
        this.laptime = laptime;
    }

    public long getLaptime() {
        return laptime;
    }

    public void setLaptime(long laptime) {
        this.laptime = laptime;
    }


}
