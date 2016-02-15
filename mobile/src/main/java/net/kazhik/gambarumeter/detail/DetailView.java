package net.kazhik.gambarumeter.detail;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Created by kazhik on 16/02/09.
 */
public interface DetailView {
    void onCreate(Bundle savedInstance);

    void onResume();
    void onPause();

    void onDestroy();

    void onLowMemory();

    void onSaveInstanceState(Bundle outState);

    void setRootView(View root);
    void setContext(Context context);

}
