package net.kazhik.gambarumeterlib.entity;

/**
 * Created by kazhik on 14/10/15.
 */
public class SensorValue {
    private long timestamp;
    private float value;
    private int accuracy;

    public SensorValue() {

    }
    public SensorValue(long timestamp, float value, int accuracy) {
        this.accuracy = accuracy;
        this.timestamp = timestamp;
        this.value = value;
    }
    public SensorValue(long timestamp, float value) {
        this.timestamp = timestamp;
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public SensorValue setValue(float value) {
        this.value = value;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public SensorValue setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

}
