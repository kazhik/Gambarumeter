package net.kazhik.gambarumeter.main;

import net.kazhik.gambarumeter.main.monitor.LocationMonitorImpl;

/**
 * Created by kazhik on 14/11/11.
 */
public class GpsMainFragment extends LocationMainFragment {
    @Override
    protected Class<?> getServiceClass() {
        return LocationMonitorImpl.class;
    }
}
