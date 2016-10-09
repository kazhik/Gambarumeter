package net.kazhik.gambarumeter.detail;

import android.support.wearable.view.WearableListView.ViewHolder;
import android.view.View;
import android.widget.TextView;

import net.kazhik.gambarumeter.R;

/**
 * Created by kazhik on 16/10/09.
 */

public class ItemViewHolder extends ViewHolder {
    private TextView tvDistance;
    private TextView tvLapTime;
    private TextView tvStepCount;
    private TextView tvHeartRate;
    public ItemViewHolder(View itemView) {
        super(itemView);
        // find the text view within the custom item's layout
        this.tvDistance = (TextView) itemView.findViewById(R.id.distance);
        this.tvLapTime = (TextView) itemView.findViewById(R.id.laptime);
        this.tvStepCount = (TextView) itemView.findViewById(R.id.stepcount_value);
        this.tvHeartRate = (TextView) itemView.findViewById(R.id.heartrate_value);
    }

    public void setHeartRate(String heartRate) {
        this.tvHeartRate.setText(heartRate);
    }
    public void setDistance(String distance) {
        this.tvDistance.setText(distance);
    }
    public void setLapTime(String lapTime) {
        this.tvLapTime.setText(lapTime);
    }
    public void setStepCount(String stepCount) {
        this.tvStepCount.setText(stepCount);
    }

}
