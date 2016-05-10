package net.kazhik.gambarumeterlib.storage;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import net.kazhik.gambarumeterlib.entity.LapTime;
import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 15/06/20.
 */
public class SplitTimeDataView extends AbstractTable {
    private static final String TAG = "SplitTimeDataView";

    public SplitTimeDataView(Context context) {
        super(context);
    }

    public List<SplitTimeStepCount> selectAll(long startTime) {
        String query = "select a.timestamp, a.distance, " +
                "(select step_count from gm_stepcount b " +
                " where a.start_time = b.start_time " +
                " and b.timestamp <= a.timestamp " +
                " order by b.timestamp desc limit 1) as stepcount, " +
                "(select heart_rate from gm_heartrate c " +
                " where a.start_time = c.start_time " +
                " and c.timestamp <= a.timestamp " +
                " order by c.timestamp desc limit 1) as heartrate " +
                "from gm_lap a " +
                "where a.start_time = ? " +
                "order by a.timestamp";

        String[] selectionArgs = {this.formatDateMsec(startTime)};

        Cursor cursor = this.db.rawQuery(query, selectionArgs);

        List<SplitTimeStepCount> dataList = new ArrayList<>();

        if (cursor.getCount() == 0) {
            return dataList;
        }

        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                SplitTimeStepCount splitTimeStepCount =
                        new SplitTimeStepCount(this.parseDate(cursor.getString(0)),
                                cursor.getFloat(1), cursor.getInt(2),
                                cursor.getInt(3));
                dataList.add(splitTimeStepCount);
                cursor.moveToNext();
            }
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        cursor.close();

        return dataList;

    }

    public List<LapTime> selectLaps(long startTime) {

        List<SplitTimeStepCount> splits = this.selectAll(startTime);

        // calculate laptimes
        List<LapTime> laptimes = new ArrayList<>();
        long prevTimestamp = 0;
        int prevStepCount = 0;
        for (SplitTimeStepCount split: splits) {
            long currentLap = (split.getTimestamp() - prevTimestamp) / 1000;
            if (prevTimestamp != 0) {

                laptimes.add(new LapTime(split.getTimestamp(),
                        split.getDistance(),
                        currentLap,
                        split.getStepCount() - prevStepCount,
                        split.getHeartRate()));
            }
            prevTimestamp = split.getTimestamp();
            prevStepCount = split.getStepCount();
        }
        return laptimes;

    }


    @Override
    public String getTableName() {
        return null;
    }
}
