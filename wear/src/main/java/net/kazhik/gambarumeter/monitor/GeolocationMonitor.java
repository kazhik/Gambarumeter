package net.kazhik.gambarumeter.monitor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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

import java.util.List;

/**
 * Created by kazhik on 14/11/29.
 */
public class GeolocationMonitor extends Service
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status> {
    private GoogleApiClient googleApiClient;

    private GeolocationBinder binder = new GeolocationBinder();
    private SensorValueListener listener;
    private LocationRecord record = new LocationRecord();
    private boolean started = false;

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
        this.started = true;
    }
    public void stop() {
        this.record.addLap(System.currentTimeMillis());
        this.started = false;

    }
    public float getDistance() {
        return this.record.getDistance();
    }
    public List<Location> getLocationList() {
        return this.record.getLocationList();
    }
    public List<Lap> getLaps() {
        return this.record.getLaps();
    }

    public void terminate() {
        Log.d(TAG, "terminate");
        if (this.googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(this.googleApiClient, this);
        }
        this.googleApiClient.disconnect();
    }

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

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: ");

        if (this.started == false) {
            this.listener.onLocationAvailable();
            return;
        }
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


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "connection failed: " + connectionResult.getErrorCode());

    }

}
