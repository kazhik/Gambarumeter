package net.kazhik.gambarumeter.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by kazhik on 14/11/08.
 */
public class WorkoutTable extends AbstractTable {
    private long startTime;
    private long stopTime;
    private int stepCount;

    public static final String TABLE_NAME = "gm_workout";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "start_time DATETIME PRIMARY KEY," +
                    "stop_time DATETIME," +
                    "step_count INTEGER)";

    public WorkoutTable(Context context) {
        super(context);
    }
    public static void init(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);

    }
    public static void upgrade(SQLiteDatabase db) {
        AbstractTable.upgrade(db, TABLE_NAME, CREATE_TABLE);
    }

    public int insert(long startTime, long stopTime, int stepCount) {
        ContentValues values = new ContentValues();

        values.put("start_time", this.formatDate(startTime));
        values.put("stop_time", this.formatDate(stopTime));
        values.put("step_count", stepCount);

        return (int)this.db.insert(TABLE_NAME, null, values);

    }
    public List<String> selectAll(int max) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        String[] columns = { "start_time" };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = "start_time";
        String limit = (max == 0)? null: Integer.toString(max);

        Cursor cursor = qb.query(this.db, columns, selection, selectionArgs, null,
                null, sortOrder, limit);

        List<String> dataList = new ArrayList<String>();

        if (cursor.getCount() == 0) {
            return dataList;
        }

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            dataList.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();

        return dataList;
    }
    public boolean delete(String startTime) {
        int deleted = this.db.delete(TABLE_NAME, "start_time = " + startTime, null);
        return (deleted > 0);
    }
    public String getTableName(){
        return TABLE_NAME;
    }

}
