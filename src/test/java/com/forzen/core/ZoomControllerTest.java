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

    @Test
    void appearanceDefaults() {
        ZoomController z = new ZoomController();
        assertEquals(LensShape.CIRCLE, z.getLensShape());
        assertEquals("#00FF41", z.getBorderColor());
        assertTrue(z.isShowCrosshair());
        assertEquals(ZoomMode.LENS, z.getMode());
    }

    @Test
    void modeOnlyLensOrDocked() {
        ZoomController z = new ZoomController();
        z.setMode(ZoomMode.DOCKED);
        assertEquals(ZoomMode.DOCKED, z.getMode());
        z.setMode(ZoomMode.LENS);
        assertEquals(ZoomMode.LENS, z.getMode());
    }

    @Test
    void legacyFullMapsToLens() {
        assertEquals(ZoomMode.LENS, ZoomMode.fromStored("FULL"));
        assertEquals(ZoomMode.DOCKED, ZoomMode.fromStored("DOCKED"));
        assertEquals(ZoomMode.LENS, ZoomMode.fromStored("bogus"));
    }

    @Test
    void effectiveFpsUsesTarget() {
        ZoomController z = new ZoomController();
        z.setTargetFps(60);
        assertEquals(60, z.effectiveFps());
        z.setMode(ZoomMode.DOCKED);
        assertEquals(60, z.effectiveFps());
    }

    @Test
    void borderColorNormalized() {
        ZoomController z = new ZoomController();
        z.setBorderColor("ff0000");
        assertEquals("#FF0000", z.getBorderColor());
        z.setBorderColor("not-a-color");
        assertEquals("#00FF41", z.getBorderColor());
    }

    @Test
    void resetDefaultsRestoresShapeAndTheme() {
        ZoomController z = new ZoomController();
        z.setLensShape(LensShape.ROUNDED);
        z.setUiTheme("midnight");
        z.setMode(ZoomMode.DOCKED);
        z.resetDefaults();
        assertEquals(LensShape.CIRCLE, z.getLensShape());
        assertEquals("forzen_dark", z.getUiTheme());
        assertEquals(ZoomMode.LENS, z.getMode());
    }
}
