package net.kazhik.gambarumeter;

import android.widget.TextView;

/**
 * Created by kazhik on 14/10/18.
 */
public class StepCountView implements Runnable {
    private TextView stepCountText;
    private int stepCount = 0;
    public void initialize(TextView textView) {
        this.stepCountText = textView;

    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    @Override
    public void run() {
        this.stepCountText.setText(String.valueOf(this.stepCount));
    }
}
