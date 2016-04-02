package net.kazhik.gambarumeter.main.monitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

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
 *   bindService ( creates GeolocationMonitor) 
 * MainFragment#onServiceConnected
 *   GeolocationMonitor#init
 *     GoogleApiClient#connect
 * GeolocationMonitor#onConnected
 *   LocationServices.FusedLocationApi.requestLocationUpdates
 * GeolocationMonitor#onLocationChanged
 *
 * Created by kazhik on 14/11/29.
 */
public class GeolocationMonitor extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GpsStatus.Listener, ResultCallback<Status> {
    
    private Context context;
    private GoogleApiClient googleApiClient;

    private GeolocationBinder binder = new GeolocationBinder();
    private LocationSensorValueListener listener;
    private LocationRecord record = new LocationRecord();
    private LocationManager locationManager;
    private DistanceUtil distanceUtil;

    private static final int UPDATE_INTERVAL_MS = 5000;
    private static final int FASTEST_INTERVAL_MS = 3000;
    private static final String TAG = "GeolocationMonitor";

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    public class GeolocationBinder extends Binder {

        public GeolocationMonitor getService() {
            return GeolocationMonitor.this;
        }
    }

    public void init(Context context, LocationSensorValueListener listener) {
        this.context = context;
        this.listener = listener;

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.googleApiClient.connect();

        this.locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.locationManager.addGpsStatusListener(this);

        this.distanceUtil = DistanceUtil.getInstance(context);
    }
    public void start() {
        if (this.googleApiClient == null) {
            return;
        }
        if (!this.googleApiClient.isConnected()) {
            return;
        }
        this.record.init(this.distanceUtil.lapDistance());
        this.record.addLap(System.currentTimeMillis());
    }
    public void stop() {
        this.record.addLap(System.currentTimeMillis());

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

    public DataMap putData(DataMap dataMap) {
        ArrayList<DataMap> locationMapList = new ArrayList<>();
        for (Location loc: this.getLocationList()) {
            DataMap locMap = new DataMap();
            locMap.putLong(DataStorage.COL_TIMESTAMP, loc.getTime());
            locMap.putDouble(DataStorage.COL_LATITUDE, loc.getLatitude());
            locMap.putDouble(DataStorage.COL_LONGITUDE, loc.getLongitude());
            locMap.putDouble(DataStorage.COL_ALTITUDE, loc.getAltitude());
            locMap.putFloat(DataStorage.COL_ACCURACY, loc.getAccuracy());

            locationMapList.add(locMap);

        }
        dataMap.putDataMapArrayList(DataStorage.TBL_LOCATION, locationMapList);

        ArrayList<DataMap> splitMapList = new ArrayList<>();
        for (SplitTime split: this.getSplits()) {
            DataMap splitMap = new DataMap();
            splitMap.putLong(DataStorage.COL_TIMESTAMP, split.getTimestamp());
            splitMap.putFloat(DataStorage.COL_DISTANCE, split.getDistance());

            splitMapList.add(splitMap);
        }
        dataMap.putDataMapArrayList(DataStorage.TBL_SPLITTIME, splitMapList);

        return dataMap;
    }
    public void saveResult(SQLiteDatabase db, long startTime) {
        LocationTable locTable = new LocationTable(this.context, db);
        for (Location loc: this.getLocationList()) {
            Log.d(TAG, "saveResult:" + loc.getTime());
            locTable.insert(startTime, loc);
        }

        SplitTable splitTable = new SplitTable(this.context, db);
        for (SplitTime split: this.getSplits()) {
            splitTable.insert(split.getTimestamp(), startTime, split.getDistance());
        }

    }
    public void terminate() {
        Log.d(TAG, "terminate: " + this.googleApiClient);
        if (this.googleApiClient == null) {
            return;
        }
        if (this.googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(this.googleApiClient, this);
        }
        this.googleApiClient.disconnect();
        this.locationManager.removeGpsStatusListener(this);
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "GoogleApiClient onConnected");

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(this.googleApiClient, locationRequest, this)
                .setResultCallback(this);

    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "GoogleApiClient onConnectionSuspended");

    }

    // ResultCallback
    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(TAG, "Successfully requested location updates");
        } else {
            Log.e(TAG,
                    "Failed in requesting location updates, "
                            + "status code: "
                            + status.getStatusCode()
                            + ", message: "
                            + status.getStatusMessage());
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

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "connection failed: " + connectionResult.getErrorCode());

    }

    // GpsStatus.Listener
    @Override
    public void onGpsStatusChanged(int event) {
        if (event == GpsStatus.GPS_EVENT_FIRST_FIX) {
            this.listener.onLocationAvailable();
        }

    }


}
