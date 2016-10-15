package net.kazhik.gambarumeter.main.monitor;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import net.kazhik.gambarumeterlib.DistanceUtil;
import net.kazhik.gambarumeterlib.LocationRecord;
import net.kazhik.gambarumeterlib.entity.SplitTime;
import net.kazhik.gambarumeterlib.storage.LocationTable;
import net.kazhik.gambarumeterlib.storage.SplitTable;

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
public class GeolocationMonitorImpl extends Service
        implements LocationMonitor,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GpsStatus.Listener, ResultCallback<Status> {

    private Context context;
    private GoogleApiClient googleApiClient;

    private LocationSensorValueListener listener;
    private LocationRecord record = new LocationRecord();
    private LocationManager locationManager;
    private DistanceUtil distanceUtil;

    private LocationBinder binder = new LocationBinder() {
        @Override
        public LocationMonitor getService() {
            return GeolocationMonitorImpl.this;
        }
    };

    private static final int UPDATE_INTERVAL_MS = 5000;
    private static final int FASTEST_INTERVAL_MS = 3000;
    private static final String TAG = "GeolocationMonitor";

    @Override
    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    // LocationMonitor
    @Override
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

        int checkResult = ContextCompat.checkSelfPermission( this.context,
                Manifest.permission.ACCESS_FINE_LOCATION );
        if ( checkResult != PackageManager.PERMISSION_GRANTED ) {
            return;
        }
        this.locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.locationManager.addGpsStatusListener(this);

        this.distanceUtil = DistanceUtil.getInstance(context);
    }

    // LocationMonitor
    @Override
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

    // LocationMonitor
    @Override
    public void stop(long stopTime) {
        this.record.addLap(stopTime);

    }

    // LocationMonitor
    @Override
    public float getDistance() {
        return this.record.getDistance();
    }

    private List<Location> getLocationList() {
        return this.record.getLocationList();
    }

    private List<SplitTime> getSplits() {
        return this.record.getSplits();
    }

    // LocationMonitor
    @Override
    public void saveResult(SQLiteDatabase db, long startTime) {
        LocationTable locTable = new LocationTable(this.context, db);
        for (Location loc : this.getLocationList()) {
            Log.d(TAG, "saveResult:" + loc.getTime());
            locTable.insert(startTime, loc);
        }

        SplitTable splitTable = new SplitTable(this.context, db);
        for (SplitTime split : this.getSplits()) {
            splitTable.insert(split.getTimestamp(), startTime, split.getDistance());
        }

    }

    // LocationMonitor
    @Override
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

        int checkResult = ContextCompat.checkSelfPermission( this.context,
                android.Manifest.permission.ACCESS_FINE_LOCATION );
        if ( checkResult != PackageManager.PERMISSION_GRANTED ) {
            return;
        }

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
    public void onResult(@NonNull Status status) {
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
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
