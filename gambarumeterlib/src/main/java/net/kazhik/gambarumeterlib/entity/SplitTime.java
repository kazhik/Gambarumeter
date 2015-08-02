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
