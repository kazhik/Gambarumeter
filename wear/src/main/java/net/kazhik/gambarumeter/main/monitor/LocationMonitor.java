package net.kazhik.gambarumeter.main.monitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;

/**
 * Created by kazhik on 10/15/16.
 */

public interface LocationMonitor {
    void init(Context context, LocationSensorValueListener listener);
    void start();
    void stop(long stopTime);
    float getDistance();
    void saveResult(SQLiteDatabase db, long startTime);
    void terminate();

    abstract class LocationBinder extends Binder {
        abstract public LocationMonitor getService();
    }

}
