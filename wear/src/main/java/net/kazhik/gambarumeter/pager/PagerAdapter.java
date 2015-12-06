package net.kazhik.gambarumeter.pager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import net.kazhik.gambarumeter.history.HeartRateHistoryFragment;
import net.kazhik.gambarumeter.history.LocationHistoryFragment;
import net.kazhik.gambarumeter.main.HeartRateMainFragment;
import net.kazhik.gambarumeter.main.LocationMainFragment;
import net.kazhik.gambarumeter.settings.SettingsFragment;

/**
 * Created by kazhik on 14/11/11.
 */
public class PagerAdapter extends FragmentGridPagerAdapter
        implements GridViewPager.OnPageChangeListener {

    private PagerFragment[][] fragments;
    private static final String TAG = "PagerAdapter";

    public PagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        PackageManager pm = context.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            this.fragments = new PagerFragment[][]{
                    {
                            new LocationMainFragment(),
                            new LocationHistoryFragment(),
                            new SettingsFragment()
                    }
            };
            if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_HEART_RATE)) {

            }
        } else {
            this.fragments = new PagerFragment[][]{
                    {
                            new HeartRateMainFragment(),
                            new HeartRateHistoryFragment()
                    }
            };
        }
        
        
        
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
        PagerFragment fragment = fragments[row][col];
        fragment.refreshView();
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
