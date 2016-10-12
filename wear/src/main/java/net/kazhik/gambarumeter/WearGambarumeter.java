package net.kazhik.gambarumeter;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import net.kazhik.gambarumeter.pager.PagerAdapter;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.HeartRateTable;
import net.kazhik.gambarumeterlib.storage.LocationTable;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.util.Calendar;

public class WearGambarumeter extends Activity {

    private static final String TAG = "WearGambarumeter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.pager);

        this.initializeDatabase();
        this.cleanDatabase();

        GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new PagerAdapter(this, this.getFragmentManager());
        pager.setOnPageChangeListener(pagerAdapter);
        pager.setAdapter(pagerAdapter);

    }

    private void cleanDatabase() {
        // Clean old data
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        DataStorage storage = new DataStorage(this);
        storage.clean(cal.getTimeInMillis());
        storage.close();

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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        super.onNewIntent(intent);
    }
}
