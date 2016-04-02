package net.kazhik.gambarumeter;

/**
 * Created by kazhik on 16/03/21.
 */
public class History {
    private long startTime;
    private boolean exists;

    public History(long startTime, boolean exists) {
        this.startTime = startTime;
        this.exists = exists;
    }

    public boolean exists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
