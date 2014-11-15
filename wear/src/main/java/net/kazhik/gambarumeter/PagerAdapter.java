package net.kazhik.gambarumeter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import net.kazhik.gambarumeter.history.HistoryCardFragment;
import net.kazhik.gambarumeter.main.MainCardFragment;

/**
 * Created by kazhik on 14/11/11.
 */
public class PagerAdapter extends FragmentGridPagerAdapter
        implements GridViewPager.OnPageChangeListener {
    private static final String TAG = "PagerAdapter";

    public PagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (col == 0) {
            return new MainCardFragment();
        } else if (col == 1) {
            return new HistoryCardFragment();
        } else {

        }
        return null;
    }

    @Override
    public void onPageScrolled(int i, int i2, float v, float v2, int i3, int i4) {

    }

    @Override
    public void onPageSelected(int row, int col) {
        Log.d(TAG, "onPageSelected: " + row + "; " + col);

    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public int getRowCount() {
        return 1;
    }

    @Override
    public int getColumnCount(int row) {
        return 2;
    }
}
