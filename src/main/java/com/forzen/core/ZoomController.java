package com.forzen.core;

import com.forzen.filter.FilterMode;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ZoomController {

    private final DoubleProperty zoomLevel;
    private final DoubleProperty lensWidth;
    private final DoubleProperty lensHeight;
    private final ObjectProperty<ZoomMode> mode;
    private final BooleanProperty running;
    private final BooleanProperty showFps;
    private final ObjectProperty<FilterMode> filterMode;
    private final DoubleProperty brightness;
    private final DoubleProperty contrast;
    private final DoubleProperty saturation;
    private final DoubleProperty borderWidth;
    private final ObjectProperty<LensShape> lensShape;
    private final DoubleProperty lensCornerRadius;
    private final StringProperty borderColor;
    private final DoubleProperty borderOpacity;
    private final BooleanProperty showCrosshair;
    private final StringProperty crosshairColor;
    private final BooleanProperty smoothScaling;
    private final ObjectProperty<DockPosition> dockPosition;
    private final BooleanProperty startWithOs;
    private final BooleanProperty autoOcr;
    private final BooleanProperty autoTts;
    private final IntegerProperty targetFps;
    private final StringProperty uiTheme;

    public ZoomController() {
        this.zoomLevel = new SimpleDoubleProperty(2.0);
        this.lensWidth = new SimpleDoubleProperty(300);
        this.lensHeight = new SimpleDoubleProperty(300);
        this.mode = new SimpleObjectProperty<>(ZoomMode.LENS);
        this.running = new SimpleBooleanProperty(true);
        this.showFps = new SimpleBooleanProperty(false);
        this.filterMode = new SimpleObjectProperty<>(FilterMode.NONE);
        this.brightness = new SimpleDoubleProperty(100);
        this.contrast = new SimpleDoubleProperty(100);
        this.saturation = new SimpleDoubleProperty(100);
        this.borderWidth = new SimpleDoubleProperty(2.5);
        this.lensShape = new SimpleObjectProperty<>(LensShape.CIRCLE);
        this.lensCornerRadius = new SimpleDoubleProperty(18);
        this.borderColor = new SimpleStringProperty("#00FF41");
        this.borderOpacity = new SimpleDoubleProperty(85);
        this.showCrosshair = new SimpleBooleanProperty(true);
        this.crosshairColor = new SimpleStringProperty("#FF003C");
        this.smoothScaling = new SimpleBooleanProperty(true);
        // TOP_RIGHT avoids the system-tray overflow (bottom-right on most PCs)
        this.dockPosition = new SimpleObjectProperty<>(DockPosition.TOP_RIGHT);
        this.startWithOs = new SimpleBooleanProperty(false);
        this.autoOcr = new SimpleBooleanProperty(false);
        this.autoTts = new SimpleBooleanProperty(false);
        this.targetFps = new SimpleIntegerProperty(60);
        this.uiTheme = new SimpleStringProperty("forzen_dark");
    }

    public double getZoomLevel() { return zoomLevel.get(); }
    public DoubleProperty zoomLevelProperty() { return zoomLevel; }
    public void setZoomLevel(double level) {
        zoomLevel.set(Math.max(1.0, Math.min(8.0, level)));
    }

    public void zoomIn() { setZoomLevel(getZoomLevel() + 0.5); }
    public void zoomOut() { setZoomLevel(getZoomLevel() - 0.5); }

    public double getLensWidth() { return lensWidth.get(); }
    public DoubleProperty lensWidthProperty() { return lensWidth; }
    public void setLensWidth(double w) { lensWidth.set(Math.max(100, Math.min(1200, w))); }

    public double getLensHeight() { return lensHeight.get(); }
    public DoubleProperty lensHeightProperty() { return lensHeight; }
    public void setLensHeight(double h) { lensHeight.set(Math.max(100, Math.min(1200, h))); }

    public ZoomMode getMode() { return mode.get(); }
    public ObjectProperty<ZoomMode> modeProperty() { return mode; }
    public void setMode(ZoomMode m) {
        mode.set(m == null ? ZoomMode.LENS : m);
    }

    public boolean isRunning() { return running.get(); }
    public BooleanProperty runningProperty() { return running; }
    public void setRunning(boolean r) { running.set(r); }

    public void toggleRunning() { setRunning(!isRunning()); }

    public boolean isShowFps() { return showFps.get(); }
    public BooleanProperty showFpsProperty() { return showFps; }
    public void setShowFps(boolean v) { showFps.set(v); }

    public FilterMode getFilterMode() { return filterMode.get(); }
    public ObjectProperty<FilterMode> filterModeProperty() { return filterMode; }
    public void setFilterMode(FilterMode m) { filterMode.set(m); }

    public double getBrightness() { return brightness.get(); }
    public DoubleProperty brightnessProperty() { return brightness; }
    public void setBrightness(double v) { brightness.set(Math.max(0, Math.min(200, v))); }

    public double getContrast() { return contrast.get(); }
    public DoubleProperty contrastProperty() { return contrast; }
    public void setContrast(double v) { contrast.set(Math.max(0, Math.min(200, v))); }

    public double getSaturation() { return saturation.get(); }
    public DoubleProperty saturationProperty() { return saturation; }
    public void setSaturation(double v) { saturation.set(Math.max(0, Math.min(200, v))); }

    public double getBorderWidth() { return borderWidth.get(); }
    public DoubleProperty borderWidthProperty() { return borderWidth; }
    public void setBorderWidth(double v) { borderWidth.set(Math.max(0, Math.min(10, v))); }

    /** Convenience: true when shape is circle. Prefer {@link #getLensShape()}. */
    public boolean isLensCircular() { return getLensShape() == LensShape.CIRCLE; }
    public void setLensCircular(boolean v) {
        if (v) {
            setLensShape(LensShape.CIRCLE);
        } else if (getLensShape() == LensShape.CIRCLE) {
            setLensShape(LensShape.RECTANGLE);
        }
    }

    public LensShape getLensShape() { return lensShape.get(); }
    public ObjectProperty<LensShape> lensShapeProperty() { return lensShape; }
    public void setLensShape(LensShape s) { lensShape.set(s == null ? LensShape.CIRCLE : s); }

    public double getLensCornerRadius() { return lensCornerRadius.get(); }
    public DoubleProperty lensCornerRadiusProperty() { return lensCornerRadius; }
    public void setLensCornerRadius(double v) { lensCornerRadius.set(Math.max(0, Math.min(80, v))); }

    public String getBorderColor() { return borderColor.get(); }
    public StringProperty borderColorProperty() { return borderColor; }
    public void setBorderColor(String hex) {
        borderColor.set(normalizeHex(hex, "#00FF41"));
    }

    public double getBorderOpacity() { return borderOpacity.get(); }
    public DoubleProperty borderOpacityProperty() { return borderOpacity; }
    public void setBorderOpacity(double v) { borderOpacity.set(Math.max(0, Math.min(100, v))); }

    public boolean isShowCrosshair() { return showCrosshair.get(); }
    public BooleanProperty showCrosshairProperty() { return showCrosshair; }
    public void setShowCrosshair(boolean v) { showCrosshair.set(v); }

    public String getCrosshairColor() { return crosshairColor.get(); }
    public StringProperty crosshairColorProperty() { return crosshairColor; }
    public void setCrosshairColor(String hex) {
        crosshairColor.set(normalizeHex(hex, "#FF003C"));
    }

    public boolean isSmoothScaling() { return smoothScaling.get(); }
    public BooleanProperty smoothScalingProperty() { return smoothScaling; }
    public void setSmoothScaling(boolean v) { smoothScaling.set(v); }

    public DockPosition getDockPosition() { return dockPosition.get(); }
    public ObjectProperty<DockPosition> dockPositionProperty() { return dockPosition; }
    public void setDockPosition(DockPosition p) {
        dockPosition.set(p == null ? DockPosition.TOP_RIGHT : p);
    }

    public boolean isStartWithOs() { return startWithOs.get(); }
    public BooleanProperty startWithOsProperty() { return startWithOs; }
    public void setStartWithOs(boolean v) { startWithOs.set(v); }

    public boolean isAutoOcr() { return autoOcr.get(); }
    public BooleanProperty autoOcrProperty() { return autoOcr; }
    public void setAutoOcr(boolean v) { autoOcr.set(v); }

    public boolean isAutoTts() { return autoTts.get(); }
    public BooleanProperty autoTtsProperty() { return autoTts; }
    public void setAutoTts(boolean v) { autoTts.set(v); }

    public int getTargetFps() { return targetFps.get(); }
    public IntegerProperty targetFpsProperty() { return targetFps; }
    public void setTargetFps(int v) { targetFps.set(Math.max(15, Math.min(120, v))); }

    public String getUiTheme() { return uiTheme.get(); }
    public StringProperty uiThemeProperty() { return uiTheme; }
    public void setUiTheme(String id) {
        uiTheme.set(id == null || id.isBlank() ? "forzen_dark" : id);
    }

    public int effectiveFps() {
        return getTargetFps();
    }

    public void resetDefaults() {
        setZoomLevel(2.0);
        setMode(ZoomMode.LENS);
        setLensWidth(300);
        setLensHeight(300);
        setShowFps(false);
        setFilterMode(FilterMode.NONE);
        setBrightness(100);
        setContrast(100);
        setSaturation(100);
        setBorderWidth(2.5);
        setLensShape(LensShape.CIRCLE);
        setLensCornerRadius(18);
        setBorderColor("#00FF41");
        setBorderOpacity(85);
        setShowCrosshair(true);
        setCrosshairColor("#FF003C");
        setSmoothScaling(true);
        setDockPosition(DockPosition.TOP_RIGHT);
        setStartWithOs(false);
        setAutoOcr(false);
        setAutoTts(false);
        setTargetFps(60);
        setUiTheme("forzen_dark");
    }

    private static String normalizeHex(String hex, String fallback) {
        if (hex == null || hex.isBlank()) return fallback;
        String h = hex.trim();
        if (!h.startsWith("#")) h = "#" + h;
        if (h.matches("#[0-9A-Fa-f]{6}")) return h.toUpperCase();
        if (h.matches("#[0-9A-Fa-f]{8}")) return h.substring(0, 7).toUpperCase();
        return fallback;
    }
}
