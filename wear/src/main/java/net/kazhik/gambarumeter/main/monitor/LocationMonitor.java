package net.kazhik.gambarumeter.main.monitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

import net.kazhik.gambarumeterlib.DistanceUtil;
import net.kazhik.gambarumeterlib.LocationRecord;
import net.kazhik.gambarumeterlib.entity.SplitTime;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.LocationTable;
import net.kazhik.gambarumeterlib.storage.SplitTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Sequence of GPS monitoring:
 *
 * MainFragment#initializeSensor
 *   bindService ( creates LocationMonitor)
 * MainFragment#onServiceConnected
 *   LocationMonitor#init
 * LocationMonitor#onLocationChanged
 *
 * Created by kazhik on 14/11/29.
 */
public class LocationMonitor extends Service
        implements LocationListener, GpsStatus.Listener {

    private Context context;

    private GeolocationBinder binder = new GeolocationBinder();
    private LocationSensorValueListener listener;
    private LocationRecord record = new LocationRecord();
    private LocationManager locationManager;
    private DistanceUtil distanceUtil;

    private static final int UPDATE_INTERVAL_MS = 5000;
    private static final int UPDATE_DISTANCE = 5;
    private static final String TAG = "LocationMonitor";

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public class GeolocationBinder extends Binder {

        public LocationMonitor getService() {
            return LocationMonitor.this;
        }
    }

    public void init(Context context, LocationSensorValueListener listener) {
        this.context = context;
        this.listener = listener;

        this.locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        int checkResult = ContextCompat.checkSelfPermission( context,
                android.Manifest.permission.ACCESS_FINE_LOCATION );
        if ( checkResult != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                UPDATE_INTERVAL_MS, UPDATE_DISTANCE, this);
        if (this.locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER)) {
            this.locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    UPDATE_INTERVAL_MS, UPDATE_DISTANCE, this);
        }
        this.locationManager.addGpsStatusListener(this);

        this.distanceUtil = DistanceUtil.getInstance(context);
    }

    public void start() {

        this.record.init(this.distanceUtil.lapDistance());
        this.record.addLap(System.currentTimeMillis());
    }

    public void stop(long stopTime) {

        this.record.addLap(stopTime);

    }

    public float getDistance() {
        return this.record.getDistance();
    }

    private List<Location> getLocationList() {
        return this.record.getLocationList();
    }

    private List<SplitTime> getSplits() {
        return this.record.getSplits();
    }

    public void saveResult(SQLiteDatabase db, long startTime) {
        LocationTable locTable = new LocationTable(this.context, db);
        for (Location loc : this.getLocationList()) {
            locTable.insert(startTime, loc);
        }

        SplitTable splitTable = new SplitTable(this.context, db);
        for (SplitTime split : this.getSplits()) {
            splitTable.insert(split.getTimestamp(), startTime, split.getDistance());
        }

    }

    public void terminate() {
        Log.d(TAG, "terminate: ");
        if (this.locationManager != null) {
            int checkResult = ContextCompat.checkSelfPermission( this.context,
                    android.Manifest.permission.ACCESS_FINE_LOCATION );
            if ( checkResult != PackageManager.PERMISSION_GRANTED ) {
                return;
            }
            this.locationManager.removeUpdates(this);
            this.locationManager.removeGpsStatusListener(this);
        }
    }

    // LocationListener
    @Override
    public void onLocationChanged(Location location) {
        long lap = this.record.setNewLocation(location);
        if (lap > 0) {
            this.listener.onLap(location.getTime(),
                    this.record.getDistance(),
                    lap);
        }
        this.listener.onLocationChanged(location.getTime(),
                this.record.getDistance(),
                location.getSpeed());

    }

    // LocationListener
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    // LocationListener
    @Override
    public void onProviderEnabled(String provider) {

    }

    // LocationListener
    @Override
    public void onProviderDisabled(String provider) {

    }

    // GpsStatus.Listener
    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
            this.listener.onLocationAvailable();
        }

    }

}
