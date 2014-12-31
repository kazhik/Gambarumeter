package net.kazhik.gambarumeter.view;

import android.widget.TextView;

import java.text.DecimalFormat;

/**
 * Created by kazhik on 14/10/18.
 */
public class DistanceView implements Runnable {
    private TextView distanceText;
    private float distance = 0;
    public void initialize(TextView textView) {
        this.distanceText = textView;

        this.setEnable(false);
    }

    public DistanceView setDistance(float distance) {
        this.distance = distance;

        return this;
    }
    public void setEnable(boolean enabled) {
        if (enabled) {
            this.distanceText.setText("0.00");
        } else {
            this.distanceText.setText("-.--");
        }
    }

    public void refresh() {
        String str = String.format("%.2f", this.distance);
        this.distanceText.setText(str);
    }

    @Override
    public void run() {
        this.refresh();
    }
}
