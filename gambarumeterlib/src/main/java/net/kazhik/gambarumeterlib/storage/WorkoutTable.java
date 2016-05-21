package net.kazhik.gambarumeterlib.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.kazhik.gambarumeterlib.entity.WorkoutInfo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/08.
 */
public class WorkoutTable extends AbstractTable {
    public static final String TABLE_NAME = "gm_workout";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "start_time DATETIME PRIMARY KEY," +
                    "stop_time DATETIME," +
                    "step_count INTEGER," +
                    "heart_rate INTEGER," +
                    "distance REAL," +
                    "synced INTEGER," +
                    "deleted INTEGER DEFAULT(0)" +
                    ")";
    private static final String TAG = "WorkoutTable";

    public WorkoutTable(Context context) {
        super(context);
    }
    public WorkoutTable(Context context, SQLiteDatabase db) {
        super(context, db);
    }
    public static void init(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);

    }
    public static void upgrade(SQLiteDatabase db) {
        AbstractTable.upgrade(db, TABLE_NAME, CREATE_TABLE);
    }

    public int insert(long startTime, long stopTime, int stepCount,
                      float distance, int heartRate) {
        ContentValues values = new ContentValues();

        values.put("start_time", this.formatDateMsec(startTime));
        values.put("stop_time", this.formatDateMsec(stopTime));
        values.put("step_count", stepCount);
        values.put("distance", distance);
        values.put("heart_rate", heartRate);
        values.put("synced", 0);
        values.put("deleted", 0);

        return (int)this.db.insertOrThrow(TABLE_NAME, null, values);

    }
    public List<Long> selectNotSynced() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        String[] columns =
                { "start_time" };
        String selection = "(synced = 0 or synced is null) and deleted = 0";
        String[] selectionArgs = null;
        String sortOrder = "start_time desc";
        String limit = null;

        Cursor cursor = qb.query(this.db, columns, selection, selectionArgs, null,
                null, sortOrder, limit);

        List<Long> dataList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            return dataList;
        }

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dataList.add(this.parseDate(cursor.getString(0)));
                cursor.moveToNext();
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            cursor.close();
        }

        return dataList;

    }
    public List<Long> selectAllStartTime() {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        String[] columns =
                { "start_time" };
        String selection = "deleted = 0";
        String[] selectionArgs = null;
        String sortOrder = "start_time desc";
        String limit = null;

        Cursor cursor = qb.query(this.db, columns, selection, selectionArgs, null,
                null, sortOrder, limit);

        List<Long> dataList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            return dataList;
        }

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                dataList.add(this.parseDate(cursor.getString(0)));
                cursor.moveToNext();
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            cursor.close();
        }

        return dataList;

    }

    public WorkoutInfo select(long startTime) {
        List<WorkoutInfo> workoutInfos = this.select(startTime, 0);
        if (workoutInfos.isEmpty()) {
            throw new SQLiteException(startTime + " not found");
        }
        return workoutInfos.get(0);
    }
    public boolean exists(long startTime) {
        List<WorkoutInfo> workoutInfos = this.select(startTime, 0);

        return !workoutInfos.isEmpty();
    }
    private List<WorkoutInfo> select(String selection, String[] selectionArgs,
                                     String sortOrder, String limit) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        String[] columns =
                { "start_time", "stop_time", "step_count", "distance", "heart_rate" };

        Cursor cursor = qb.query(this.db, columns, selection, selectionArgs, null,
                null, sortOrder, limit);

        List<WorkoutInfo> dataList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            return dataList;
        }

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                WorkoutInfo workout = new WorkoutInfo(
                        this.parseDate(cursor.getString(0)),
                        this.parseDate(cursor.getString(1)),
                        cursor.getInt(2),
                        cursor.getFloat(3),
                        cursor.getInt(4));
                dataList.add(workout);
                cursor.moveToNext();
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            cursor.close();
        }

        return dataList;
    }
    public List<WorkoutInfo> selectAll() {
        return this.select(0, 0);
    }
    public List<WorkoutInfo> selectAll(int max) {
        return this.select(0, max);
    }
    public List<WorkoutInfo> selectMonth(int monthshift) {
        String selection;
        String[] selectionArgs;
        selection = "start_time >= datetime('now', 'start of month', ?) " +
                "AND start_time < datetime('now', 'start of month', ?)";
        selectionArgs =
                new String[]{String.valueOf(monthshift) + " months",
                        String.valueOf(monthshift + 1) + " months"};
        String sortOrder = "start_time desc";
        String limit = null;

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        String[] columns =
                { "start_time", "stop_time", "step_count", "distance", "heart_rate" };

        Cursor cursor = qb.query(this.db, columns, selection, selectionArgs, null,
                null, sortOrder, limit);

        List<WorkoutInfo> dataList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            return dataList;
        }

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                WorkoutInfo workout = new WorkoutInfo(
                        this.parseDate(cursor.getString(0)),
                        this.parseDate(cursor.getString(1)),
                        cursor.getInt(2),
                        cursor.getFloat(3),
                        cursor.getInt(4));
                dataList.add(workout);
                cursor.moveToNext();
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            cursor.close();
        }

        return dataList;
    }

    private List<WorkoutInfo> select(long startTime, int max) {
        String selection;
        String[] selectionArgs;
        if (startTime == 0) {
            selection = "deleted = ?";
            selectionArgs = new String[]{"0"};
        } else {
            selection = "start_time = ? and deleted = ?";
            selectionArgs = new String[]{this.formatDateMsec(startTime), "0"};
        }
        String sortOrder = "start_time desc";
        String limit = (max == 0)? null: Integer.toString(max);

        return select(selection, selectionArgs, sortOrder, limit);
    }
    public boolean delete(long startTime) {
        String where = "start_time = ?";
        String[] whereArgs = {this.formatDateMsec(startTime)};

        ContentValues values = new ContentValues();
        values.put("deleted", 1);

        int deleted = this.db.update(TABLE_NAME, values, where, whereArgs);
        return (deleted > 0);
    }
    public boolean clean(long startTime) {
        String where = "start_time <= ? and synced = 1";
        String[] whereArgs = {this.formatDateMsec(startTime)};
        int deleted = this.db.delete(TABLE_NAME, where, whereArgs);
        return (deleted > 0);
    }
    public boolean updateSynced(long startTime) {
        String where = "start_time = ?";
        String[] whereArgs = {this.formatDateMsec(startTime)};

        ContentValues values = new ContentValues();

        values.put("synced", 1);

        int updated = this.db.update(TABLE_NAME, values, where, whereArgs);


        return (updated > 0);

    }
    public boolean initializeSynced() {
        String where = null;
        String[] whereArgs = null;

        ContentValues values = new ContentValues();

        values.put("synced", 0);

        int updated = this.db.update(TABLE_NAME, values, where, whereArgs);


        return (updated > 0);

    }
    public String getTableName(){
        return TABLE_NAME;
    }

}
