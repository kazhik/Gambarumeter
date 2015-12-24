package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import net.kazhik.gambarumeterlib.entity.SensorValue;
import net.kazhik.gambarumeterlib.entity.SplitTimeStepCount;
import net.kazhik.gambarumeterlib.storage.HeartRateTable;
import net.kazhik.gambarumeterlib.storage.LocationTable;
import net.kazhik.gambarumeterlib.storage.SplitTimeView;
import net.kazhik.gambarumeterlib.storage.StepCountTable;

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
        this.chart.getAxisLeft().setValueFormatter(valueFormatter);
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


            /*
        for (int x = 0; x < hr.size(); x++) {
            HeartRate data = hr.get(x);
            Log.d(TAG, data.getTimestamp() + "/" + data.getHeartRate() + "/" + data.getHrv());
            long timestamp = hr.get(x).getTimestamp();
            xVals.add(DateUtils.formatDateTime(activity,
                    timestamp,
                    DateUtils.FORMAT_SHOW_TIME));
            if (data.getHeartRate() > 0) {
                yHeartRate.add(new Entry(data.getHeartRate(), x));
            }
            if (data.getHrv() > 0) {
                yHrv.add(new Entry(data.getHrv(), x));
            }
        }
            */

        LineDataSet hrSet = new LineDataSet(yHeartRate, getString(R.string.heart_rate));
        LineDataSet stepSet = new LineDataSet(ySteps, getString(R.string.steps));
        LineDataSet distanceSet = new LineDataSet(yDistance, getString(R.string.distance));

//        set1.enableDashedLine(10f, 5f, 0f);
        hrSet.setColor(Color.RED);
        hrSet.setCircleColor(Color.RED);
        hrSet.setLineWidth(1f);
        hrSet.setCircleSize(3f);
        hrSet.setDrawCircleHole(false);
        hrSet.setValueTextSize(9f);
        hrSet.setFillAlpha(65);
        hrSet.setFillColor(Color.RED);
        hrSet.setValueFormatter(valueFormatter);

        distanceSet.setColor(Color.GREEN);
        distanceSet.setCircleColor(Color.GREEN);
        distanceSet.setLineWidth(1f);
        distanceSet.setCircleSize(3f);
        distanceSet.setDrawCircleHole(false);
        distanceSet.setValueTextSize(9f);
        distanceSet.setFillAlpha(65);
        distanceSet.setFillColor(Color.GREEN);

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
        dataSets.add(distanceSet);

        LineData data = new LineData(xVals, dataSets);

        // set data
        this.chart.setData(data);

    }

}
