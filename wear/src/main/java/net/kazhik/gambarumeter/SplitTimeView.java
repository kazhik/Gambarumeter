package net.kazhik.gambarumeter;

import android.os.SystemClock;
import android.support.wearable.view.WatchViewStub;
import android.text.format.DateUtils;
import android.widget.Chronometer;

/**
 * Created by kazhik on 14/10/11.
 */
public class SplitTimeView implements Chronometer.OnChronometerTickListener {

    private Chronometer chronometer;

    public void initialize(WatchViewStub stub) {
        this.chronometer = (Chronometer) stub.findViewById(R.id.split_time);
        this.chronometer.setText(DateUtils.formatElapsedTime(0));
        this.chronometer.setOnChronometerTickListener(this);

    }
    public String getText() {
        return this.chronometer.getText().toString();
    }
    public void start() {
        this.chronometer.setBase(SystemClock.elapsedRealtime());
        this.chronometer.start();
    }
    public void stop() {
        this.chronometer.stop();
    }


    @Override
    public void onChronometerTick(Chronometer chronometer) {
        long elapsedSeconds = (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
        this.chronometer.setText(DateUtils.formatElapsedTime(elapsedSeconds));

    }
}
