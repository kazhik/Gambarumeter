package net.kazhik.gambarumeter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.util.Log;

import net.kazhik.gambarumeter.history.HistoryFragment;
import net.kazhik.gambarumeter.main.MainFragment;

/**
 * Created by kazhik on 14/11/11.
 */
public class PagerAdapter extends FragmentGridPagerAdapter
        implements GridViewPager.OnPageChangeListener {

    private final Fragment[][] fragments = {
            {new MainFragment(), new HistoryFragment()}
    };
    private static final String TAG = "PagerAdapter";

    public PagerAdapter(Context context, FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getFragment(int row, int col) {
        if (row > fragments.length - 1 || col > fragments[row].length - 1) {
            return null;
        }
        return fragments[row][col];
    }

    @Override
    public void onPageScrolled(int i, int i2, float v, float v2, int i3, int i4) {

    }

    @Override
    public void onPageSelected(int row, int col) {
        Log.d(TAG, "onPageSelected: " + row + "; " + col);
        Fragment fragment = fragments[row][col];
        if (fragment instanceof HistoryFragment) {
            ((HistoryFragment)fragment).refreshListItem();
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public int getRowCount() {
        return fragments.length;
    }

    @Override
    public int getColumnCount(int row) {
        return fragments[row].length;
    }
}