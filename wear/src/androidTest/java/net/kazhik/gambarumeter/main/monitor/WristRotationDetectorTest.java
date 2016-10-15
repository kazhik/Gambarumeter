package net.kazhik.gambarumeter.main.monitor;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Created by kazhik on 9/10/16.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class WristRotationDetectorTest {
    @Test
    public void testOnSensorEvent() {
        WristRotationDetector wristRotationDetector = new WristRotationDetector();

        int interval = 1000;
        float threshold = 10f;
        long timestamp = System.currentTimeMillis();
        float[] sensorValues = new float[3];
        boolean result;

        wristRotationDetector.setInterval(interval);
        wristRotationDetector.setThreshold(threshold);

        // Normal case
        sensorValues[0] = threshold;
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("1st+", result, is(false));

        timestamp += interval - 1;
        sensorValues[0] = -threshold;
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("1st-", result, is(false));

        sensorValues[0] = threshold;
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("2nd+", result, is(true));

        // long interval
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("1st+", result, is(false));

        sensorValues[0] = -threshold;
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("1st-", result, is(false));

        timestamp += interval + 1;
        sensorValues[0] = threshold;
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("2nd+", result, is(false));

        // opposite move
        timestamp += interval + 1;

        sensorValues[0] = -threshold;
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("1st+", result, is(false));

        sensorValues[0] = threshold;
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("1st-", result, is(false));

        timestamp += interval - 1;
        sensorValues[0] = threshold;
        result = wristRotationDetector.onSensorEvent(timestamp, sensorValues);
        assertThat("2nd+", result, is(false));

    }
}
