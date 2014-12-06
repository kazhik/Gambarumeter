package net.kazhik.gambarumeter.detail;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;
import net.kazhik.gambarumeter.entity.SensorValue;
import net.kazhik.gambarumeter.storage.HeartRateTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/11/18.
 */
public class HeartRateDetailFragment extends Fragment
        implements View.OnClickListener {

    private class Result {
        private int average;
        private int max;
        private int min;

        public int getAverage() {
            return average;
        }

        public void setAverage(int average) {
            this.average = average;
        }

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public void setMin(int min) {
            this.min = min;
        }
    }

    private long startTime;
    private static final String TAG = "DetailFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.detail, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = this.getActivity();

        ImageButton deleteButton = (ImageButton)activity.findViewById(R.id.delete);
        deleteButton.setOnClickListener(this);

        List<SensorValue> heartRates = new ArrayList<SensorValue>();
        try {

            HeartRateTable heartRateTable = new HeartRateTable(this.getActivity());
            heartRateTable.open(true);
            heartRates = heartRateTable.selectAll(this.startTime, 0);
            heartRateTable.close();

        } catch (SQLException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (heartRates.isEmpty()) {
            return;
        }
        Result result = this.calculate(heartRates);

        this.refreshView(result);
    }
    public void read(long startTime) {
        this.startTime = startTime;

    }
    private void refreshView(Result result) {
        Activity activity = this.getActivity();

        TextView avgBpm = (TextView)activity.findViewById(R.id.avg_bpm);
        avgBpm.setText(String.valueOf(result.getAverage()));

        TextView minBpm = (TextView)activity.findViewById(R.id.min_bpm);
        minBpm.setText(String.valueOf(result.getMin()));

        TextView maxBpm = (TextView)activity.findViewById(R.id.max_bpm);
        maxBpm.setText(String.valueOf(result.getMax()));

    }
    private Result calculate(List<SensorValue> heartRates) {
        Result result = new Result();

        if (heartRates.isEmpty()) {
            return result;
        }
        result.setMax(0);
        result.setMin(999);
        long sum = 0;
        for (SensorValue val: heartRates) {
            int heartRate = (int)val.getValue();
            if (heartRate > result.getMax()) {
                result.setMax(heartRate);
            }
            if (heartRate < result.getMin()) {
                result.setMin(heartRate);
            }
            sum += heartRate;
        }
        result.setAverage((int) (sum / heartRates.size()));

        return result;
    }

    @Override
    public void onClick(View v) {

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.remove(this);
        fragmentTransaction.commit();


    }
}