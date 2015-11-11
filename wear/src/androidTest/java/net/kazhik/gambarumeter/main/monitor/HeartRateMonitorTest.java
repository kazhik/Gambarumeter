package net.kazhik.gambarumeter.main.monitor;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.test.InstrumentationTestCase;

import net.kazhik.gambarumeterlib.entity.SensorValue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by kazhik on 15/01/16.
 */
public class HeartRateMonitorTest extends InstrumentationTestCase {
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

    public void testOnSensorEvent() throws Exception {
        HeartRateMonitor heartRateMonitor = new HeartRateMonitor();

        Context context = getInstrumentation().getContext();
        SensorManager sensorManager =
                (SensorManager)context.getSystemService(Activity.SENSOR_SERVICE);

        heartRateMonitor.init(context, sensorManager, new TestListener());
        heartRateMonitor.start();

        Method onSensorEvent =
                heartRateMonitor.getClass().getDeclaredMethod("onSensorEvent",
                        long.class, float.class, int.class);
        onSensorEvent.setAccessible(true);
        Object[] parameters;

        List<SensorValue> dataList;
        LinkedBlockingQueue<SensorValue> queue;
        Field dataListField = heartRateMonitor.getClass().getDeclaredField("dataList");
        dataListField.setAccessible(true);
        Field queueField = heartRateMonitor.getClass().getDeclaredField("queue");
        queueField.setAccessible(true);


        long timestamp;
        float sensorValue;
        int accuracy;
        Calendar cal = Calendar.getInstance();
        
        // 1st
        cal.set(2015, 0, 17, 23, 22, 8);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue = 67;
        accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH;

        parameters = new Object[]{timestamp, sensorValue, accuracy};
        onSensorEvent.invoke(heartRateMonitor, parameters);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(0, dataList.size());
        assertEquals(1, queue.size());

        // 2nd
        cal.set(2015, 0, 17, 23, 22, 9);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue = 65;

        parameters = new Object[]{timestamp, sensorValue, accuracy};
        onSensorEvent.invoke(heartRateMonitor, parameters);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(0, dataList.size());
        assertEquals(2, queue.size());

        // 3rd
        cal.set(2015, 0, 17, 23, 22, 33);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue = 63;

        parameters = new Object[]{timestamp, sensorValue, accuracy};
        onSensorEvent.invoke(heartRateMonitor, parameters);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(0, dataList.size());
        assertEquals(3, queue.size());

        // 4th: calculate average
        cal.set(2015, 0, 17, 23, 23, 9);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue = 65;

        parameters = new Object[]{timestamp, sensorValue, accuracy};
        onSensorEvent.invoke(heartRateMonitor, parameters);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(1, dataList.size());
        assertEquals(1, queue.size());
        assertEquals(65f, dataList.get(0).getValue());

        // 5th: sensorValue not changed
        cal.set(2015, 0, 17, 23, 23, 44);
        timestamp = cal.getTime().getTime() * 1000 * 1000;
        sensorValue = 65;

        parameters = new Object[]{timestamp, sensorValue, accuracy};
        onSensorEvent.invoke(heartRateMonitor, parameters);

        dataList = (List<SensorValue>)dataListField.get(heartRateMonitor);
        queue = (LinkedBlockingQueue<SensorValue>)queueField.get(heartRateMonitor);

        assertEquals(1, dataList.size());
        assertEquals(1, queue.size());


    }
    
}
