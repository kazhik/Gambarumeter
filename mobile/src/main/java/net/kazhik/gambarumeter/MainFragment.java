package net.kazhik.gambarumeter;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.HeartRateTable;
import net.kazhik.gambarumeterlib.storage.LocationTable;
import net.kazhik.gambarumeterlib.storage.SplitTable;
import net.kazhik.gambarumeterlib.storage.StepCountTable;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

/**
 * Created by kazhik on 15/10/14.
 */
public class MainFragment extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        ResultCallback<DataItemBuffer> {
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "MainFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

    }
    @Override
    public void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }
    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.DataApi.addListener(mGoogleApiClient, this);

        PendingResult<DataItemBuffer> results =
                Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(this);

    }

    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {

    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    // DataApi.DataListener
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "onDataChanged");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                this.handleDataItem(event.getDataItem());
            }
        }

    }

    // ResultCallback<DataItemBuffer>
    @Override
    public void onResult(DataItemBuffer dataItems) {
        for (DataItem dataItem : dataItems) {
            this.handleDataItem(dataItem);
        }
        dataItems.release();

    }
    private void handleDataItem(DataItem item) {
        if (item.getUri().getPath().equals("/database")) {
            DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
            Log.d(TAG, "database: " + dataMap.toString());
            this.saveData(dataMap);
        }

    }
    private void saveData(DataMap dataMap) {
        Context context = this.getActivity();

        DataMap workoutDataMap = dataMap.getDataMap(DataStorage.TBL_WORKOUT);
        long startTime = workoutDataMap.getLong(DataStorage.COL_START_TIME);

        WorkoutTable workoutTable = new WorkoutTable(context);
        workoutTable.open(false);
        workoutTable.insert(
                startTime,
                workoutDataMap.getLong(DataStorage.COL_STOP_TIME),
                workoutDataMap.getInt(DataStorage.COL_STEP_COUNT),
                workoutDataMap.getFloat(DataStorage.COL_DISTANCE),
                workoutDataMap.getInt(DataStorage.COL_HEART_RATE));
        workoutTable.close();

        StepCountTable stepCountTable = new StepCountTable(context);
        stepCountTable.open(false);

        for (DataMap stepCountDataMap:
                dataMap.getDataMapArrayList(DataStorage.TBL_STEPCOUNT)) {
            stepCountTable.insert(
                    stepCountDataMap.getLong(DataStorage.COL_TIMESTAMP),
                    startTime,
                    (int)stepCountDataMap.getLong(DataStorage.COL_STEP_COUNT));
        }
        stepCountTable.close();

        if (dataMap.containsKey(DataStorage.TBL_HEARTRATE)) {
            HeartRateTable heartRateTable = new HeartRateTable(context);
            heartRateTable.open(false);
            for (DataMap heartRateDataMap:
                    dataMap.getDataMapArrayList(DataStorage.TBL_HEARTRATE)) {
                heartRateTable.insert(
                        heartRateDataMap.getLong(DataStorage.COL_TIMESTAMP),
                        startTime,
                        heartRateDataMap.getInt(DataStorage.COL_HEART_RATE),
                        heartRateDataMap.getInt(DataStorage.COL_ACCURACY));
            }
            heartRateTable.close();
        }

        if (dataMap.containsKey(DataStorage.TBL_LOCATION)) {
            LocationTable locTable = new LocationTable(context);
            locTable.open(false);
            for (DataMap locationDataMap:
                    dataMap.getDataMapArrayList(DataStorage.TBL_LOCATION)) {
                Location loc = new Location("");
                loc.setLatitude(locationDataMap.getDouble(DataStorage.COL_LATITUDE));
                loc.setLongitude(locationDataMap.getDouble(DataStorage.COL_LONGITUDE));
                loc.setAltitude(locationDataMap.getDouble(DataStorage.COL_ALTITUDE));
                loc.setAccuracy(locationDataMap.getFloat(DataStorage.COL_ACCURACY));
                loc.setTime(locationDataMap.getLong(DataStorage.COL_TIMESTAMP));

                locTable.insert(startTime, loc);
            }
            locTable.close();
        }

        if (dataMap.containsKey(DataStorage.TBL_SPLITTIME)) {
            SplitTable splitTable = new SplitTable(context);
            splitTable.open(false);
            for (DataMap splitDataMap:
                    dataMap.getDataMapArrayList(DataStorage.TBL_SPLITTIME)) {
                splitTable.insert(
                        splitDataMap.getLong(DataStorage.COL_TIMESTAMP),
                        startTime,
                        splitDataMap.getFloat(DataStorage.COL_DISTANCE));
            }
            splitTable.close();

        }

    }

}
