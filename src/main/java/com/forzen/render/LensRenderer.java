package com.forzen.render;

import com.forzen.capture.ScreenCapture;
import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;
import com.forzen.util.ScreenGeometry;
import com.forzen.util.ScreenGeometry.MonitorInfo;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LensRenderer {

    private final ScreenCapture capture;
    private final ImagePipeline pipeline;
    private final ZoomController zoomController;
    private final ImageView imageView;
    private final Circle clipCircle;
    private final Rectangle clipRect;
    private final Circle borderCircle;
    private final Rectangle borderRect;
    private final Rectangle crosshairV;
    private final Rectangle crosshairH;
    private final Rectangle dockPanel;
    private final Label fpsLabel;
    private final Pane root;
    private AnimationTimer timer;

    private long lastFrameTime = 0;
    private long lastRenderNanos = 0;
    private int frameCount = 0;
    private double currentFps = 0;

    private WritableImage writableImage;
    private BufferedImage lastCaptured;
    private MonitorInfo currentMonitor;

    private Consumer<Boolean> clickThroughHandler;
    private BiConsumer<MonitorInfo, Boolean> overlayVisibilityHandler;
    private Consumer<MonitorInfo> monitorChangeHandler;

    public LensRenderer(ScreenCapture capture, ImagePipeline pipeline, ZoomController zoomController, Pane root) {
        this.capture = capture;
        this.pipeline = pipeline;
        this.zoomController = zoomController;
        this.root = root;

        imageView = new ImageView();
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        clipCircle = new Circle();
        clipRect = new Rectangle();
        imageView.setClip(clipCircle);

        borderCircle = new Circle();
        borderCircle.setFill(Color.TRANSPARENT);
        borderCircle.setStroke(Color.rgb(0, 255, 65, 0.85));
        borderCircle.setStrokeWidth(zoomController.getBorderWidth());
        borderCircle.setMouseTransparent(true);

        borderRect = new Rectangle();
        borderRect.setFill(Color.TRANSPARENT);
        borderRect.setStroke(Color.rgb(0, 255, 65, 0.85));
        borderRect.setStrokeWidth(zoomController.getBorderWidth());
        borderRect.setMouseTransparent(true);
        borderRect.setVisible(false);

        crosshairV = new Rectangle(1, 20, Color.rgb(255, 0, 60, 0.7));
        crosshairH = new Rectangle(20, 1, Color.rgb(255, 0, 60, 0.7));
        crosshairV.setMouseTransparent(true);
        crosshairH.setMouseTransparent(true);

        dockPanel = new Rectangle();
        dockPanel.setFill(Color.rgb(13, 13, 13, 0.9));
        dockPanel.setStroke(Color.rgb(0, 255, 65, 0.6));
        dockPanel.setStrokeWidth(1.5);
        dockPanel.setVisible(false);
        dockPanel.setMouseTransparent(true);

        fpsLabel = new Label();
        fpsLabel.setStyle(
                "-fx-text-fill: #00FF41; -fx-font-size: 12px; -fx-font-family: 'Consolas', monospace; "
                        + "-fx-background-color: rgba(13,13,13,0.75); -fx-padding: 4 8;");
        fpsLabel.setVisible(false);
        fpsLabel.setMouseTransparent(true);

        root.getChildren().addAll(dockPanel, imageView, borderCircle, borderRect, crosshairV, crosshairH, fpsLabel);
        root.setMouseTransparent(true);

        zoomController.runningProperty().addListener((obs, o, running) -> {
            if (!running) {
                hideVisuals();
            }
            if (overlayVisibilityHandler != null) {
                overlayVisibilityHandler.accept(currentMonitor, running);
            }
        });

        zoomController.modeProperty().addListener((obs, o, mode) -> applyClickThrough(mode));
    }

    public void setClickThroughHandler(Consumer<Boolean> handler) {
        this.clickThroughHandler = handler;
        applyClickThrough(zoomController.getMode());
    }

    public void setOverlayVisibilityHandler(BiConsumer<MonitorInfo, Boolean> handler) {
        this.overlayVisibilityHandler = handler;
    }

    public void setMonitorChangeHandler(Consumer<MonitorInfo> handler) {
        this.monitorChangeHandler = handler;
    }

    private void applyClickThrough(ZoomMode mode) {
        // FULL blocks clicks; LENS and DOCKED pass through
        boolean clickThrough = mode != ZoomMode.FULL;
        if (clickThroughHandler != null) {
            Platform.runLater(() -> clickThroughHandler.accept(clickThrough));
        }
    }

    public void start() {
        if (timer != null) return;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!zoomController.isRunning()) {
                    return;
                }

                long minInterval = 1_000_000_000L / Math.max(15, zoomController.getTargetFps());
                if (lastRenderNanos > 0 && now - lastRenderNanos < minInterval) {
                    return;
                }
                lastRenderNanos = now;

                render();

                if (zoomController.isShowFps()) {
                    frameCount++;
                    if (now - lastFrameTime >= 1_000_000_000L) {
                        currentFps = frameCount;
                        frameCount = 0;
                        lastFrameTime = now;
                        fpsLabel.setText(String.format("%.0f FPS", currentFps));
                    }
                }
            }
        };
        timer.start();
    }

    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    public BufferedImage getLastCaptured() {
        return lastCaptured;
    }

    public MonitorInfo getCurrentMonitor() {
        return currentMonitor;
    }

    private void hideVisuals() {
        imageView.setImage(null);
        imageView.setVisible(false);
        borderCircle.setVisible(false);
        borderRect.setVisible(false);
        crosshairV.setVisible(false);
        crosshairH.setVisible(false);
        dockPanel.setVisible(false);
        fpsLabel.setVisible(false);
    }

    private void render() {
        pipeline.setFilter(
                zoomController.getFilterMode(),
                zoomController.getBrightness(),
                zoomController.getContrast(),
                zoomController.getSaturation()
        );

        switch (zoomController.getMode()) {
            case LENS -> renderLens();
            case FULL -> renderFull();
            case DOCKED -> renderDocked();
        }
    }

    private void updateMonitor(int mouseX, int mouseY) {
        MonitorInfo m = ScreenGeometry.monitorAtPhysical(mouseX, mouseY);
        boolean changed = currentMonitor == null
                || currentMonitor.physX() != m.physX()
                || currentMonitor.physY() != m.physY()
                || currentMonitor.physW() != m.physW()
                || currentMonitor.physH() != m.physH();
        currentMonitor = m;
        if (changed && monitorChangeHandler != null) {
            monitorChangeHandler.accept(m);
        }
    }

    private void renderLens() {
        double zoom = zoomController.getZoomLevel();
        double lensW = zoomController.getLensWidth();
        double lensH = zoomController.getLensHeight();

        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return;
        Point mouse = pi.getLocation();
        int mx = mouse.x;
        int my = mouse.y;

        updateMonitor(mx, my);
        MonitorInfo mon = currentMonitor;
        if (mon == null) return;

        // Source size in physical pixels
        double srcW = (lensW * mon.scaleX()) / zoom;
        double srcH = (lensH * mon.scaleY()) / zoom;
        java.awt.Rectangle region = ScreenGeometry.captureAround(mx, my, srcW, srcH, mon);
        if (region == null) return;

        BufferedImage captured = capture.capture(region);
        if (captured == null) return;
        lastCaptured = captured;

        int outW = (int) Math.round(lensW);
        int outH = (int) Math.round(lensH);
        BufferedImage processed = pipeline.scale(captured, outW, outH);
        if (processed == null) return;

        showImage(processed, outW, outH);

        double fxMx = mon.toFxX(mx);
        double fxMy = mon.toFxY(my);
        // Convert to scene-local: root is positioned at monitor origin in stage coords
        double localMx = fxMx - mon.fxBounds().getMinX();
        double localMy = fyLocal(fxMy, mon);

        double lensX = localMx - lensW / 2;
        double lensY = localMy - lensH / 2;
        imageView.setVisible(true);
        imageView.setX(lensX);
        imageView.setY(lensY);
        imageView.setFitWidth(lensW);
        imageView.setFitHeight(lensH);

        boolean circular = zoomController.isLensCircular();
        applyClip(circular, lensW, lensH);

        double bw = zoomController.getBorderWidth();
        if (circular) {
            double radius = Math.min(lensW, lensH) / 2;
            borderCircle.setCenterX(localMx);
            borderCircle.setCenterY(localMy);
            borderCircle.setRadius(radius + 1);
            borderCircle.setStrokeWidth(bw);
            borderCircle.setVisible(true);
            borderRect.setVisible(false);
        } else {
            borderRect.setX(lensX - 1);
            borderRect.setY(lensY - 1);
            borderRect.setWidth(lensW + 2);
            borderRect.setHeight(lensH + 2);
            borderRect.setStrokeWidth(bw);
            borderRect.setVisible(true);
            borderCircle.setVisible(false);
        }

        crosshairV.setX(localMx - 0.5);
        crosshairV.setY(localMy - 10);
        crosshairH.setX(localMx - 10);
        crosshairH.setY(localMy - 0.5);
        crosshairV.setVisible(true);
        crosshairH.setVisible(true);
        dockPanel.setVisible(false);

        placeFps(8, 8);
    }

    private double fyLocal(double fxMy, MonitorInfo mon) {
        return fxMy - mon.fxBounds().getMinY();
    }

    private void renderFull() {
        double zoom = zoomController.getZoomLevel();

        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return;
        Point mouse = pi.getLocation();
        int mx = mouse.x;
        int my = mouse.y;

        updateMonitor(mx, my);
        MonitorInfo mon = currentMonitor;
        if (mon == null) return;

        double screenW = mon.fxBounds().getWidth();
        double screenH = mon.fxBounds().getHeight();

        double srcW = mon.physW() / zoom;
        double srcH = mon.physH() / zoom;
        java.awt.Rectangle region = ScreenGeometry.captureAround(mx, my, srcW, srcH, mon);
        if (region == null) return;

        BufferedImage captured = capture.capture(region);
        if (captured == null) return;
        lastCaptured = captured;

        int outW = (int) Math.round(screenW);
        int outH = (int) Math.round(screenH);
        BufferedImage processed = pipeline.scale(captured, outW, outH);
        if (processed == null) return;

        showImage(processed, outW, outH);
        imageView.setVisible(true);
        imageView.setX(0);
        imageView.setY(0);
        imageView.setFitWidth(screenW);
        imageView.setFitHeight(screenH);
        imageView.setClip(null);

        borderCircle.setVisible(false);
        borderRect.setVisible(false);
        crosshairV.setVisible(false);
        crosshairH.setVisible(false);
        dockPanel.setVisible(false);

        placeFps(12, 12);
    }

    private void renderDocked() {
        double zoom = zoomController.getZoomLevel();

        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return;
        Point mouse = pi.getLocation();
        int mx = mouse.x;
        int my = mouse.y;

        updateMonitor(mx, my);
        MonitorInfo mon = currentMonitor;
        if (mon == null) return;

        double screenW = mon.fxBounds().getWidth();
        double screenH = mon.fxBounds().getHeight();
        double dockW = screenW * 0.4;
        double dockH = screenH * 0.3;
        double dockX = screenW - dockW - 10;
        double dockY = screenH - dockH - 10;

        double srcW = (dockW * mon.scaleX()) / zoom;
        double srcH = (dockH * mon.scaleY()) / zoom;
        java.awt.Rectangle region = ScreenGeometry.captureAround(mx, my, srcW, srcH, mon);
        if (region == null) return;

        BufferedImage captured = capture.capture(region);
        if (captured == null) return;
        lastCaptured = captured;

        int outW = (int) Math.round(dockW);
        int outH = (int) Math.round(dockH);
        BufferedImage processed = pipeline.scale(captured, outW, outH);
        if (processed == null) return;

        showImage(processed, outW, outH);
        imageView.setVisible(true);
        imageView.setX(dockX);
        imageView.setY(dockY);
        imageView.setFitWidth(dockW);
        imageView.setFitHeight(dockH);
        imageView.setClip(null);

        dockPanel.setX(dockX - 1);
        dockPanel.setY(dockY - 1);
        dockPanel.setWidth(dockW + 2);
        dockPanel.setHeight(dockH + 2);
        dockPanel.setVisible(true);

        borderCircle.setVisible(false);
        borderRect.setVisible(false);
        crosshairV.setVisible(false);
        crosshairH.setVisible(false);

        placeFps(dockX + 8, dockY + 8);
    }

    private void applyClip(boolean circular, double lensW, double lensH) {
        if (circular) {
            double radius = Math.min(lensW, lensH) / 2;
            clipCircle.setCenterX(lensW / 2);
            clipCircle.setCenterY(lensH / 2);
            clipCircle.setRadius(radius);
            imageView.setClip(clipCircle);
        } else {
            clipRect.setX(0);
            clipRect.setY(0);
            clipRect.setWidth(lensW);
            clipRect.setHeight(lensH);
            imageView.setClip(clipRect);
        }
    }

    private void showImage(BufferedImage processed, int outW, int outH) {
        if (writableImage == null
                || (int) writableImage.getWidth() != outW
                || (int) writableImage.getHeight() != outH) {
            writableImage = new WritableImage(outW, outH);
        }
        SwingFXUtils.toFXImage(processed, writableImage);
        imageView.setImage(writableImage);
    }

    private void placeFps(double x, double y) {
        boolean show = zoomController.isShowFps();
        fpsLabel.setVisible(show);
        if (show) {
            fpsLabel.setLayoutX(x);
            fpsLabel.setLayoutY(y);
            if (fpsLabel.getText() == null || fpsLabel.getText().isBlank()) {
                fpsLabel.setText("-- FPS");
            }
        }
    }
}
