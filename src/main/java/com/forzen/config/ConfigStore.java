package com.forzen.config;

import com.forzen.core.DockPosition;
import com.forzen.core.LensShape;
import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;
import com.forzen.filter.FilterMode;
import com.forzen.input.HotkeyAction;
import com.forzen.input.HotkeyBinding;

import java.util.prefs.Preferences;

public class ConfigStore {

    private static final String KEY_ZOOM_LEVEL = "zoomLevel";
    private static final String KEY_ZOOM_MODE = "zoomMode";
    private static final String KEY_LENS_WIDTH = "lensWidth";
    private static final String KEY_LENS_HEIGHT = "lensHeight";
    private static final String KEY_SHOW_FPS = "showFps";
    private static final String KEY_BORDER_WIDTH = "borderWidth";
    private static final String KEY_LENS_SHAPE = "lensShape";
    private static final String KEY_LENS_CORNER_RADIUS = "lensCornerRadius";
    private static final String KEY_BORDER_COLOR = "borderColor";
    private static final String KEY_BORDER_OPACITY = "borderOpacity";
    private static final String KEY_SHOW_CROSSHAIR = "showCrosshair";
    private static final String KEY_CROSSHAIR_COLOR = "crosshairColor";
    private static final String KEY_SMOOTH_SCALING = "smoothScaling";
    private static final String KEY_DOCK_POSITION = "dockPosition";
    private static final String KEY_BRIGHTNESS = "brightness";
    private static final String KEY_CONTRAST = "contrast";
    private static final String KEY_SATURATION = "saturation";
    private static final String KEY_START_WITH_OS = "startWithOs";
    private static final String KEY_AUTO_OCR = "autoOcr";
    private static final String KEY_AUTO_TTS = "autoTts";
    private static final String KEY_FILTER_MODE = "filterMode";
    private static final String KEY_TARGET_FPS = "targetFps";
    private static final String KEY_UI_THEME = "uiTheme";
    private static final String KEY_HOTKEY_PREFIX = "hotkey.";

    private final Preferences prefs;

    public ConfigStore() {
        prefs = Preferences.userNodeForPackage(ConfigStore.class);
    }

    public double getZoomLevel() { return prefs.getDouble(KEY_ZOOM_LEVEL, 2.0); }
    public void setZoomLevel(double v) { prefs.putDouble(KEY_ZOOM_LEVEL, v); }

    public ZoomMode getZoomMode() {
        // FULL (retired) and unknown values → LENS
        return ZoomMode.fromStored(prefs.get(KEY_ZOOM_MODE, ZoomMode.LENS.name()));
    }
    public void setZoomMode(ZoomMode v) {
        prefs.put(KEY_ZOOM_MODE, (v == null ? ZoomMode.LENS : v).name());
    }

    public double getLensWidth() { return prefs.getDouble(KEY_LENS_WIDTH, 300); }
    public void setLensWidth(double v) { prefs.putDouble(KEY_LENS_WIDTH, v); }

    public double getLensHeight() { return prefs.getDouble(KEY_LENS_HEIGHT, 300); }
    public void setLensHeight(double v) { prefs.putDouble(KEY_LENS_HEIGHT, v); }

    public boolean isShowFps() { return prefs.getBoolean(KEY_SHOW_FPS, false); }
    public void setShowFps(boolean v) { prefs.putBoolean(KEY_SHOW_FPS, v); }

    public double getBorderWidth() { return prefs.getDouble(KEY_BORDER_WIDTH, 2.5); }
    public void setBorderWidth(double v) { prefs.putDouble(KEY_BORDER_WIDTH, v); }

    public LensShape getLensShape() {
        String raw = prefs.get(KEY_LENS_SHAPE, null);
        if (raw == null) {
            // Migrate legacy boolean key stored as true/false under same name
            return prefs.getBoolean(KEY_LENS_SHAPE, true) ? LensShape.CIRCLE : LensShape.RECTANGLE;
        }
        try {
            if ("true".equalsIgnoreCase(raw)) return LensShape.CIRCLE;
            if ("false".equalsIgnoreCase(raw)) return LensShape.RECTANGLE;
            return LensShape.valueOf(raw);
        } catch (Exception e) {
            return LensShape.CIRCLE;
        }
    }
    public void setLensShape(LensShape v) { prefs.put(KEY_LENS_SHAPE, v.name()); }

    public double getLensCornerRadius() { return prefs.getDouble(KEY_LENS_CORNER_RADIUS, 18); }
    public void setLensCornerRadius(double v) { prefs.putDouble(KEY_LENS_CORNER_RADIUS, v); }

    public String getBorderColor() { return prefs.get(KEY_BORDER_COLOR, "#00FF41"); }
    public void setBorderColor(String v) { prefs.put(KEY_BORDER_COLOR, v); }

    public double getBorderOpacity() { return prefs.getDouble(KEY_BORDER_OPACITY, 85); }
    public void setBorderOpacity(double v) { prefs.putDouble(KEY_BORDER_OPACITY, v); }

    public boolean isShowCrosshair() { return prefs.getBoolean(KEY_SHOW_CROSSHAIR, true); }
    public void setShowCrosshair(boolean v) { prefs.putBoolean(KEY_SHOW_CROSSHAIR, v); }

