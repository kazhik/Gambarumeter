package net.kazhik.gambarumeterlib.storage;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.wearable.DataMap;

public class DataStorage {
    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            WorkoutTable.init(db);
            HeartRateTable.init(db);
            LocationTable.init(db);
            SplitTable.init(db);
            StepCountTable.init(db);
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion,
                                int newVersion) {
            // super.onDowngrade(db, oldVersion, newVersion);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // http://stackoverflow.com/questions/3505900/sqliteopenhelper-onupgrade-confusion-android
            db.beginTransaction();
            try {
                WorkoutTable.upgrade(db);
                HeartRateTable.upgrade(db);
                LocationTable.upgrade(db);
                SplitTable.upgrade(db);
                StepCountTable.upgrade(db);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                db.endTransaction();
            }

        }
    }

    public static final String TBL_WORKOUT = "Workout";
    public static final String TBL_STEPCOUNT = "StepCount";
    public static final String TBL_HEARTRATE = "HeartRate";
    public static final String TBL_LOCATION = "Location";
    public static final String TBL_SPLITTIME = "SplitTime";

    public static final String COL_START_TIME = "start_time";
    public static final String COL_STOP_TIME = "stop_time";
    public static final String COL_TIMESTAMP = "timestamp";
    public static final String COL_ACCURACY = "accuracy";

    public static final String COL_STEP_COUNT = "step_count";

    public static final String COL_HEART_RATE = "heart_rate";

    public static final String COL_LATITUDE = "latitude";
    public static final String COL_LONGITUDE = "longitude";
    public static final String COL_ALTITUDE = "altitude";
    public static final String COL_DISTANCE = "distance";


    public static final String DATABASE_NAME = "gambarumeter.sqlite";
    public static final int DATABASE_VERSION = 8;

    private static final String TAG = "DataStorage";

    private DatabaseHelper DBHelper;
    private Context context;

    /**
     * Constructor
     *
     * @param ctx
     */
    public DataStorage(Context ctx) {
        this.context = ctx;
        this.DBHelper = new DatabaseHelper(ctx);
    }

    /**
     * open the db
     *
     * @return this
     * @throws SQLException return type: DBAdapter
     */
    public SQLiteDatabase open() throws SQLException {
        return this.DBHelper.getWritableDatabase();
    }

    /**
     * close the db return type: void
     */
    public void close() {
        this.DBHelper.close();
    }

    public void save(DataMap dataMap) {

        DataMap workoutDataMap = dataMap.getDataMap(DataStorage.TBL_WORKOUT);
        long startTime = workoutDataMap.getLong(DataStorage.COL_START_TIME);


        SQLiteDatabase db = this.open();
        db.beginTransaction();
        try {
            WorkoutTable workoutTable = new WorkoutTable(this.context, db);
            workoutTable.insert(
                    startTime,
                    workoutDataMap.getLong(DataStorage.COL_STOP_TIME),
                    workoutDataMap.getInt(DataStorage.COL_STEP_COUNT),
                    workoutDataMap.getFloat(DataStorage.COL_DISTANCE),
                    workoutDataMap.getInt(DataStorage.COL_HEART_RATE));


            if (dataMap.containsKey(DataStorage.TBL_HEARTRATE)) {
                HeartRateTable heartRateTable = new HeartRateTable(this.context, db);
                for (DataMap heartRateDataMap:
                        dataMap.getDataMapArrayList(DataStorage.TBL_HEARTRATE)) {
                    heartRateTable.insert(
                            heartRateDataMap.getLong(DataStorage.COL_TIMESTAMP),
                            startTime,
                            heartRateDataMap.getInt(DataStorage.COL_HEART_RATE),
                            heartRateDataMap.getInt(DataStorage.COL_ACCURACY));
                }
            }

            if (dataMap.containsKey(DataStorage.TBL_LOCATION)) {
                LocationTable locationTable = new LocationTable(this.context, db);
                for (DataMap locationDataMap:
                        dataMap.getDataMapArrayList(DataStorage.TBL_LOCATION)) {
                    Location loc = new Location("");
                    loc.setLatitude(locationDataMap.getDouble(DataStorage.COL_LATITUDE));
                    loc.setLongitude(locationDataMap.getDouble(DataStorage.COL_LONGITUDE));
                    loc.setAltitude(locationDataMap.getDouble(DataStorage.COL_ALTITUDE));
                    loc.setAccuracy(locationDataMap.getFloat(DataStorage.COL_ACCURACY));
                    loc.setTime(locationDataMap.getLong(DataStorage.COL_TIMESTAMP));

                    locationTable.insert(startTime, loc);
                }
            }

            if (dataMap.containsKey(DataStorage.TBL_SPLITTIME)) {
                SplitTable splitTable = new SplitTable(this.context, db);
                for (DataMap splitDataMap:
                        dataMap.getDataMapArrayList(DataStorage.TBL_SPLITTIME)) {
                    splitTable.insert(
                            splitDataMap.getLong(DataStorage.COL_TIMESTAMP),
                            startTime,
                            splitDataMap.getFloat(DataStorage.COL_DISTANCE));
                }
            }

            StepCountTable stepCountTable = new StepCountTable(this.context, db);
            for (DataMap stepCountDataMap:
                    dataMap.getDataMapArrayList(DataStorage.TBL_STEPCOUNT)) {
                stepCountTable.insert(
                        stepCountDataMap.getLong(DataStorage.COL_TIMESTAMP),
                        startTime,
                        (int)stepCountDataMap.getLong(DataStorage.COL_STEP_COUNT));
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        this.close();


    }
    public void delete(long startTime) {

        SQLiteDatabase db = this.open();
        db.beginTransaction();
        try {
            HeartRateTable heartRateTable = new HeartRateTable(this.context, db);
            heartRateTable.delete(startTime);

            LocationTable locationTable = new LocationTable(this.context, db);
            locationTable.delete(startTime);

            SplitTable splitTable = new SplitTable(this.context, db);
            splitTable.delete(startTime);

            StepCountTable stepCountTable = new StepCountTable(this.context, db);
            stepCountTable.delete(startTime);

            WorkoutTable workoutTable = new WorkoutTable(this.context, db);
            workoutTable.delete(startTime);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        this.close();

    }

}