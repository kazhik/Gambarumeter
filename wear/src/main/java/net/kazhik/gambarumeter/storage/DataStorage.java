package net.kazhik.gambarumeter.storage;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataStorage {

    public static final String DATABASE_NAME = "gambarumeter.sqlite";
    public static final int DATABASE_VERSION = 6;

    private final Context context;
    private DatabaseHelper DBHelper;
    private static final String TAG = "DataStorage";

    /**
     * Constructor
     *
     * @param ctx
     */
    public DataStorage(Context ctx) {
        this.context = ctx;
        this.DBHelper = new DatabaseHelper(this.context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private Context context;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            WorkoutTable.init(db);
            HeartRateTable.init(db);
            LocationTable.init(db);
            LapTable.init(db);
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
                LapTable.upgrade(db);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            } finally {
                db.endTransaction();
            }

        }
    }

    /**
     * open the db
     *
     * @return this
     * @throws SQLException return type: DBAdapter
     */
    public DataStorage open() throws SQLException {
        this.DBHelper.getWritableDatabase();
        return this;
    }

    /**
     * close the db return type: void
     */
    public void close() {
        this.DBHelper.close();
    }

}