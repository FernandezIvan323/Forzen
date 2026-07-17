package com.forzen.util;

import com.forzen.util.ScreenGeometry.MonitorInfo;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

class ScreenGeometryTest {

    private static MonitorInfo fakeMonitor() {
        // Physical 1920x1080 at origin, scale 1.5, logical 1280x720
        Rectangle2D fx = new Rectangle2D(0, 0, 1280, 720);
        return new MonitorInfo(0, 0, 1920, 1080, 1.5, 1.5, fx, null);
    }

    @Test
    void clampKeepsRegionInsideMonitor() {
        MonitorInfo m = fakeMonitor();
        Rectangle r = ScreenGeometry.clampRegion(-50, -20, 200, 100, m);
        assertNotNull(r);
        assertEquals(0, r.x);
        assertEquals(0, r.y);
        assertTrue(r.width <= 200);
        assertTrue(r.height <= 100);
    }

    @Test
    void clampRejectsEmptyRegion() {
        MonitorInfo m = fakeMonitor();
        assertNull(ScreenGeometry.clampRegion(0, 0, 0, 10, m));
        assertNull(ScreenGeometry.clampRegion(5000, 5000, 100, 100, m));
    }

    @Test
    void captureAroundCentersAndClamps() {
        MonitorInfo m = fakeMonitor();
        Rectangle r = ScreenGeometry.captureAround(10, 10, 100, 80, m);
        assertNotNull(r);
        assertEquals(0, r.x);
        assertEquals(0, r.y);
        assertTrue(r.width > 0 && r.height > 0);
    }

    @Test
    void physicalToFxConversion() {
        MonitorInfo m = fakeMonitor();
        assertEquals(100.0, m.toFxX(150), 0.01); // 150 / 1.5
        assertEquals(200.0, m.toFxY(300), 0.01);
        assertEquals(150, m.toPhysicalX(100));
        assertEquals(300, m.toPhysicalY(200));
    }
}
