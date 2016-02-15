package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.DistanceUtil;
import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 16/02/07.
 */
public class ChartView implements DetailView {
    private LineChart chart;
    private Context context;

    private static final String TAG = "ChartView";
    private static final ValueFormatter valueFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            return String.valueOf((int)value);
        }
    };
    private static final ValueFormatter laptimeFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            // convert seconds -> min:sec

            String laptimeStr = String.format("%d:%02d",
                    (int)value / 60, (int)value % 60);

            return laptimeStr;
        }
    };
    @Override
    public void setContext(Context context) {
        this.context = context;
    }
    @Override
    public void setRootView(View root) {
        this.chart = (LineChart)root.findViewById(R.id.chart);
        this.chart.setDescription(null);
        this.chart.getAxisLeft().setValueFormatter(laptimeFormatter);
        this.chart.getAxisRight().setValueFormatter(valueFormatter);

    }
    public void load(List<SplitTimeStepCount> splits) {
        if (splits.isEmpty()) {
            Toast.makeText(this.context, context.getString(R.string.nodata),
                    Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yHeartRate = new ArrayList<>();
        ArrayList<Entry> ySteps = new ArrayList<>();
        ArrayList<Entry> yDistance = new ArrayList<>();
        ArrayList<Entry> yLap = new ArrayList<>();

        DistanceUtil distanceUtil = DistanceUtil.getInstance(this.context);

        long prevTimestamp = splits.get(0).getTimestamp();
        int prevStepCount = splits.get(0).getStepCount();
        for (int x = 1; x < splits.size(); x++) {
            SplitTimeStepCount data = splits.get(x);

            int distance = (int)distanceUtil.convertMeter(data.getDistance());
            xVals.add(String.valueOf(distance));
            if (data.getStepCount() > 0) {
                ySteps.add(new Entry(data.getStepCount() - prevStepCount, x));
            }
            int laptime = (int) ((data.getTimestamp() - prevTimestamp) / 1000);
            yLap.add(new Entry(laptime, x));

            prevStepCount = data.getStepCount();
            prevTimestamp = data.getTimestamp();
            /*
            xVals.add(DateUtils.formatDateTime(context,
                    data.getTimestamp() - prevTimestamp,
                    DateUtils.FORMAT_SHOW_TIME));
            if (data.getDistance() > 0) {
                yDistance.add(new Entry(data.getDistance(), x));
            }
            */
        }

        LineDataSet hrSet = this.getDefaultLineDataSet(yHeartRate,
                this.context.getString(R.string.heart_rate),
                Color.RED);

        LineDataSet lapSet = this.getDefaultLineDataSet(yLap,
                this.context.getString(R.string.lap),
                Color.GREEN);
        lapSet.setValueFormatter(laptimeFormatter);

        LineDataSet stepSet = this.getDefaultLineDataSet(ySteps,
                this.context.getString(R.string.stepLabel),
                Color.BLUE);

//        set1.enableDashedLine(10f, 5f, 0f);
        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(hrSet);
        dataSets.add(stepSet);
        dataSets.add(lapSet);

        LineData data = new LineData(xVals, dataSets);

        // set data
        this.chart.setData(data);


    }
    private LineDataSet getDefaultLineDataSet(List<Entry> yVal,
                                              String label,
                                              int color) {
        LineDataSet lineDataSet = new LineDataSet(yVal, label);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleSize(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setValueTextSize(9f);
        lineDataSet.setFillAlpha(65);

        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setFillColor(color);

        return lineDataSet;
    }

    @Override
    public void onCreate(Bundle savedInstance) {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }
}
