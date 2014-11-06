package net.kazhik.gambarumeter;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * Created by kazhik on 14/11/02.
 */
public class UserInputManager
        extends GestureDetector.SimpleOnGestureListener
        implements View.OnClickListener, View.OnTouchListener {

    public interface UserInputListener {
        public void onUserStart();
        public void onUserStop();
    }
    private ImageButton startButton;
    private ImageButton stopButton;

    private GestureDetectorCompat gestureDetector;
    private UserInputListener listener;
    private boolean started = false;
    private static final String TAG = "GestureManager";

    public UserInputManager(Context context, UserInputListener listener, WatchViewStub stub) {
        this.listener = listener;

        this.gestureDetector = new GestureDetectorCompat(context, this);
        this.gestureDetector.setOnDoubleTapListener(this);

        this.startButton = (ImageButton) stub.findViewById(R.id.start);
        this.startButton.setOnClickListener(this);

        this.stopButton = (ImageButton) stub.findViewById(R.id.stop);
        this.stopButton.setOnClickListener(this);
        this.stopButton.setVisibility(View.GONE);

        LinearLayout mainLayout = (LinearLayout)stub.findViewById(R.id.main_layout);
        mainLayout.setOnTouchListener(this);

    }
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start) {

            this.onUserStart();

        } else if (view.getId() == R.id.stop) {
            this.onUserStop();

        }
    }
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return this.gestureDetector.onTouchEvent(motionEvent);

    }
    private void onUserStart() {
        this.startButton.setVisibility(View.GONE);
        this.stopButton.setVisibility(View.VISIBLE);

        this.listener.onUserStart();

    }
    private void onUserStop() {
        this.startButton.setVisibility(View.VISIBLE);
        this.stopButton.setVisibility(View.GONE);

        this.listener.onUserStop();

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
            this.onUserStart();
        } else {
            Log.d(TAG, "stop");
            this.onUserStop();
        }
        this.started = !this.started;

        return false;
    }

}
