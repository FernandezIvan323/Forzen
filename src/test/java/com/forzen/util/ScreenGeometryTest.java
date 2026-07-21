package com.forzen.util;

import com.forzen.util.ScreenGeometry.MonitorInfo;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

class ScreenGeometryTest {

    /** Physical 1920x1080, FX logical 1536x864 (125%). */
    private static MonitorInfo laptop125() {
        Rectangle2D fx = new Rectangle2D(0, 0, 1536, 864);
        Rectangle2D visual = new Rectangle2D(0, 0, 1536, 816); // taskbar ~48px
        return new MonitorInfo(0, 0, 1920, 1080, 1.25, 1.25, fx, visual, null);
    }

    private static MonitorInfo hiDpi150() {
        Rectangle2D fx = new Rectangle2D(0, 0, 1280, 720);
        return new MonitorInfo(0, 0, 1920, 1080, 1.5, 1.5, fx, null);
    }

    @Test
    void localMapsPhysicalEdgesToFullLogical() {
        MonitorInfo m = laptop125();
        assertEquals(0.0, m.toLocalX(0), 0.5);
        assertEquals(0.0, m.toLocalY(0), 0.5);
        assertEquals(1536.0, m.toLocalX(1920), 0.5);
        assertEquals(864.0, m.toLocalY(1080), 0.5);
    }

    @Test
    void visualLocalAccountsForWorkAreaOffset() {
        MonitorInfo m = laptop125();
        // Full local Y at bottom of work area vs full screen
        assertEquals(0.0, m.toVisualLocalY(0), 0.5);
        // visual height 816 — mapping still from full phys
        double fullY = m.toLocalY(540);
        assertEquals(fullY, m.toVisualLocalY(540), 0.5); // visual minY = bounds minY
    }

    @Test
    void physPerFxIsRatio() {
        MonitorInfo m = laptop125();
        assertEquals(1.25, m.physPerFxX(), 0.01);
        assertEquals(1.25, m.physPerFxY(), 0.01);
    }

    @Test
    void captureViewportKeepsSizeAtCorner() {
        MonitorInfo m = hiDpi150();
        Rectangle r = ScreenGeometry.captureViewport(10, 10, 960, 540, m);
        assertEquals(960, r.width);
        assertEquals(540, r.height);
        assertEquals(0, r.x);
        assertEquals(0, r.y);
    }

    @Test
    void captureViewportNoBlackAtTopLeft_staysInsideMonitor() {
        MonitorInfo m = laptop125();
        Rectangle r = ScreenGeometry.captureViewport(10, 10, 400, 300, m);
        assertNotNull(r);
        assertEquals(0, r.x);
        assertEquals(0, r.y);
        assertEquals(400, r.width);
        assertEquals(300, r.height);
        // Entire region inside monitor
        assertTrue(r.x >= m.physX());
        assertTrue(r.y >= m.physY());
        assertTrue(r.x + r.width <= m.physX() + m.physW());
        assertTrue(r.y + r.height <= m.physY() + m.physH());
    }

    @Test
    void captureViewportClampsBottomRight() {
        MonitorInfo m = laptop125();
        Rectangle r = ScreenGeometry.captureViewport(1910, 1070, 400, 300, m);
        assertEquals(400, r.width);
        assertEquals(300, r.height);
        assertEquals(1920 - 400, r.x);
        assertEquals(1080 - 300, r.y);
    }

    @Test
    void clampRejectsEmpty() {
        MonitorInfo m = hiDpi150();
        assertNull(ScreenGeometry.clampRegion(0, 0, 0, 10, m));
    }

    @Test
    void captureAroundLegacyClamps() {
        MonitorInfo m = hiDpi150();
        Rectangle r = ScreenGeometry.captureAround(10, 10, 100, 80, m);
        assertNotNull(r);
        assertEquals(0, r.x);
        assertEquals(0, r.y);
    }

    @Test
    void captureViewportHighZoomKeepsSizeAtAllCorners() {
        MonitorInfo m = laptop125();
        // ~zoom 8 relative to full logical: 1536/8 × 864/8 → phys via 1.25
        double srcW = (1536 * m.physPerFxX()) / 8.0;
        double srcH = (864 * m.physPerFxY()) / 8.0;
        int[][] cursors = {{10, 10}, {1910, 10}, {10, 1070}, {1910, 1070}, {960, 540}};
        for (int[] c : cursors) {
            Rectangle r = ScreenGeometry.captureViewport(c[0], c[1], srcW, srcH, m);
            assertNotNull(r);
            assertEquals(Math.round(srcW), r.width, 1);
            assertEquals(Math.round(srcH), r.height, 1);
            assertTrue(r.x >= m.physX());
            assertTrue(r.y >= m.physY());
            assertTrue(r.x + r.width <= m.physX() + m.physW());
            assertTrue(r.y + r.height <= m.physY() + m.physH());
        }
    }

    @Test
    void captureViewportZoom1FillsMonitor() {
        MonitorInfo m = laptop125();
        double srcW = m.physW();
        double srcH = m.physH();
        Rectangle r = ScreenGeometry.captureViewport(100, 100, srcW, srcH, m);
        assertEquals(m.physW(), r.width);
        assertEquals(m.physH(), r.height);
        assertEquals(m.physX(), r.x);
        assertEquals(m.physY(), r.y);
    }

    @Test
    void cursorInViewportCenterWhenUnclamped() {
        MonitorInfo m = laptop125();
        Rectangle r = ScreenGeometry.captureViewport(960, 540, 400, 300, m);
        int[] hot = ScreenGeometry.cursorInViewport(960, 540, r);
        assertEquals(200, hot[0], 1);
        assertEquals(150, hot[1], 1);
    }

    @Test
    void cursorInViewportNotCenterWhenClampedTopLeft() {
        MonitorInfo m = laptop125();
        Rectangle r = ScreenGeometry.captureViewport(10, 10, 400, 300, m);
        // Clamped to 0,0 — cursor near top-left of buffer
        int[] hot = ScreenGeometry.cursorInViewport(10, 10, r);
        assertEquals(10, hot[0], 1);
        assertEquals(10, hot[1], 1);
        assertTrue(hot[0] < r.width / 2);
        assertTrue(hot[1] < r.height / 2);
    }

    @Test
    void captureSizePhysUsesScale() {
        MonitorInfo m = laptop125();
        double[] s = ScreenGeometry.captureSizePhys(300, 300, 2.0, m);
        // 300 * 1.25 / 2 = 187.5
        assertEquals(187.5, s[0], 0.5);
        assertEquals(187.5, s[1], 0.5);
    }
}
