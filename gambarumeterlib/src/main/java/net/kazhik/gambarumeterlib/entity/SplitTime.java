package net.kazhik.gambarumeterlib.entity;

/**
 * Created by kazhik on 14/12/01.
 */
public class SplitTime {
    private long timestamp;
    private float distance;

    public SplitTime(long timestamp, float distance) {
        this.timestamp = timestamp;
        this.distance = distance;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public SplitTime setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public float getDistance() {
        return distance;
    }

    public SplitTime setDistance(float distance) {
        this.distance = distance;
        return this;
    }

}
