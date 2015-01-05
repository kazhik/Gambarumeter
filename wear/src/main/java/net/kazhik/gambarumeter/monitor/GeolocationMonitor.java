package net.kazhik.gambarumeter.monitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.wearable.Wearable;

import net.kazhik.gambarumeter.entity.Lap;
import net.kazhik.gambarumeter.storage.LapTable;
import net.kazhik.gambarumeter.storage.LocationTable;

import java.util.List;

/**
 * Created by kazhik on 14/11/29.
 */
public class GeolocationMonitor extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GpsStatus.Listener, ResultCallback<Status> {
    
    private Context context;
    private GoogleApiClient googleApiClient;

    private GeolocationBinder binder = new GeolocationBinder();
    private SensorValueListener listener;
    private LocationRecord record = new LocationRecord();

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

    public void init(Context context, SensorValueListener listener) {
        this.listener = listener;

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        this.googleApiClient.connect();

        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.addGpsStatusListener(this);

    }
    public void start(float lapDistance) {
        if (this.googleApiClient == null) {
            return;
        }
        if (!this.googleApiClient.isConnected()) {
            return;
        }
        this.record.init(lapDistance);
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
    private List<Lap> getLaps() {
        return this.record.getLaps();
    }

    public void saveResult(long startTime) {
        LocationTable locTable = new LocationTable(this.context);
        locTable.open(false);
        for (Location loc: this.getLocationList()) {
            Log.d(TAG, "saveResult:" + loc.getTime());
            locTable.insert(startTime, loc);
        }
        locTable.close();

        LapTable lapTable = new LapTable(this.context);
        lapTable.open(false);
        for (Lap lap: this.getLaps()) {
            lapTable.insert(lap.getTimestamp(), startTime, lap.getDistance());
        }
        lapTable.close();

    }
    public void terminate() {
        Log.d(TAG, "terminate");
        if (this.googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(this.googleApiClient, this);
        }
        this.googleApiClient.disconnect();
    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

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
        Log.d(TAG, "onConnectionSuspended");

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
        Log.d(TAG, "onLocationChanged: ");

        long lap = this.record.setCurrentLocation(location);
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
