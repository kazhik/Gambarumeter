package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;
import net.kazhik.gambarumeterlib.storage.SplitTimeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 15/06/13.
 */
public class ChartFragment extends Fragment {

    private LineChart chart;

    private static final String TAG = "ChartFragment";
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.chart_fragment, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.initializeChart();

        long startTime = getArguments().getLong("startTime");

        this.loadData(startTime);
    }
    private void initializeChart() {

        Activity activity = this.getActivity();
        this.chart = (LineChart)activity.findViewById(R.id.chart);
        this.chart.setDescription(null);
        this.chart.getAxisLeft().setValueFormatter(laptimeFormatter);
        this.chart.getAxisRight().setValueFormatter(valueFormatter);

    }
    private void loadData(long startTime) {
        Context context = this.getActivity();


        SplitTimeView splitTimeView = new SplitTimeView(context);
        splitTimeView.openReadonly();
        List<SplitTimeStepCount> splits = splitTimeView.selectAll(startTime);
        splitTimeView.close();

        ArrayList<String> xVals = new ArrayList<>();
        ArrayList<Entry> yHeartRate = new ArrayList<>();
        ArrayList<Entry> ySteps = new ArrayList<>();
        ArrayList<Entry> yDistance = new ArrayList<>();
        ArrayList<Entry> yLap = new ArrayList<>();

        long prevTimestamp = splits.get(0).getTimestamp();
        int prevStepCount = splits.get(0).getStepCount();
        for (int x = 1; x < splits.size(); x++) {
            SplitTimeStepCount data = splits.get(x);
            int distance = (int) (data.getDistance() / 1000); //TODO: mile
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

        LineDataSet hrSet = new LineDataSet(yHeartRate, getString(R.string.heart_rate));
        LineDataSet stepSet = new LineDataSet(ySteps, getString(R.string.stepLabel));
        LineDataSet lapSet = new LineDataSet(yLap, getString(R.string.lap));

//        set1.enableDashedLine(10f, 5f, 0f);
        hrSet.setColor(Color.RED);
        hrSet.setCircleColor(Color.RED);
        hrSet.setLineWidth(1f);
        hrSet.setCircleSize(3f);
        hrSet.setDrawCircleHole(false);
        hrSet.setValueTextSize(9f);
        hrSet.setFillAlpha(65);
        hrSet.setFillColor(Color.RED);

        lapSet.setColor(Color.GREEN);
        lapSet.setCircleColor(Color.GREEN);
        lapSet.setLineWidth(1f);
        lapSet.setCircleSize(3f);
        lapSet.setDrawCircleHole(false);
        lapSet.setValueTextSize(9f);
        lapSet.setFillAlpha(65);
        lapSet.setFillColor(Color.GREEN);
        lapSet.setValueFormatter(laptimeFormatter);

        stepSet.setColor(Color.BLUE);
        stepSet.setCircleColor(Color.BLUE);
        stepSet.setLineWidth(1f);
        stepSet.setCircleSize(3f);
        stepSet.setDrawCircleHole(false);
        stepSet.setValueTextSize(9f);
        stepSet.setFillAlpha(65);
        stepSet.setFillColor(Color.BLUE);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(hrSet);
        dataSets.add(stepSet);
        dataSets.add(lapSet);

        LineData data = new LineData(xVals, dataSets);

        // set data
        this.chart.setData(data);

    }

}
