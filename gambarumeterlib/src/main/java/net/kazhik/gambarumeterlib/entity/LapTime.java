package net.kazhik.gambarumeterlib.entity;

/**
 * Created by kazhik on 14/12/01.
 */
public class LapTime extends SplitTimeStepCount {
    private long laptime;
    private String distanceUnitStr;

    public LapTime(long timestamp, float distance, long laptime, int stepCount,
                   String distanceUnit) {
        super(timestamp, distance, stepCount);
        this.laptime = laptime;
        
        this.distanceUnitStr = distanceUnit;
    }

    public long getLaptime() {
        return laptime;
    }

    public void setLaptime(long laptime) {
        this.laptime = laptime;
    }

    public String getDistanceUnitStr() {
        return distanceUnitStr;
    }

    public void setDistanceUnitStr(String distanceUnitStr) {
        this.distanceUnitStr = distanceUnitStr;
    }


}
