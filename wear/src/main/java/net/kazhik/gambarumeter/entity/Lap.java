package net.kazhik.gambarumeter.entity;

/**
 * Created by kazhik on 14/12/01.
 */
public class Lap {
    private long timestamp;
    private float distance;
    private long laptime;

    public Lap(long timestamp, float distance) {
        this.timestamp = timestamp;
        this.distance = distance;
    }
    public Lap(long timestamp, float distance, long laptime) {
        this.timestamp = timestamp;
        this.distance = distance;
        this.laptime = laptime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public long getLaptime() {
        return laptime;
    }

    public void setLaptime(long laptime) {
        this.laptime = laptime;
    }

}
