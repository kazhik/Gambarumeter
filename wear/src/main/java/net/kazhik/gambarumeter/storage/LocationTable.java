package net.kazhik.gambarumeter.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/08.
 */
public class LocationTable extends AbstractTable {
    public static final String TABLE_NAME = "gm_location";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    "timestamp DATETIME PRIMARY KEY," +
                    "start_time DATETIME," +
                    "latitude REAL," +
                    "longitude REAL," +
                    "altitude REAL," +
                    "accuracy REAL)";
    private static final String TAG = "LocationTable";

    public LocationTable(Context context) {
        super(context);
    }
    public static void init(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE);

    }
    public static void upgrade(SQLiteDatabase db) {
        AbstractTable.upgrade(db, TABLE_NAME, CREATE_TABLE);
    }

    public int insert(long timestamp, long startTime,
                      double latitude, double longitude, double altitude, float accuracy) {
        ContentValues values = new ContentValues();

        values.put("timestamp", this.formatDate(timestamp));
        values.put("start_time", this.formatDate(startTime));
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("altitude", altitude);
        values.put("accuracy", accuracy);

        return (int)this.db.insert(TABLE_NAME, null, values);

    }
    public List<Location> selectAll(long startTime, int max) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(TABLE_NAME);

        String[] columns = { "timestamp, latitude, longitude, altitude, accuracy" };
        String selection = "start_time = ?";
        String[] selectionArgs = {this.formatDate(startTime)};
        String sortOrder = "timestamp";
        String limit = (max == 0)? null: Integer.toString(max);

        Cursor cursor = qb.query(this.db, columns, selection, selectionArgs, null,
                null, sortOrder, limit);

        List<Location> dataList = new ArrayList<Location>();

        if (cursor.getCount() == 0) {
            return dataList;
        }

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            Location loc = new Location("");
            loc.setTime(cursor.getLong(0));
            loc.setLatitude(cursor.getDouble(1));
            loc.setLongitude(cursor.getDouble(2));
            loc.setAltitude(cursor.getDouble(3));
            loc.setAccuracy(cursor.getFloat(4));
            dataList.add(loc);
            cursor.moveToNext();
        }
        cursor.close();

        return dataList;
    }
    public boolean delete(long startTime) {
        String where = "start_time = ?";
        String[] whereArgs = {this.formatDate(startTime)};
        int deleted = this.db.delete(TABLE_NAME, where, whereArgs);
        return (deleted > 0);
    }
    public String getTableName(){
        return TABLE_NAME;
    }


}
