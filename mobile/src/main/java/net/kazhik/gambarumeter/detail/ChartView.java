package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeterlib.DistanceUtil;
import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by kazhik on 16/02/07.
 */
public class ChartView implements DetailView {
    private LineChart chart;
    private Context context;

    private static final String TAG = "ChartView";
    private static class LapTimeFormatter implements YAxisValueFormatter, ValueFormatter {
        private String formatTime(float sec) {
            // convert seconds -> min:sec
            return String.format(Locale.getDefault(), "%d:%02d",
                    (int)sec / 60, (int)sec % 60);
        }
        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            return this.formatTime(value);
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return this.formatTime(value);
        }

    }
    @Override
    public void setContext(Context context) {
        this.context = context;
    }
    @Override
    public void setRootView(View root) {
        this.chart = (LineChart)root.findViewById(R.id.chart);
        this.chart.setDescription(null);
        this.chart.getAxisLeft().setValueFormatter(new LapTimeFormatter());
        this.chart.getAxisRight().setValueFormatter(new YAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, YAxis yAxis) {
                return String.valueOf((int)value);
            }
        });

    }
    public void load(List<SplitTimeStepCount> splits) {
        if (splits.isEmpty()) {
            Toast.makeText(this.context, R.string.nodata,
                    Toast.LENGTH_LONG).show();
            return;
        }

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yHeartRate = new ArrayList<>();
        ArrayList<Entry> ySteps = new ArrayList<>();
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
        }

        LineDataSet hrSet = this.getDefaultLineDataSet(yHeartRate,
                this.context.getString(R.string.heart_rate),
                Color.RED);

        LineDataSet lapSet = this.getDefaultLineDataSet(yLap,
                this.context.getString(R.string.lap),
                Color.GREEN);
        lapSet.setValueFormatter(new LapTimeFormatter());

        LineDataSet stepSet = this.getDefaultLineDataSet(ySteps,
                this.context.getString(R.string.stepLabel),
                Color.BLUE);

        List<ILineDataSet> dataSets = new ArrayList<>();
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
        lineDataSet.setCircleRadius(3f);
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
