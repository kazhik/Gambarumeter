package net.kazhik.gambarumeterlib.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
                    "distance REAL" +
                    ")";
    private static final String TAG = "WorkoutTable";

    public WorkoutTable(Context context) {
        super(context);
    }
    public static void init(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);

    }
    public static void upgrade(SQLiteDatabase db) {
        AbstractTable.upgrade(db, TABLE_NAME, CREATE_TABLE);
    }

    public int insert(long startTime, long stopTime, int stepCount, float distance, int heartRate) {
        ContentValues values = new ContentValues();

        values.put("start_time", this.formatDateMsec(startTime));
        values.put("stop_time", this.formatDateMsec(stopTime));
        values.put("step_count", stepCount);
        values.put("distance", distance);
        values.put("heart_rate", heartRate);

        return (int)this.db.insert(TABLE_NAME, null, values);

    }
    public List<WorkoutInfo> selectAll() {
        return this.selectAll(0);
    }
    public List<WorkoutInfo> selectAll(int max) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        String[] columns = { "start_time", "stop_time", "step_count", "distance", "heart_rate" };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = "start_time desc";
        String limit = (max == 0)? null: Integer.toString(max);

        Cursor cursor = qb.query(this.db, columns, selection, selectionArgs, null,
                null, sortOrder, limit);

        List<WorkoutInfo> dataList = new ArrayList<WorkoutInfo>();

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

    public boolean delete(long startTime) {
        String where = "start_time = ?";
        String[] whereArgs = {this.formatDateMsec(startTime)};
        int deleted = this.db.delete(TABLE_NAME, where, whereArgs);
        if (deleted == 0) {
            whereArgs[0] = this.formatDateSec(startTime);
            deleted = this.db.delete(TABLE_NAME, where, whereArgs);
        }
        return (deleted > 0);
    }
    public String getTableName(){
        return TABLE_NAME;
    }

}
