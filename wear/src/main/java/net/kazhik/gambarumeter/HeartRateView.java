package net.kazhik.gambarumeter;

import android.widget.TextView;

/**
 * Created by kazhik on 14/10/18.
 */
public class HeartRateView implements Runnable {
    private TextView bpmText;
    private int currentRate = 0;
    public void initialize(TextView textView) {
        this.bpmText = textView;

    }

    public void setCurrentRate(int rate) {
        this.currentRate = rate;
    }

    @Override
    public void run() {
        this.bpmText.setText(String.valueOf(this.currentRate));
    }
}
