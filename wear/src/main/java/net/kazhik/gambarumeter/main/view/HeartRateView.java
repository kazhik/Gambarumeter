package net.kazhik.gambarumeter.main.view;

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

    public HeartRateView setCurrentRate(int rate) {
        this.currentRate = rate;

        return this;
    }

    public void refresh() {
        this.bpmText.setText(String.valueOf(this.currentRate));
    }

    @Override
    public void run() {
        this.refresh();
    }
}
