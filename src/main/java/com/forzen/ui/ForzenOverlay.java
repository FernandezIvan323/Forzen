package com.forzen.ui;

import com.forzen.capture.ScreenCapture;
import com.forzen.core.ZoomController;
import com.forzen.render.ImagePipeline;
import com.forzen.render.LensRenderer;
import com.forzen.util.ScreenGeometry.MonitorInfo;
import com.forzen.win.WindowNative;
import com.sun.jna.platform.win32.WinDef;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ForzenOverlay extends Stage {

    private final ZoomController zoomController;
    private final LensRenderer lensRenderer;
    private final Pane root;
    private WinDef.HWND hwnd;
    private boolean clickThrough = true;

    public ForzenOverlay(ZoomController zoomController, ScreenCapture screenCapture, ImagePipeline imagePipeline) {
        this.zoomController = zoomController;

        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);
        setTitle("Forzen");
        initOwner(null);

        // Start on primary; LensRenderer will re-home to cursor monitor
        Rectangle2D bounds = Screen.getPrimary().getBounds();
        applyMonitorBounds(bounds);

        root = new Pane();
        root.setStyle("-fx-background-color: transparent;");
        root.setMouseTransparent(true);

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        // Never steal focus from the user's apps
        setOnShown(e -> Platform.runLater(this::initNativeWindow));

        lensRenderer = new LensRenderer(screenCapture, imagePipeline, zoomController, root);
        lensRenderer.setClickThroughHandler(this::applyClickThrough);
        lensRenderer.setMonitorChangeHandler(this::onMonitorChanged);
        lensRenderer.setOverlayVisibilityHandler(this::onVisibility);
        lensRenderer.start();

        zoomController.runningProperty().addListener((obs, o, running) -> {
            if (running) {
                if (!isShowing()) show();
                Platform.runLater(this::initNativeWindow);
            } else {
                // Keep stage but visuals cleared; hide fully to free the desktop
                hide();
            }
        });

        show();
    }

    private void applyMonitorBounds(Rectangle2D bounds) {
        setX(bounds.getMinX());
        setY(bounds.getMinY());
        setWidth(bounds.getWidth());
        setHeight(bounds.getHeight());
        if (getScene() != null) {
            getScene().getRoot().resize(bounds.getWidth(), bounds.getHeight());
        }
    }

    private void onMonitorChanged(MonitorInfo mon) {
        if (mon == null) return;
        Rectangle2D b = mon.fxBounds();
        if (Math.abs(getX() - b.getMinX()) > 0.5
                || Math.abs(getY() - b.getMinY()) > 0.5
                || Math.abs(getWidth() - b.getWidth()) > 0.5
                || Math.abs(getHeight() - b.getHeight()) > 0.5) {
            applyMonitorBounds(b);
            Platform.runLater(this::initNativeWindow);
        }
    }

    private void onVisibility(MonitorInfo mon, boolean running) {
        if (running) {
            if (!isShowing()) {
                show();
                Platform.runLater(this::initNativeWindow);
            }
        } else {
            hide();
        }
    }

    private void initNativeWindow() {
        try {
            hwnd = WindowNative.findHwnd(this);
            if (hwnd == null) {
                // Fallback: find by current title
                hwnd = WindowNative.findHwnd(this);
            }
            if (hwnd != null) {
                WindowNative.excludeFromCapture(hwnd);
                WindowNative.setClickThrough(hwnd, clickThrough);
            }
        } catch (Throwable t) {
            System.err.println("Native overlay setup failed: " + t.getMessage());
        }
    }

    private void applyClickThrough(boolean enabled) {
        this.clickThrough = enabled;
        root.setMouseTransparent(enabled);
        if (hwnd != null) {
            WindowNative.setClickThrough(hwnd, enabled);
        } else {
            Platform.runLater(this::initNativeWindow);
        }
    }

    public LensRenderer getLensRenderer() {
        return lensRenderer;
    }

    public Pane getRoot() {
        return root;
    }

    public void shutdown() {
        lensRenderer.stop();
        hide();
    }
}
