package net.kazhik.gambarumeter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.wearable.Wearable;

import net.kazhik.gambarumeterlib.TimeUtil;
import net.kazhik.gambarumeterlib.storage.DataStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 16/06/11.
 */
public class WearConnector
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DataApi.DataListener,
        ResultCallback<DataItemBuffer> {
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "WearConnector";

    public void initialize(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        this.context = context;
    }
    public void terminate() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
        }
    }

    public void connect() {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }
    public void disconnect() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    // ResultCallback<DataItemBuffer>
    @Override
    public void onResult(@NonNull DataItemBuffer dataItems) {
        Log.d(TAG, "onResult");
        for (DataItem dataItem : dataItems) {
            this.handleDataItem(dataItem);
        }
        dataItems.release();

    }
    private void handleDataItem(DataItem item) {
        boolean success;
        String dataPath = item.getUri().getPath();
        DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
        switch (dataPath) {
            case "/newdata":
            case "/database":
                success = this.saveData(dataMap);
                Log.d(TAG, "handleDataItem: " + dataPath + " saved=" + success);
                break;
            case "/sync":
                success = this.saveResyncData(dataMap);
                Log.d(TAG, "handleDataItem: " + dataPath + " saved=" + success);
                break;
            default:
                break;
        }

    }

    private void sendSyncResult(List<History> history) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/synced");

        DataMap dataMap = putDataMapReq.getDataMap();

        for (History data: history) {
            String startTimeStr = String.valueOf(data.getStartTime());
            Log.d(TAG, "sendSyncResult: " +
                    TimeUtil.formatDateTime(this.context, data.getStartTime()) +
                    " exists=" + data.exists());
            dataMap.putBoolean(startTimeStr, data.exists());
        }

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(this.mGoogleApiClient,
                        putDataMapReq.asPutDataRequest());

        Log.d(TAG, "putDataItem: " + putDataMapReq.getUri().getPath());
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "putDataItem done: " +
                        dataItemResult.getDataItem().getUri().getPath() +
                        " isSuccess=" +
                        dataItemResult.getStatus().isSuccess());
            }
        });
    }
    public void sendConfig(String key, String value) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/config");

        DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putString(key, value);

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(this.mGoogleApiClient,
                        putDataMapReq.asPutDataRequest());

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "putDataItem done: " + dataItemResult.getDataItem().getUri());
            }
        });

    }

    private boolean saveData(DataMap dataMap) {

        boolean success;
        DataStorage dataStorage = new DataStorage(this.context);
        success = dataStorage.save(dataMap);
        dataStorage.close();

        if (success) {
            DataMap workoutDataMap = dataMap.getDataMap(DataStorage.TBL_WORKOUT);
            long startTime = workoutDataMap.getLong(DataStorage.COL_START_TIME);
            List<History> hist = new ArrayList<>();
            hist.add(new History(startTime, true));
            this.sendSyncResult(hist);
        }

        return success;
    }

    private boolean saveResyncData(final DataMap dataMap) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Asset resyncAsset = dataMap.getAsset("data");
                InputStream is = Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, resyncAsset).await().getInputStream();
                mGoogleApiClient.disconnect();

                ByteArrayOutputStream buffer;
                try {
                    buffer = new ByteArrayOutputStream();

                    int nRead;
                    byte[] data = new byte[16384];

                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }

                    buffer.flush();
                    DataMap dataMap = DataMap.fromByteArray(buffer.toByteArray());

                    saveData(dataMap);

                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }

            }
        });



        return true;
    }

    private void clearItems(String path) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);

        PendingResult<DataApi.DeleteDataItemsResult> result =
                Wearable.DataApi.deleteDataItems(this.mGoogleApiClient, putDataMapReq.getUri());
        result.setResultCallback(new ResultCallback<DataApi.DeleteDataItemsResult>() {
            @Override
            public void onResult(@NonNull DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                Log.d(TAG, "deleteDataItems: success=" +
                        deleteDataItemsResult.getStatus().isSuccess());
            }
        });
    }
}
