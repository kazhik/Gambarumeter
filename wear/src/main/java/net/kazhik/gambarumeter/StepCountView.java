package net.kazhik.gambarumeter;

import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

/**
 * Created by kazhik on 14/10/18.
 */
public class StepCountView implements Runnable {
    private TextView stepCountText;
    private int stepCount = 0;
    public void initialize(WatchViewStub stub) {
        this.stepCountText = (TextView) stub.findViewById(R.id.stepcount_value);

    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    @Override
    public void run() {
        this.stepCountText.setText(String.valueOf(this.stepCount));
    }
}
