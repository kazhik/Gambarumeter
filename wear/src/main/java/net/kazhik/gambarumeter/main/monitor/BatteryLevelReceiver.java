package net.kazhik.gambarumeter.main.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kazhik on 15/01/17.
 */
public class BatteryLevelReceiver extends BroadcastReceiver {
    private SensorValueListener listener;

    public BatteryLevelReceiver(SensorValueListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BATTERY_LOW)) {
            this.listener.onBatteryLow();
        } else if (intent.getAction().equals(Intent.ACTION_BATTERY_OKAY)) {
            this.listener.onBatteryOkay();
        }
        
    }
}
