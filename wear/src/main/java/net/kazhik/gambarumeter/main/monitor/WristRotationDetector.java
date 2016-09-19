package net.kazhik.gambarumeter.main.monitor;

import android.util.Log;

/**
 * Created by kazhik on 15/12/06.
 */
public class WristRotationDetector {
    private float threshold = 15.0f;
    private int interval = 1000;
    private int rotateCount = 0;
    private long prevTimestamp = 0;
    private float[] prevValues = new float[3];
    private static final String TAG = "WristRotationDetector";

    public WristRotationDetector setThreshold(float threshold) {
        this.threshold = threshold;
        return this;
    }
    public WristRotationDetector setInterval(int interval) {
        this.interval = interval;
        return this;
    }
    public boolean onSensorEvent(long timestamp, float[] newValues) {
        if (newValues.length < 3) {
            return false;
        }

        // no value is over threshold
        if (Math.abs(newValues[0]) < this.threshold &&
                Math.abs(newValues[1]) < this.threshold &&
                Math.abs(newValues[2]) < this.threshold) {
            return false;
        }

        Log.d(TAG, "onSensorEvent:" +
                newValues[0] + "/" +
                newValues[1] + "/" +
                newValues[2] + "; " + (timestamp - this.prevTimestamp));

        if (this.prevTimestamp == 0) {
            this.prevTimestamp = timestamp;
            System.arraycopy(newValues, 0, this.prevValues, 0, 3);
            return false;
        }

        if ((timestamp - this.prevTimestamp) > this.interval) {
            this.rotateCount = 0;
        } else {
            for (int i = 0; i < 3; i++) {
                if (Math.abs(newValues[i] - this.prevValues[i]) >= threshold * 2) {
                    if (this.rotateCount == 1) {
                        this.rotateCount = 0;
                        this.prevTimestamp = 0;
                        return true;
                    }
                    this.rotateCount++;
                    break;
                }
            }
        }

        this.prevTimestamp = timestamp;
        System.arraycopy(newValues, 0, this.prevValues, 0, 3);

        return false;
    }
}
