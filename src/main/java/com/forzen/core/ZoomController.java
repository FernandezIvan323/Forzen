package com.forzen.core;

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

    public ZoomController() {
        this.zoomLevel = new SimpleDoubleProperty(2.0);
        this.lensWidth = new SimpleDoubleProperty(300);
        this.lensHeight = new SimpleDoubleProperty(300);
        this.mode = new SimpleObjectProperty<>(ZoomMode.LENS);
        this.running = new SimpleBooleanProperty(true);
        this.showFps = new SimpleBooleanProperty(false);
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
}
