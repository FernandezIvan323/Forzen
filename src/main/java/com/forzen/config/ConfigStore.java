package com.forzen.config;

import com.forzen.core.ZoomMode;

import java.util.prefs.Preferences;

public class ConfigStore {

    private static final String KEY_ZOOM_LEVEL = "zoomLevel";
    private static final String KEY_ZOOM_MODE = "zoomMode";
    private static final String KEY_LENS_WIDTH = "lensWidth";
    private static final String KEY_LENS_HEIGHT = "lensHeight";
    private static final String KEY_SHOW_FPS = "showFps";
    private static final String KEY_BORDER_WIDTH = "borderWidth";
    private static final String KEY_LENS_SHAPE = "lensShape";
    private static final String KEY_INVERT_COLORS = "invertColors";
    private static final String KEY_HIGH_CONTRAST = "highContrast";
    private static final String KEY_GRAYSCALE = "grayscale";
    private static final String KEY_BRIGHTNESS = "brightness";
    private static final String KEY_CONTRAST = "contrast";
    private static final String KEY_SATURATION = "saturation";
    private static final String KEY_START_WITH_OS = "startWithOs";
    private static final String KEY_AUTO_OCR = "autoOcr";

    private final Preferences prefs;

    public ConfigStore() {
        prefs = Preferences.userNodeForPackage(ConfigStore.class);
    }

    public double getZoomLevel() { return prefs.getDouble(KEY_ZOOM_LEVEL, 2.0); }
    public void setZoomLevel(double v) { prefs.putDouble(KEY_ZOOM_LEVEL, v); }

    public ZoomMode getZoomMode() {
        try { return ZoomMode.valueOf(prefs.get(KEY_ZOOM_MODE, ZoomMode.LENS.name())); }
        catch (Exception e) { return ZoomMode.LENS; }
    }
    public void setZoomMode(ZoomMode v) { prefs.put(KEY_ZOOM_MODE, v.name()); }

    public double getLensWidth() { return prefs.getDouble(KEY_LENS_WIDTH, 300); }
    public void setLensWidth(double v) { prefs.putDouble(KEY_LENS_WIDTH, v); }

    public double getLensHeight() { return prefs.getDouble(KEY_LENS_HEIGHT, 300); }
    public void setLensHeight(double v) { prefs.putDouble(KEY_LENS_HEIGHT, v); }

    public boolean isShowFps() { return prefs.getBoolean(KEY_SHOW_FPS, false); }
    public void setShowFps(boolean v) { prefs.putBoolean(KEY_SHOW_FPS, v); }

    public double getBorderWidth() { return prefs.getDouble(KEY_BORDER_WIDTH, 2.5); }
    public void setBorderWidth(double v) { prefs.putDouble(KEY_BORDER_WIDTH, v); }

    public boolean isLensCircular() { return prefs.getBoolean(KEY_LENS_SHAPE, true); }
    public void setLensCircular(boolean v) { prefs.putBoolean(KEY_LENS_SHAPE, v); }

    public boolean isInvertColors() { return prefs.getBoolean(KEY_INVERT_COLORS, false); }
    public void setInvertColors(boolean v) { prefs.putBoolean(KEY_INVERT_COLORS, v); }

    public boolean isHighContrast() { return prefs.getBoolean(KEY_HIGH_CONTRAST, false); }
    public void setHighContrast(boolean v) { prefs.putBoolean(KEY_HIGH_CONTRAST, v); }

    public boolean isGrayscale() { return prefs.getBoolean(KEY_GRAYSCALE, false); }
    public void setGrayscale(boolean v) { prefs.putBoolean(KEY_GRAYSCALE, v); }

    public double getBrightness() { return prefs.getDouble(KEY_BRIGHTNESS, 100); }
    public void setBrightness(double v) { prefs.putDouble(KEY_BRIGHTNESS, v); }

    public double getContrast() { return prefs.getDouble(KEY_CONTRAST, 100); }
    public void setContrast(double v) { prefs.putDouble(KEY_CONTRAST, v); }

    public double getSaturation() { return prefs.getDouble(KEY_SATURATION, 100); }
    public void setSaturation(double v) { prefs.putDouble(KEY_SATURATION, v); }

    public boolean isStartWithOs() { return prefs.getBoolean(KEY_START_WITH_OS, false); }
    public void setStartWithOs(boolean v) { prefs.putBoolean(KEY_START_WITH_OS, v); }

    public boolean isAutoOcr() { return prefs.getBoolean(KEY_AUTO_OCR, false); }
    public void setAutoOcr(boolean v) { prefs.putBoolean(KEY_AUTO_OCR, v); }

    public void applyTo(com.forzen.core.ZoomController zoomController) {
        zoomController.setZoomLevel(getZoomLevel());
        zoomController.setMode(getZoomMode());
        zoomController.setLensWidth(getLensWidth());
        zoomController.setLensHeight(getLensHeight());
        zoomController.setShowFps(isShowFps());
    }

    public void saveFrom(com.forzen.core.ZoomController zoomController) {
        setZoomLevel(zoomController.getZoomLevel());
        setZoomMode(zoomController.getMode());
        setLensWidth(zoomController.getLensWidth());
        setLensHeight(zoomController.getLensHeight());
        setShowFps(zoomController.isShowFps());
    }
}
