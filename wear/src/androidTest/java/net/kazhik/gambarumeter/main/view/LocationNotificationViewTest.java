package net.kazhik.gambarumeter.main.view;

import android.content.Context;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import net.kazhik.gambarumeter.main.notification.LocationNotificationView;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by kazhik on 15/01/10.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class LocationNotificationViewTest extends InstrumentationTestCase {

    /*
    @Test
    public void testMakeSummaryText() throws Exception {

        LocationNotificationView notificationView = new LocationNotificationView();


        notificationView.initialize(InstrumentationRegistry.getTargetContext().getApplicationContext());

        notificationView.updateStepCount(445);
        notificationView.updateDistance(2300);
        notificationView.updateLap(10 * 60 * 1000);

        Method makeSummaryText =
                notificationView.getClass().getSuperclass().getDeclaredMethod("makeSummaryText",
                        long.class);
        makeSummaryText.setAccessible(true);
        Object[] parameters = new Object[]{45 * 1000};

        String text = (String)makeSummaryText.invoke(notificationView, parameters);

        assertEquals("00:45 2.3km", text);

    }

    @Test
    public void testMakeDetailedText() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext().getApplicationContext();
        Configuration config = appContext.getResources().getConfiguration();
        config.setLocale(Locale.US);
        appContext.getResources().updateConfiguration(config,
                appContext.getResources().getDisplayMetrics());

        LocationNotificationView notificationView = new LocationNotificationView();


        notificationView.initialize(appContext);

        notificationView.updateStepCount(445);
        notificationView.updateDistance(2300);
        notificationView.updateLap(((4 * 60) + 30) * 1000);

        Method makeDetailedText =
                notificationView.getClass().getSuperclass().getDeclaredMethod("makeDetailedText");
        makeDetailedText.setAccessible(true);

        String text = (String)makeDetailedText.invoke(notificationView);

        assertEquals("445steps 04:30/km", text);


        notificationView.updateStepCount(446);
        notificationView.updateDistance(2334);
        notificationView.updateLap(((4 * 60) + 35) * 1000);

        text = (String)makeDetailedText.invoke(notificationView);

        assertEquals("446steps 04:35/mi", text);

    }
    */
}
