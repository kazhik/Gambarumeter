package net.kazhik.gambarumeter;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

public class Gambarumeter extends Activity
        implements WatchViewStub.OnLayoutInflatedListener {

    private static final String TAG = "Gambarumeter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        setContentView(R.layout.stub);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.stub);
        stub.setOnLayoutInflatedListener(this);

    }

    @Override
    public void onLayoutInflated(WatchViewStub watchViewStub) {
        this.initPager();
    }

    private void initPager() {
        Log.d(TAG, "initPager: ");
        PagerAdapter pagerAdapter = new PagerAdapter(this, this.getFragmentManager());

        GridViewPager pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOnPageChangeListener(pagerAdapter);
        pager.setAdapter(pagerAdapter);

    }

}
