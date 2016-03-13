package net.kazhik.gambarumeter.main;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import net.kazhik.gambarumeter.R;

/**
 * Created by kazhik on 14/11/02.
 */
public class UserInputManager
        extends GestureDetector.SimpleOnGestureListener
        implements View.OnClickListener, View.OnTouchListener {

    public interface UserInputListener {
        void onUserStart();
        void onUserStop();
        void onUserDismiss();
    }
    private ImageButton startButton;
    private ImageButton stopButton;

    private GestureDetectorCompat gestureDetector;
    private UserInputListener listener;
    private boolean started = false;
    private static final String TAG = "GestureManager";

    public UserInputManager(UserInputListener listener) {
        this.listener = listener;
    }
    public UserInputManager initTouch(Context context, FrameLayout layout) {
        this.gestureDetector = new GestureDetectorCompat(context, this);
        this.gestureDetector.setOnDoubleTapListener(this);
        layout.setOnTouchListener(this);

        return this;
    }
    public UserInputManager initButtons(ImageButton startButton, ImageButton stopButton) {
        this.startButton = startButton;
        this.stopButton = stopButton;

        this.startButton = startButton;
        this.startButton.setOnClickListener(this);

        this.stopButton = stopButton;
        this.stopButton.setOnClickListener(this);
        this.stopButton.setVisibility(View.GONE);

        return this;
    }

    // View.OnClickListener
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start) {

            this.onUserStart();

        } else if (view.getId() == R.id.stop) {
            this.onUserStop();

        }
    }
    // View.OnTouchListener
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d(TAG, "onTouch");
        return this.gestureDetector.onTouchEvent(motionEvent);
    }

    private void onUserStart() {
        this.toggleVisibility(true);

        this.listener.onUserStart();

    }
    private void onUserStop() {
        this.toggleVisibility(false);

        this.listener.onUserStop();

    }
    public void toggleVisibility(boolean started) {
        if (started) {
            this.startButton.setVisibility(View.GONE);
            this.stopButton.setVisibility(View.VISIBLE);
        } else {
            this.startButton.setVisibility(View.VISIBLE);
            this.stopButton.setVisibility(View.GONE);
        }
        this.started = started;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d(TAG, "onLongPress");
        this.listener.onUserDismiss();
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d(TAG, " onScroll: " + e1.toString()+e2.toString());
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, " onFling: " + e1.toString()+e2.toString());
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        if (!this.started) {
            Log.d(TAG, "start");
            this.onUserStart();
        } else {
            Log.d(TAG, "stop");
            this.onUserStop();
        }

        return false;
    }
}
