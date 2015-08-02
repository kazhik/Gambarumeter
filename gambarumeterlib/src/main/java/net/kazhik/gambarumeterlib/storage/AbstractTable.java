package net.kazhik.gambarumeterlib.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class AbstractTable {

    private DbTblHelper dbHelper;
    protected SQLiteDatabase db;
    protected final Context context;
    private SimpleDateFormat sdfMsec =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    private SimpleDateFormat sdfSec =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public AbstractTable(Context context) {
        this.context = context;
    }

    public AbstractTable open(boolean readOnly) throws SQLException {
        this.dbHelper = new DbTblHelper(this.context);
        if (readOnly) {
            this.db = this.dbHelper.getReadableDatabase();
        } else {
            this.db = this.dbHelper.getWritableDatabase();
        }
        return this;
    }

    public void close() {
        this.dbHelper.close();
    }

    public static void upgrade(SQLiteDatabase db, String tableName, String createSql) {
        String sql = createSql.replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS");
        db.execSQL(sql);
        String tmpTable = "temp_" + tableName;
        List<String> colList = AbstractTable.getColumns(db, tableName);
        db.execSQL("ALTER TABLE " + tableName + " RENAME TO " + tmpTable);
        db.execSQL(createSql);

        colList.retainAll(AbstractTable.getColumns(db, tableName));

        String cols = joinString(colList, ",");
        sql = String.format("INSERT INTO %s (%s) SELECT %s FROM %s",
                tableName, cols, cols, tmpTable);
        db.execSQL(sql);

        db.execSQL("DROP TABLE " + tmpTable);

    }
    private static String joinString(List<String> list, String delim) {
        StringBuilder buf = new StringBuilder();
        int num = list.size();
        for (int i = 0; i < num; i++) {
            if (i != 0)
                buf.append(delim);
            buf.append((String) list.get(i));
        }
        return buf.toString();
    }
    public static List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> ar = null;
        Cursor c = null;
        try {
            c = db.rawQuery("select * from " + tableName + " limit 1", null);
            if (c != null) {
                ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
            }
        } catch (Exception e) {
            Log.v(tableName, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (c != null)
                c.close();
        }
        return ar;
    }

    public String formatDateMsec(long timestamp) {

        return this.sdfMsec.format(new Date(timestamp));
    }
    public String formatDateSec(long timestamp) {

        return this.sdfSec.format(new Date(timestamp));
    }
    public long parseDate(String datetime) throws ParseException {

        long parsed;
        try {
            parsed = this.sdfMsec.parse(datetime).getTime();
        } catch (ParseException e) {
            parsed = this.sdfSec.parse(datetime).getTime();
        }

        return parsed;
    }

    public abstract String getTableName();

    public boolean deleteAll() {
        int deleted = this.db.delete(this.getTableName(), null, null);
        return (deleted > 0);
    }
}
