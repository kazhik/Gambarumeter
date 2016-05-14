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
        String query = "select\n" +
                " a1.timestamp,\n" +
                " a1.distance,\n" +
                " b1.step_count, \n" +
                " case \n" +
                "  when c1.timestamp > a2.timestamp then c1.heart_rate\n" +
                "  else null\n" +
                " end as heart_rate\n" +
                "from gm_lap a1\n" +
                "left join gm_lap a2\n" +
                " on a1.start_time = a2.start_time\n" +
                "  and a2.timestamp = (\n" +
                "   select a3.timestamp\n" +
                "   from gm_lap a3\n" +
                "   where a3.timestamp < a1.timestamp\n" +
                "   order by a3.timestamp desc limit 1\n" +
                "  )\n" +
                "left join gm_stepcount b1\n" +
                " on a1.start_time = b1.start_time\n" +
                "  and b1.timestamp = (\n" +
                "   select b2.timestamp\n" +
                "   from gm_stepcount b2\n" +
                "   where b2.timestamp <= a1.timestamp\n" +
                "   order by b2.timestamp desc limit 1\n" +
                "  )\n" +
                "left join gm_heartrate c1\n" +
                " on a1.start_time = c1.start_time\n" +
                "  and c1.timestamp = (\n" +
                "   select c2.timestamp\n" +
                "   from gm_heartrate c2\n" +
                "   where c2.timestamp <= a1.timestamp\n" +
                "   order by c2.timestamp desc limit 1\n" +
                "  )\n" +
                "where a1.start_time = ?\n" +
                "order by a1.timestamp\n";

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
