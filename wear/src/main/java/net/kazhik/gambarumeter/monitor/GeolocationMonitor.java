package net.kazhik.gambarumeter.monitor;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/29.
 */
public class GeolocationMonitor implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ResultCallback<Status> {
    private GoogleApiClient googleApiClient;
    private static final int UPDATE_INTERVAL_MS = 5000;
    private static final int FASTEST_INTERVAL_MS = 1000;

    private SensorValueListener listener;
    private LocationRecord record = new LocationRecord();
    private boolean started = false;

    private static final String TAG = "GeolocationMonitor";

    public void init(Context context, SensorValueListener listener) {
        this.listener = listener;

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }
    public void start() {
        if (!this.googleApiClient.isConnected()) {
            return;
        }
        float lapDistance = 1000; // 1km

        this.record.init(lapDistance);
        this.record.addLap(System.currentTimeMillis());
        this.started = true;
    }
    public void stop() {
        this.started = false;

    }

    public void connect() {
        this.googleApiClient.connect();

    }
    public void disconnect() {
        if (this.googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(this.googleApiClient, this);
        }
        this.googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(this.googleApiClient, locationRequest, this)
                .setResultCallback(this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (this.started == false) {
            return;
        }
        long lap = this.record.setCurrentLocation(location);
        if (lap > 0) {
            this.listener.onLap(location.getTime(), lap);
        }
        this.listener.onLocationChanged(location.getTime(), this.record.getDistance());

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Successfully requested location updates");
            }
        } else {
            Log.e(TAG,
                    "Failed in requesting location updates, "
                            + "status code: "
                            + status.getStatusCode()
                            + ", message: "
                            + status.getStatusMessage());
        }
    }
}
