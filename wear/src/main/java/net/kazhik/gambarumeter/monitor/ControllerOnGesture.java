package net.kazhik.gambarumeter.monitor;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by kazhik on 14/11/02.
 */
public class ControllerOnGesture
        extends GestureDetector.SimpleOnGestureListener {

    public interface GestureListener {
        public void onGestureStart();
        public void onGestureStop();
    }
    private GestureDetectorCompat gestureDetector;
    private GestureListener listener;
    private boolean started = false;
    private static final String TAG = "GestureManager";

    public ControllerOnGesture(Context context, GestureListener listener) {
        this.listener = listener;

        this.gestureDetector = new GestureDetectorCompat(context, this);
        this.gestureDetector.setOnDoubleTapListener(this);

    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress");
        super.onLongPress(e);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        if (this.started == false) {
            Log.d(TAG, "start");
            this.listener.onGestureStart();
        } else {
            Log.d(TAG, "stop");
            this.listener.onGestureStop();
        }
        this.started = !this.started;

        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.gestureDetector.onTouchEvent(event);

    }
}
