package net.kazhik.gambarumeter;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

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


public class MobileGambarumeter extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataItemBuffer>, DataApi.DataListener, android.app.FragmentManager.OnBackStackChangedListener {
    private DrawerLayout mDrawerLayout;
    public ActionBarDrawerToggle mDrawerToggle;
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "MobileGambarumeter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambarumeter);

        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        for (Sensor sensor: sensorList) {
            Log.d(TAG, "Sensor:" + sensor.getName() + "; " + sensor.getType());
        }
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, new MainFragment())
                    .commit();

        }
        this.initializeDatabase();
        this.initializeDrawer();

        Intent intent = getIntent();
        Uri dataUri = intent.getData();
        if (dataUri != null) {
            this.importTcxFile(dataUri.getPath());
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();


    }

    private void importTcxFile(String filepath) {
        ExternalFile externalFile = new ExternalFile();
        externalFile.importTcxFile(this, filepath);

    }

    private void initializeDatabase() {
        try {
            DataStorage storage = new DataStorage(this);
            storage.open();
            storage.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
    private void initializeDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close){
        };

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
                if (f instanceof SettingsFragment) {
                    onBackPressed();
                    return;
                }
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        //Setting the actionbarToggle to drawer layout
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        getFragmentManager().addOnBackStackChangedListener(this);
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            this.getFragmentManager().popBackStack();
            if(this.getFragmentManager().getBackStackEntryCount() == 0) {
                super.onBackPressed();
            }
        }
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
            Wearable.DataApi.removeListener(mGoogleApiClient, this);
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
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                this.handleDataItem(event.getDataItem());
            }
        }

    }

    // ResultCallback<DataItemBuffer>
    @Override
    public void onResult(DataItemBuffer dataItems) {
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
                    TimeUtil.formatDateTime(this, data.getStartTime()) +
                    " exists=" + data.exists());
            dataMap.putBoolean(startTimeStr, data.exists());
        }

        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(this.mGoogleApiClient,
                        putDataMapReq.asPutDataRequest());

        Log.d(TAG, "putDataItem: " + putDataMapReq.getUri().getPath());
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
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
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "putDataItem done: " + dataItemResult.getDataItem().getUri());
            }
        });

    }

    private boolean saveData(DataMap dataMap) {

        boolean success;
        DataStorage dataStorage = new DataStorage(this);
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

                ByteArrayOutputStream buffer = null;
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
                } finally {
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
            public void onResult(DataApi.DeleteDataItemsResult deleteDataItemsResult) {
                Log.d(TAG, "deleteDataItems: success=" +
                        deleteDataItemsResult.getStatus().isSuccess());
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        boolean showHomeAsUp = false;
        Fragment f = getFragmentManager().findFragmentById(R.id.fragment_container);
        if (f instanceof SettingsFragment) {
            Log.d(TAG, "SettingsFragment");
            showHomeAsUp = true;
        } else if (f instanceof DetailFragment) {
            Log.d(TAG, "DetailFragment");
        } else if (f instanceof MainFragment) {
            Log.d(TAG, "MainFragment");
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(showHomeAsUp);
        if (showHomeAsUp) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mDrawerToggle.syncState();
//            mDrawerToggle.setDrawerIndicatorEnabled(true);
        }

    }
}
