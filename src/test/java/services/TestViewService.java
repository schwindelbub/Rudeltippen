package services;

import static org.junit.Assert.*;

import java.util.Date;

import models.User;
import ninja.NinjaTest;

import org.junit.Test;

public class TestViewService extends NinjaTest {

    @Test
    public void testGetPlaceTrend() {
        CommonService commonService = getInjector().getInstance(CommonService.class);

        final User user = new User();
        user.setPlace(1);
        user.setPreviousPlace(0);

        assertEquals("", commonService.getPlaceTrend(user));

        user.setPlace(2);
        user.setPreviousPlace(1);

        assertEquals("<i class=\"icon-arrow-down icon-red\"></i> (1)", commonService.getPlaceTrend(user));

        user.setPlace(1);
        user.setPreviousPlace(2);

        assertEquals("<i class=\"icon-arrow-up icon-green\"></i> (2)", commonService.getPlaceTrend(user));

        user.setPreviousPlace(1);

        assertEquals("<i class=\"icon-minus\"></i> (1)", commonService.getPlaceTrend(user));
    }

    @Test
    public void testGetPlaceName() {
        CommonService commonService = getInjector().getInstance(CommonService.class);

        assertEquals(commonService.getPlaceName(-5), "");
        assertEquals(commonService.getPlaceName(11), "");
        assertEquals(commonService.getPlaceName(1), "Erster");
    }

    @Test
    public void testFormatDate() {
        CommonService commonService = getInjector().getInstance(CommonService.class);

        assertNotNull(commonService.difference(new Date()));
    }
}