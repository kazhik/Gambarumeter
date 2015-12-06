package net.kazhik.gambarumeter.main.monitor;

import android.location.Location;
import android.test.InstrumentationTestCase;

import net.kazhik.gambarumeterlib.LocationRecord;

/**
 * Created by kazhik on 14/12/02.
 */
public class LocationRecordTest extends InstrumentationTestCase {
    public void test() throws Exception {

        LocationRecord locRecord = new LocationRecord();
        long lap;

        Location loc1 = new Location("test");
        loc1.setLatitude(39.899004);
        loc1.setLongitude(-112.507396);
        loc1.setAccuracy(8);
        loc1.setAltitude(1.0f);
        loc1.setTime(System.currentTimeMillis());

        locRecord.addLap(loc1.getTime());

        lap = locRecord.setNewLocation(loc1);

        assertEquals(0, lap);
        assertEquals(0.0f, locRecord.getDistance());

        Location loc2 = new Location("test");
        loc2.setLatitude(39.89907);
        loc2.setLongitude(-112.500272);
        loc2.setAccuracy(8);
        loc2.setAltitude(62.0f);
        loc2.setTime(loc1.getTime() + (2 * 60 * 1000));
        lap = locRecord.setNewLocation(loc2);
        assertEquals(0, lap);
        assertEquals(613.0d, Math.ceil(locRecord.getDistance()));

        Location loc3 = new Location("test");
        loc3.setLatitude(39.899004);
        loc3.setLongitude(-112.507396);
        loc3.setAccuracy(8);
        loc3.setAltitude(123.0f);
        loc3.setTime(loc1.getTime() + (3 * 60 * 1000));
        lap = locRecord.setNewLocation(loc3);
        assertEquals(180 * 1000, lap);
        assertEquals(1225.0d, Math.ceil(locRecord.getDistance()));
        assertEquals(122d, locRecord.getElevationGain());

    }
}
