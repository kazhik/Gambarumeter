package net.kazhik.gambarumeter;

import android.support.wearable.view.WatchViewStub;
import android.text.format.DateUtils;
import android.widget.TextView;

/**
 * Created by kazhik on 14/10/11.
 */
public class SplitTimeView implements Runnable {

    private TextView splitTime;
    private long elapsed;

    public void initialize(WatchViewStub stub) {
        this.splitTime = (TextView) stub.findViewById(R.id.split_time);

    }
    public void setTime(long elapsed) {
        this.elapsed = elapsed;
    }

    @Override
    public void run() {
        this.splitTime.setText(DateUtils.formatElapsedTime(this.elapsed / 1000));
    }


}
