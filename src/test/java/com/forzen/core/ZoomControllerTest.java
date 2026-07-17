package com.forzen.core;

import com.forzen.filter.FilterMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZoomControllerTest {

    @Test
    void zoomClampedBetween1And8() {
        ZoomController z = new ZoomController();
        z.setZoomLevel(0.1);
        assertEquals(1.0, z.getZoomLevel(), 0.001);
        z.setZoomLevel(99);
        assertEquals(8.0, z.getZoomLevel(), 0.001);
    }

    @Test
    void zoomInOutSteps() {
        ZoomController z = new ZoomController();
        z.setZoomLevel(2.0);
        z.zoomIn();
        assertEquals(2.5, z.getZoomLevel(), 0.001);
        z.zoomOut();
        assertEquals(2.0, z.getZoomLevel(), 0.001);
    }

    @Test
    void toggleRunning() {
        ZoomController z = new ZoomController();
        assertTrue(z.isRunning());
        z.toggleRunning();
        assertFalse(z.isRunning());
    }

    @Test
    void filterModeAssignable() {
        ZoomController z = new ZoomController();
        z.setFilterMode(FilterMode.INVERT);
        assertEquals(FilterMode.INVERT, z.getFilterMode());
    }
}