    public String getCrosshairColor() { return prefs.get(KEY_CROSSHAIR_COLOR, "#FF003C"); }
    public void setCrosshairColor(String v) { prefs.put(KEY_CROSSHAIR_COLOR, v); }

    public boolean isSmoothScaling() { return prefs.getBoolean(KEY_SMOOTH_SCALING, true); }
    public void setSmoothScaling(boolean v) { prefs.putBoolean(KEY_SMOOTH_SCALING, v); }

    public DockPosition getDockPosition() {
        try { return DockPosition.valueOf(prefs.get(KEY_DOCK_POSITION, DockPosition.TOP_RIGHT.name())); }
        catch (Exception e) { return DockPosition.TOP_RIGHT; }
    }
    public void setDockPosition(DockPosition v) { prefs.put(KEY_DOCK_POSITION, v.name()); }

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

    public boolean isAutoTts() { return prefs.getBoolean(KEY_AUTO_TTS, false); }
    public void setAutoTts(boolean v) { prefs.putBoolean(KEY_AUTO_TTS, v); }

    public int getTargetFps() { return prefs.getInt(KEY_TARGET_FPS, 60); }
    public void setTargetFps(int v) { prefs.putInt(KEY_TARGET_FPS, Math.max(15, Math.min(120, v))); }

    public String getUiTheme() {
        String raw = prefs.get(KEY_UI_THEME, "forzen_dark");
        return com.forzen.ui.theme.ThemeCatalog.resolveId(raw);
    }
    public void setUiTheme(String v) {
        prefs.put(KEY_UI_THEME, com.forzen.ui.theme.ThemeCatalog.resolveId(v));
    }

    public FilterMode getFilterMode() {
        try { return FilterMode.valueOf(prefs.get(KEY_FILTER_MODE, FilterMode.NONE.name())); }
        catch (Exception e) { return FilterMode.NONE; }
    }
    public void setFilterMode(FilterMode v) { prefs.put(KEY_FILTER_MODE, v.name()); }

    public HotkeyBinding getHotkey(HotkeyAction action) {
        String raw = prefs.get(KEY_HOTKEY_PREFIX + action.name(), null);
        return HotkeyBinding.deserialize(raw, HotkeyBinding.defaults(action));
    }

    public void setHotkey(HotkeyAction action, HotkeyBinding binding) {
        prefs.put(KEY_HOTKEY_PREFIX + action.name(), binding.serialize());
    }

    public void applyTo(ZoomController zoomController) {
        zoomController.setZoomLevel(getZoomLevel());
        zoomController.setMode(getZoomMode());
        zoomController.setLensWidth(getLensWidth());
        zoomController.setLensHeight(getLensHeight());
        zoomController.setShowFps(isShowFps());
        zoomController.setFilterMode(getFilterMode());
        zoomController.setBrightness(getBrightness());
        zoomController.setContrast(getContrast());
        zoomController.setSaturation(getSaturation());
        zoomController.setBorderWidth(getBorderWidth());
        zoomController.setLensShape(getLensShape());
        zoomController.setLensCornerRadius(getLensCornerRadius());
        zoomController.setBorderColor(getBorderColor());
        zoomController.setBorderOpacity(getBorderOpacity());
        zoomController.setShowCrosshair(isShowCrosshair());
        zoomController.setCrosshairColor(getCrosshairColor());
        zoomController.setSmoothScaling(isSmoothScaling());
        zoomController.setDockPosition(getDockPosition());
        zoomController.setStartWithOs(isStartWithOs());
        zoomController.setAutoOcr(isAutoOcr());
        zoomController.setAutoTts(isAutoTts());
        zoomController.setTargetFps(getTargetFps());
        zoomController.setUiTheme(getUiTheme());
    }

    public void saveFrom(ZoomController zoomController) {
        setZoomLevel(zoomController.getZoomLevel());
        setZoomMode(zoomController.getMode());
        setLensWidth(zoomController.getLensWidth());
        setLensHeight(zoomController.getLensHeight());
        setShowFps(zoomController.isShowFps());
        setFilterMode(zoomController.getFilterMode());
        setBrightness(zoomController.getBrightness());
        setContrast(zoomController.getContrast());
        setSaturation(zoomController.getSaturation());
        setBorderWidth(zoomController.getBorderWidth());
        setLensShape(zoomController.getLensShape());
        setLensCornerRadius(zoomController.getLensCornerRadius());
        setBorderColor(zoomController.getBorderColor());
        setBorderOpacity(zoomController.getBorderOpacity());
        setShowCrosshair(zoomController.isShowCrosshair());
        setCrosshairColor(zoomController.getCrosshairColor());
        setSmoothScaling(zoomController.isSmoothScaling());
        setDockPosition(zoomController.getDockPosition());
        setStartWithOs(zoomController.isStartWithOs());
        setAutoOcr(zoomController.isAutoOcr());
        setAutoTts(zoomController.isAutoTts());
        setTargetFps(zoomController.getTargetFps());
        setUiTheme(zoomController.getUiTheme());
        flush();
    }

    /** Force Preferences to disk so reopen Settings sees the new values. */
    public void flush() {
        try {
            prefs.flush();
        } catch (Exception e) {
            System.err.println("ConfigStore.flush failed: " + e.getMessage());
        }
    }

    public void resetHotkeys() {
        for (HotkeyAction action : HotkeyAction.values()) {
            setHotkey(action, HotkeyBinding.defaults(action));
        }
    }
}
