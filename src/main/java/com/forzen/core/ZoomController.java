package com.forzen.core;

import com.forzen.filter.FilterMode;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

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
    public void setLensWidth(double w) { lensWidth.set(Math.max(100, w)); }

    public double getLensHeight() { return lensHeight.get(); }
    public DoubleProperty lensHeightProperty() { return lensHeight; }
    public void setLensHeight(double h) { lensHeight.set(Math.max(100, h)); }

    public ZoomMode getMode() { return mode.get(); }
    public ObjectProperty<ZoomMode> modeProperty() { return mode; }
    public void setMode(ZoomMode m) { mode.set(m); }

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
}
