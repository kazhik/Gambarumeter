package net.kazhik.gambarumeterlib.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import net.kazhik.gambarumeterlib.entity.SensorValue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/08.
 */
public class HeartRateTable extends AbstractTable {
    public static final String TABLE_NAME = "gm_heartrate";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "timestamp DATETIME PRIMARY KEY," +
                    "start_time DATETIME," +
                    "heart_rate INTEGER," +
                    "accuracy INTEGER)";
    private static final String TAG = "HeartRateTable";

    public HeartRateTable(Context context) {
        super(context);
    }
    public HeartRateTable(Context context, SQLiteDatabase db) {
        super(context, db);
    }
    public static void init(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);

    }
    public static void upgrade(SQLiteDatabase db) {
        AbstractTable.upgrade(db, TABLE_NAME, CREATE_TABLE);
    }

    public int insert(long timestamp, long startTime, int heartRate, int accuracy) {
        ContentValues values = new ContentValues();

        values.put("timestamp", this.formatDateMsec(timestamp));
        values.put("start_time", this.formatDateMsec(startTime));
        values.put("heart_rate", heartRate);
        values.put("accuracy", accuracy);

        return (int)this.db.insertOrThrow(TABLE_NAME, null, values);

    }
    public List<SensorValue> selectAll(long startTime) {
        return this.selectAll(startTime, 0);
    }

    public List<SensorValue> selectAll(long startTime, int max) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        String[] columns = { "timestamp, heart_rate, accuracy" };
        String selection = "start_time = ?";
        String[] selectionArgs = {this.formatDateMsec(startTime)};
        String sortOrder = "timestamp";
        String limit = (max == 0)? null: Integer.toString(max);

        Cursor cursor = qb.query(this.db, columns, selection, selectionArgs, null,
                null, sortOrder, limit);

        List<SensorValue> dataList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            return dataList;
        }

        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                SensorValue v =
                        new SensorValue(this.parseDate(cursor.getString(0)),
                                cursor.getInt(1),
                                cursor.getInt(2));
                dataList.add(v);
                cursor.moveToNext();
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        cursor.close();

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
