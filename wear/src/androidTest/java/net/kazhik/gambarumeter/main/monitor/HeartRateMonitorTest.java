package net.kazhik.gambarumeter.main.monitor;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import net.kazhik.gambarumeterlib.entity.SensorValue;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by kazhik on 15/01/16.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class HeartRateMonitorTest {
    class TestListener implements HeartRateSensorValueListener {


        @Override
        public void onHeartRateChanged(long timestamp, int rate) {

        }

        @Override
        public void onStepCountChanged(long timestamp, int stepCount) {

        }

        @Override
        public void onRotation(long timestamp) {

        }

        @Override
        public void onBatteryLow() {

        }

        @Override
        public void onBatteryOkay() {

        }

    }
/*
    public void testStoreHeartRate() throws Exception {
        HeartRateMonitor heartRateMonitor = new HeartRateMonitor();

        Context context = getInstrumentation().getContext();
        SensorManager sensorManager =
                (SensorManager)context.getSystemService(Activity.SENSOR_SERVICE);

        heartRateMonitor.init(context, sensorManager, new TestListener());
        heartRateMonitor.start();

        Method storeHeartRate =
                heartRateMonitor.getClass().getDeclaredMethod("storeHeartRate",
                        long.class, float.class);
        storeHeartRate.setAccessible(true);
        Object[] parameters;

        long timestamp;
        float heartRate;
        Calendar cal = Calendar.getInstance();

        cal.set(2015, 0, 17, 23, 22, 8);
        timestamp = cal.getTime().getTime();
        heartRate = 67;

        parameters = new Object[]{timestamp, heartRate};
        storeHeartRate.invoke(heartRateMonitor, parameters);

        cal.set(2015, 0, 17, 23, 22, 9);
        timestamp = cal.getTime().getTime();
        heartRate = 65;

        parameters = new Object[]{timestamp, heartRate};
        storeHeartRate.invoke(heartRateMonitor, parameters);

        cal.set(2015, 0, 17, 23, 22, 33);
        timestamp = cal.getTime().getTime();
        heartRate = 63;

        parameters = new Object[]{timestamp, heartRate};
        storeHeartRate.invoke(heartRateMonitor, parameters);

        List<SensorValue> dataList;
        LinkedBlockingQueue<SensorValue> queue;
        
        Field dataListField = heartRateMonitor.getClass().getDeclaredField("dataList");
        dataListField.setAccessible(true);
        Field queueField = heartRateMonitor.getClass().getDeclaredField("queue");
        queueField.setAccessible(true);
        
        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);
        
        assertEquals(0, dataList.size());
        assertEquals(3, queue.size());

        cal.set(2015, 0, 17, 23, 23, 9);
        timestamp = cal.getTime().getTime();
        heartRate = 63;

        parameters = new Object[]{timestamp, heartRate};
        storeHeartRate.invoke(heartRateMonitor, parameters);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(1, dataList.size());
        assertEquals(1, queue.size());
        assertEquals(65f, dataList.get(0).getValue());
        
    }
*/
    @Test
    public void testOnSensorEvent() throws Exception {
        HeartRateMonitor heartRateMonitor = new HeartRateMonitor();

        Context context = InstrumentationRegistry.getTargetContext();
        SensorManager sensorManager =
                (SensorManager)context.getSystemService(context.SENSOR_SERVICE);

        assertNotNull(sensorManager);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) == null) {
            return;
        }

        heartRateMonitor.init(context, new TestListener());
        heartRateMonitor.start();

        List<SensorValue> dataList;
        LinkedBlockingQueue<SensorValue> queue;
        Field dataListField = heartRateMonitor.getClass().getDeclaredField("dataList");
        dataListField.setAccessible(true);
        Field queueField = heartRateMonitor.getClass().getDeclaredField("queue");
        queueField.setAccessible(true);

        long timestamp;
        float[] sensorValue = new float[3];
        int accuracy;
        Calendar cal = Calendar.getInstance();
        
        // 1st
        cal.set(2015, 0, 17, 23, 22, 8);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue[0] = 67;
        accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;

        heartRateMonitor.onSensorEvent(timestamp, sensorValue, accuracy);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(0, dataList.size());
        assertEquals(1, queue.size());

        // 2nd
        cal.set(2015, 0, 17, 23, 22, 9);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue[0] = 65;

        heartRateMonitor.onSensorEvent(timestamp, sensorValue, accuracy);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(0, dataList.size());
        assertEquals(2, queue.size());

        // 3rd
        cal.set(2015, 0, 17, 23, 22, 33);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue[0] = 63;

        heartRateMonitor.onSensorEvent(timestamp, sensorValue, accuracy);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(0, dataList.size());
        assertEquals(3, queue.size());

        // 4th: sensorValue not changed
        cal.set(2015, 0, 17, 23, 23, 44);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue[0] = 63;

        heartRateMonitor.onSensorEvent(timestamp, sensorValue, accuracy);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(0, dataList.size());
        assertEquals(3, queue.size());

        heartRateMonitor.stop(0);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(1, dataList.size());
        assertEquals(0, queue.size());

        SensorValue average = dataList.get(0);
        assertEquals(65f, average.getValue());

        assertEquals(65, heartRateMonitor.getAverageHeartRate());
    }
    
}
