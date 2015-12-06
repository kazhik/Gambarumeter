package net.kazhik.gambarumeter;

import android.app.Activity;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import net.kazhik.gambarumeter.pager.PagerAdapter;
import net.kazhik.gambarumeterlib.storage.DataStorage;
import net.kazhik.gambarumeterlib.storage.HeartRateTable;
import net.kazhik.gambarumeterlib.storage.LocationTable;
import net.kazhik.gambarumeterlib.storage.WorkoutTable;

import java.io.File;
import java.io.IOException;

public class WearGambarumeter extends Activity {

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

        //this.startLogWrite();

    }
    private void startLogWrite() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        try {
            File f = new File(Environment.getExternalStorageDirectory()
                    + "/" + this.getString(R.string.app_name) + ".log");
            f.createNewFile();
            String cmd = "logcat -d -v time -f " + f.getAbsolutePath();
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

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
