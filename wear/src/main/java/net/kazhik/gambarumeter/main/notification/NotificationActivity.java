package net.kazhik.gambarumeter.main.notification;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import net.kazhik.gambarumeter.R;

/**
 * Created by kazhik on 16/04/27.
 */
public class NotificationActivity extends Activity {
    private static final String TAG = "NotificationActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            Log.d(TAG, "time:" + b.getLong("time"));
        }
    }

}
