package net.kazhik.gambarumeter;

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
    public void setTime(long elapsed) {
        this.elapsed = elapsed;
    }

    @Override
    public void run() {
        this.splitTime.setText(DateUtils.formatElapsedTime(this.elapsed / 1000));
    }


}
