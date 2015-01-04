package net.kazhik.gambarumeter;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import net.kazhik.gambarumeter.pager.PagerAdapter;
import net.kazhik.gambarumeter.storage.DataStorage;
import net.kazhik.gambarumeter.storage.HeartRateTable;
import net.kazhik.gambarumeter.storage.LocationTable;
import net.kazhik.gambarumeter.storage.WorkoutTable;

public class Gambarumeter extends Activity {

    private static final String TAG = "Gambarumeter";

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.pager);

        this.initializeDatabase();

        GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new PagerAdapter(this, this.getFragmentManager());
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
            workoutTable.close();

            HeartRateTable heartRateTable = new HeartRateTable(this);
            heartRateTable.open(false);
            heartRateTable.close();

            LocationTable locationTable = new LocationTable(this);
            locationTable.open(false);
            locationTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }


}
