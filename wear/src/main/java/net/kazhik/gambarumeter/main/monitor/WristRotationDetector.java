package net.kazhik.gambarumeter.main.monitor;

import android.util.Log;

/**
 * Created by kazhik on 15/12/06.
 */
class WristRotationDetector {
    private float threshold = 15.0f;
    private int interval = 1000;
    private long prevTimestamp = 0;
    private float prevValue = 0;
    private int counter = 0;
    private static final String TAG = "WristRotationDetector";

    WristRotationDetector setThreshold(float threshold) {
        this.threshold = threshold;
        return this;
    }
    WristRotationDetector setInterval(int interval) {
        this.interval = interval;
        return this;
    }
    private float getMaxValue(float[] values) {
        float maxValue;
        maxValue = (Math.abs(values[0]) > Math.abs(values[1]))?
                values[0]: values[1];
        maxValue = (Math.abs(maxValue) > Math.abs(values[2]))?
                maxValue: values[2];
        return maxValue;
    }

    private boolean hasSameSign(float val1, float val2) {
        return (val1 < 0 && val2 < 0) || (val1 >= 0 && val2 >= 0);
    }
    boolean onSensorEvent(long timestamp, float[] newValues) {
        if (newValues.length < 3) {
            return false;
        }

        float maxValue = this.getMaxValue(newValues);

        // no value is over threshold
        if (Math.abs(maxValue) < this.threshold) {
            if (Math.abs(maxValue) > this.threshold / 2) {
                Log.d(TAG, "onSensorEvent(0):" +
                        newValues[0] + "/" +
                        newValues[1] + "/" +
                        newValues[2] + "; " +
                        (timestamp - this.prevTimestamp));
            }
            return false;
        }

        // 1st event
        if ((timestamp - this.prevTimestamp) > this.interval) {
            Log.d(TAG, "onSensorEvent(1):" +
                    newValues[0] + "/" +
                    newValues[1] + "/" +
                    newValues[2] + "; " +
                    (timestamp - this.prevTimestamp));
            this.prevTimestamp = timestamp;
            this.prevValue = maxValue;
            this.counter = 0;
        // 2nd event, opposite move
        } else if (!this.hasSameSign(maxValue, this.prevValue)) {
            this.counter++;
        // 3rd event, not opposite move
        } else if (this.counter > 0) {
            Log.d(TAG, "onSensorEvent:" +
                    newValues[0] + "/" +
                    newValues[1] + "/" +
                    newValues[2] + "; " +
                    (timestamp - this.prevTimestamp));
            this.prevTimestamp = 0;
            return true;
        }


        return false;
    }
}
