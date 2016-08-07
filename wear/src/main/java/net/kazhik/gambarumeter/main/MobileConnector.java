package net.kazhik.gambarumeter.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import net.kazhik.gambarumeterlib.TimeUtil;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 16/06/11.
 */
public class MobileConnector
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        ResultCallback<DataItemBuffer> {
    private Context context;
    private GoogleApiClient googleApiClient;
    private static final String TAG = "MobileConnector";

    public void initialize(Context context) {
        Log.d(TAG, "initialize");

        this.googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        this.context = context;
    }
    public void terminate() {
        if (this.googleApiClient != null) {
            this.googleApiClient.unregisterConnectionCallbacks(this);
        }

    }
    public void connect() {
        Log.d(TAG, "connect");
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
        }

    }
    public void disconnect() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(googleApiClient, this);
            googleApiClient.disconnect();
        }

    }

    private void sendDataToMobile(PutDataRequest putDataReq) {
        Log.d(TAG, "putDataItem:" + putDataReq.getUri().getPath());
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(this.googleApiClient, putDataReq);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                DataItem item = dataItemResult.getDataItem();

                if (item != null) {
                    Log.d(TAG, "putDataItem:" + item.getUri().getPath() +
                            " isSuccess=" + dataItemResult.getStatus().isSuccess());
                } else {
                    Log.d(TAG, "putDataItem: Status=" +
                            dataItemResult.getStatus().toString());
                }
            }
        });
    }
    private void clearItems(final String path) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);

        PendingResult<DataApi.DeleteDataItemsResult> result =
                Wearable.DataApi.deleteDataItems(this.googleApiClient, putDataMapReq.getUri());
        result.setResultCallback(new ResultCallback<DataApi.DeleteDataItemsResult>() {
            @Override
            public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                Log.d(TAG, "deleteDataItems: " + path +
                        " success=" +
                        deleteDataItemsResult.getStatus().isSuccess());
            }
        });
    }
    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "GoogleApiClient onConnected");
        Wearable.DataApi.addListener(this.googleApiClient, this);
        PendingResult<DataItemBuffer> results =
                Wearable.DataApi.getDataItems(this.googleApiClient);
        results.setResultCallback(this);

        this.sync();

    }
    // GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }

    // GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");

    }

    // DataApi.DataListener
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                this.handleDataItem(event.getDataItem());
            }
        }
    }
    private void handleDataItem(DataItem item) {
        String dataPath = item.getUri().getPath();
        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
        switch (dataPath) {
            case "/config":
                Log.d(TAG, "handleDataItem: " + dataPath);
                updateConfig(dataMap);
                break;
            case "/synced":
                Log.d(TAG, "handleDataItem: " + dataPath);
                List<Long> notSynced = this.updateSynced(dataMap);
                if (!notSynced.isEmpty()) {
                    Log.d(TAG, "not synced: " + notSynced.size());
                }
                break;
            default:
                break;
        }

    }

    private void updateConfig(DataMap dataMap) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(this.context);
        SharedPreferences.Editor editor = prefs.edit();

        for (String key: dataMap.keySet()) {
            String value = dataMap.getString(key);
            Log.d(TAG, "updateConfig: " + key + "=" + value);
            editor.putString(key, value);
        }
        editor.apply();

    }
    private List<Long> getNotSynced() {
        WorkoutTable workoutTable = new WorkoutTable(this.context);
        workoutTable.openReadonly();
        List<Long> notSynced = workoutTable.selectNotSynced();
        workoutTable.close();

        return notSynced;
    }

    private void sync() {
        List<Long> notSynced = this.getNotSynced();
        if (!notSynced.isEmpty()) {
            this.sync(notSynced.get(0));
        }
    }

    public void sync(long startTime) {
        Log.d(TAG, "sync: " + TimeUtil.formatDateTime(this.context, startTime));
        DataStorage dataStorage = new DataStorage(this.context);
        DataMap dataMap = dataStorage.load(startTime);
        dataStorage.close();

        DataMap workoutDataMap = dataMap.getDataMap(DataStorage.TBL_WORKOUT);
        if (workoutDataMap == null) {
            Log.d(TAG, "Workout data doesn't exist: " + startTime);
            return;
        }
        Log.d(TAG, "sync: " + workoutDataMap.toString());

        final PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sync");

        // put data on datamap
        Asset asset = Asset.createFromBytes(dataMap.toByteArray());
        DataMap sendData = putDataMapReq.getDataMap();
        sendData.putAsset("data", asset);

        byte[] byteArray = sendData.getAsset("data").getData();
        if (byteArray == null || byteArray.length == 0) {
            Log.d(TAG, "sync: no asset: " + sendData.toString());
            return;
        }
        byte[] assetdata = putDataMapReq.getDataMap().getAsset("data").getData();
        DataMap dMap = DataMap.fromByteArray(assetdata);
        Log.d(TAG, "sync: send asset: " + dMap.toString());

        this.sendDataToMobile(putDataMapReq.asPutDataRequest());

    }
    private List<Long> updateSynced(DataMap dataMap) {
        List<Long> notSynced = new ArrayList<>();
        WorkoutTable workoutTable = new WorkoutTable(this.context);
        workoutTable.openWritable();
        for (String startTimeStr: dataMap.keySet()) {
            long startTime = Long.parseLong(startTimeStr);
            if (dataMap.getBoolean(startTimeStr) == true) {
                boolean ret = workoutTable.updateSynced(startTime);
                Log.d(TAG, "updateSynced: " +
                        TimeUtil.formatDateTime(this.context, startTime) + " synced=" + ret);
            } else {
                notSynced.add(startTime);
                Log.d(TAG, "updateSynced: " +
                        TimeUtil.formatDateTime(this.context, startTime) + " not synced");
            }
        }
        workoutTable.close();

        return notSynced;
    }
    @Override
    public void onResult(DataItemBuffer dataItems) {
        for (DataItem dataItem : dataItems) {
            this.handleDataItem(dataItem);
        }
        dataItems.release();
    }

}
