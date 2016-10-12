package net.kazhik.gambarumeter.main.view;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import org.junit.runner.RunWith;

/**
 * Created by kazhik on 15/01/10.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class LocationNotificationViewTest extends InstrumentationTestCase {

    /*
    @Test
    public void testMakeSummaryText() throws Exception {

        LocationNotificationView notificationController = new LocationNotificationView();


        notificationController.initialize(InstrumentationRegistry.getTargetContext().getApplicationContext());

        notificationController.updateStepCount(445);
        notificationController.updateDistance(2300);
        notificationController.updateLap(10 * 60 * 1000);

        Method makeSummaryText =
                notificationController.getClass().getSuperclass().getDeclaredMethod("makeSummaryText",
                        long.class);
        makeSummaryText.setAccessible(true);
        Object[] parameters = new Object[]{45 * 1000};

        String text = (String)makeSummaryText.invoke(notificationController, parameters);

        assertEquals("00:45 2.3km", text);

    }

    @Test
    public void testMakeDetailedText() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext().getApplicationContext();
        Configuration config = appContext.getResources().getConfiguration();
        config.setLocale(Locale.US);
        appContext.getResources().updateConfiguration(config,
                appContext.getResources().getDisplayMetrics());

        LocationNotificationView notificationController = new LocationNotificationView();


        notificationController.initialize(appContext);

        notificationController.updateStepCount(445);
        notificationController.updateDistance(2300);
        notificationController.updateLap(((4 * 60) + 30) * 1000);

        Method makeDetailedText =
                notificationController.getClass().getSuperclass().getDeclaredMethod("makeDetailedText");
        makeDetailedText.setAccessible(true);

        String text = (String)makeDetailedText.invoke(notificationController);

        assertEquals("445steps 04:30/km", text);


        notificationController.updateStepCount(446);
        notificationController.updateDistance(2334);
        notificationController.updateLap(((4 * 60) + 35) * 1000);

        text = (String)makeDetailedText.invoke(notificationController);

        assertEquals("446steps 04:35/mi", text);

    }
    */
}
