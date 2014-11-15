package net.kazhik.gambarumeter.view;

import android.text.format.DateUtils;
import android.widget.TextView;

/**
 * Created by kazhik on 14/10/11.
 */
public class SplitTimeView implements Runnable {

    private TextView splitTime;
    private long elapsed;

    public void initialize(TextView textView) {
        this.splitTime = textView;

    }
    public SplitTimeView setTime(long elapsed) {
        this.elapsed = elapsed;
        return this;
    }

    public void refresh() {
        this.splitTime.setText(DateUtils.formatElapsedTime(this.elapsed / 1000));
    }

    @Override
    public void run() {
        this.refresh();
    }


}
