package net.kazhik.gambarumeter;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import net.kazhik.gambarumeter.storage.DataStorage;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.WorkoutTable;

public class Gambarumeter extends Activity
        implements WatchViewStub.OnLayoutInflatedListener {

    private static final String TAG = "Gambarumeter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.stub);

        this.initializeDatabase();

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(this);

    }

    @Override
    public void onLayoutInflated(WatchViewStub watchViewStub) {
        this.initPager();
    }

    private void initPager() {
        Log.d(TAG, "initPager: ");
        PagerAdapter pagerAdapter = new PagerAdapter(this, this.getFragmentManager());

        GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOnPageChangeListener(pagerAdapter);
        pager.setAdapter(pagerAdapter);

    }
    private void initializeDatabase() {
        try {
            DataStorage storage = new DataStorage(this);
            storage.open();
            storage.close();

            WorkoutTable workoutTable = new WorkoutTable(this);
            workoutTable.open(false);
//            workoutTable.deleteAll();
            workoutTable.close();

            HeartRateTable heartRateTable = new HeartRateTable(this);
            heartRateTable.open(false);
//            heartRateTable.deleteAll();
            heartRateTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


}
