package net.kazhik.gambarumeter.monitor;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kazhik on 14/10/26.
 */
public class Stopwatch implements Handler.Callback {
    public interface OnTickListener {
        void onTick(long elapsed);
    }
    private long startTime = -1;
    private List<Long> laps = new ArrayList<Long>();
    private Handler handler = new Handler(this);
    private OnTickListener listener;
    private static final int TICK = 2000;
    private long frequency = 0L;
    private boolean running = false;

    public Stopwatch(long frequency, OnTickListener listener) {
        this.frequency = frequency;
        this.listener = listener;
    }
    public void start() {
        this.startTime = SystemClock.elapsedRealtime();
        this.running = true;
        this.handler.sendEmptyMessage(TICK);
    }
    public long stop() {
        this.handler.removeMessages(TICK);
        this.running = false;
        return SystemClock.elapsedRealtime() - this.startTime;
    }

    public void lap() {
        this.laps.add(SystemClock.elapsedRealtime());
    }
    public List<Long> getLaps() {
        return this.laps;
    }
    public void reset() {
        this.laps.clear();
    }

    @Override
    public boolean handleMessage(Message message) {
        if (message.what == TICK && this.running == true) {
            this.listener.onTick(SystemClock.elapsedRealtime() - this.startTime);
            this.handler.sendEmptyMessageDelayed(TICK, this.frequency);
        }
        return false;
    }
}