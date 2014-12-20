package net.kazhik.gambarumeter.entity;

/**
 * Created by kazhik on 14/12/01.
 */
public class Lap {
    private long timestamp;
    private float distance;

    public Lap(long timestamp, float distance) {
        this.timestamp = timestamp;
        this.distance = distance;
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
}
