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
        String query = "SELECT\n" +
                " a1.timestamp,\n" +
                " a1.distance,\n" +
                " b1.step_count, \n" +
                " CASE \n" +
                "  WHEN c1.timestamp > a2.timestamp THEN c1.heart_rate\n" +
                "  ELSE null\n" +
                " END AS heart_rate\n" +
                "FROM gm_lap a1\n" +
                "LEFT JOIN gm_lap a2\n" +
                " ON a1.start_time = a2.start_time\n" +
                "  AND a2.timestamp = (\n" +
                "   SELECT a3.timestamp\n" +
                "   FROM gm_lap a3\n" +
                "   WHERE a3.timestamp < a1.timestamp\n" +
                "   ORDER BY a3.timestamp DESC LIMIT 1\n" +
                "  )\n" +
                "LEFT JOIN gm_stepcount b1\n" +
                " ON a1.start_time = b1.start_time\n" +
                "  AND b1.timestamp = (\n" +
                "   SELECT b2.timestamp\n" +
                "   FROM gm_stepcount b2\n" +
                "   WHERE b2.timestamp <= a1.timestamp\n" +
                "   ORDER BY b2.timestamp DESC LIMIT 1\n" +
                "  )\n" +
                "LEFT JOIN gm_heartrate c1\n" +
                " ON a1.start_time = c1.start_time\n" +
                "  AND c1.timestamp = (\n" +
                "   SELECT c2.timestamp\n" +
                "   FROM gm_heartrate c2\n" +
                "   WHERE c2.timestamp <= a1.timestamp\n" +
                "   ORDER BY c2.timestamp DESC LIMIT 1\n" +
                "  )\n" +
                "WHERE a1.start_time = ?\n" +
                "ORDER BY a1.timestamp\n";

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
