package com.forzen.render;

import com.forzen.capture.ScreenCapture;
import com.forzen.core.DockPosition;
import com.forzen.core.LensShape;
import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;
import com.forzen.util.ScreenGeometry;
import com.forzen.util.ScreenGeometry.MonitorInfo;
import com.forzen.win.NativeScreen;
import com.forzen.win.NativeScreen.PhysPoint;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LensRenderer {

    private final ScreenCapture capture;
    private final ImagePipeline pipeline;
    private final ZoomController zoomController;

    private final Pane lensHost;
    private final Canvas lensCanvas;
    private final Circle borderCircle;
    private final Rectangle borderRect;
    private final Rectangle crosshairV;
    private final Rectangle crosshairH;

    private final ImageView fullImageView;
    private final Rectangle dockPanel;
    private final Label fpsLabel;
    private final Pane root;
    private AnimationTimer timer;

    private long lastFrameTime = 0;
    private long lastRenderNanos = 0;
    private int frameCount = 0;
    private double currentFps = 0;
    private boolean loggedGeometry;
    private long lastDockLogNanos = 0;

    private WritableImage lensWritable;
    private WritableImage fullWritable;
    private BufferedImage lastCaptured;
    private MonitorInfo currentMonitor;

    private Consumer<Boolean> clickThroughHandler;
    private Runnable clickThroughReassertHandler;
    private BiConsumer<MonitorInfo, Boolean> overlayVisibilityHandler;
    private Consumer<MonitorInfo> monitorChangeHandler;
    private Supplier<double[]> stageSizeSupplier;
    private Consumer<Rectangle2D> stageBoundsHandler;

    public LensRenderer(ScreenCapture capture, ImagePipeline pipeline, ZoomController zoomController, Pane root) {
        this.capture = capture;
        this.pipeline = pipeline;
        this.zoomController = zoomController;
        this.root = root;

        lensHost = new Pane();
        lensHost.setMouseTransparent(true);
        lensHost.setVisible(false);

        lensCanvas = new Canvas(300, 300);
        lensCanvas.setMouseTransparent(true);

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

        lensHost.getChildren().addAll(lensCanvas, borderCircle, borderRect, crosshairV, crosshairH);

        fullImageView = new ImageView();
        fullImageView.setPreserveRatio(false);
        fullImageView.setSmooth(true);
        fullImageView.setMouseTransparent(true);
        fullImageView.setVisible(false);

        dockPanel = new Rectangle();
        dockPanel.setFill(Color.rgb(13, 13, 13, 0.55));
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

        root.getChildren().addAll(dockPanel, fullImageView, lensHost, fpsLabel);
        root.setMouseTransparent(true);

        zoomController.runningProperty().addListener((obs, o, running) -> {
            if (!running) {
                hideVisuals();
            }
            if (overlayVisibilityHandler != null) {
                overlayVisibilityHandler.accept(currentMonitor, running);
            }
        });

        zoomController.modeProperty().addListener((obs, o, mode) -> {
            // Always wipe every mode's visuals so LENS + DOCKED never stack
            hideAllModeVisuals();
            applyClickThrough(mode);
        });
    }

    public void setClickThroughHandler(Consumer<Boolean> handler) {
        this.clickThroughHandler = handler;
        applyClickThrough(zoomController.getMode());
    }

    public void setClickThroughReassertHandler(Runnable handler) {
        this.clickThroughReassertHandler = handler;
    }

    public void setOverlayVisibilityHandler(BiConsumer<MonitorInfo, Boolean> handler) {
        this.overlayVisibilityHandler = handler;
    }

    public void setMonitorChangeHandler(Consumer<MonitorInfo> handler) {
        this.monitorChangeHandler = handler;
    }

    public void setStageSizeSupplier(Supplier<double[]> supplier) {
        this.stageSizeSupplier = supplier;
    }

    /** Screen-space FX bounds the overlay stage should occupy this frame. */
    public void setStageBoundsHandler(Consumer<Rectangle2D> handler) {
        this.stageBoundsHandler = handler;
    }

    private void publishStageBounds(double screenX, double screenY, double w, double h) {
        if (stageBoundsHandler == null) return;
        if (w < 2 || h < 2) return;
        stageBoundsHandler.accept(new Rectangle2D(screenX, screenY, w, h));
    }

    private void applyClickThrough(ZoomMode mode) {
        // LENS + DOCKED always pass clicks through to the desktop
        if (clickThroughHandler != null) {
            Platform.runLater(() -> clickThroughHandler.accept(true));
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

                long minInterval = 1_000_000_000L / Math.max(15, zoomController.effectiveFps());
                if (lastRenderNanos > 0 && now - lastRenderNanos < minInterval) {
                    return;
                }
                lastRenderNanos = now;

                if (clickThroughReassertHandler != null) {
                    clickThroughReassertHandler.run();
                }

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
        hideAllModeVisuals();
        fpsLabel.setVisible(false);
    }

    /** Hard reset so only one mode paints on the next frame. */
    private void hideAllModeVisuals() {
        lensHost.setVisible(false);
        clearLensCanvas();
        borderCircle.setVisible(false);
        borderRect.setVisible(false);
        crosshairV.setVisible(false);
        crosshairH.setVisible(false);
        fullImageView.setImage(null);
        fullImageView.setVisible(false);
        fullImageView.setClip(null);
        dockPanel.setVisible(false);
    }

    private void clearLensCanvas() {
        GraphicsContext gc = lensCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, lensCanvas.getWidth(), lensCanvas.getHeight());
    }

    private void render() {
        pipeline.setFilter(
                zoomController.getFilterMode(),
                zoomController.getBrightness(),
                zoomController.getContrast(),
                zoomController.getSaturation()
        );
        pipeline.setSmoothScaling(zoomController.isSmoothScaling());
        fullImageView.setSmooth(zoomController.isSmoothScaling());

        switch (zoomController.getMode()) {
            case DOCKED -> renderDocked();
            default -> renderLens();
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
        if (!loggedGeometry) {
            loggedGeometry = true;
            System.out.printf(
                    "Geometry: phys=%dx%d@%d,%d  fx=%.0fx%.0f  visual=%.0fx%.0f  scale=%.3f  cursor=%d,%d%n",
                    m.physW(), m.physH(), m.physX(), m.physY(),
                    m.fxBounds().getWidth(), m.fxBounds().getHeight(),
                    m.fxVisualBounds().getWidth(), m.fxVisualBounds().getHeight(),
                    m.scaleX(), mouseX, mouseY
            );
        }
    }

    private Color parseHex(String hex, double opacity01, Color fallback) {
        try {
            String h = hex == null ? "" : hex.trim();
            if (!h.startsWith("#")) h = "#" + h;
            Color base = Color.web(h);
            return new Color(base.getRed(), base.getGreen(), base.getBlue(),
                    Math.max(0, Math.min(1, opacity01)));
        } catch (Exception e) {
            return fallback;
        }
    }

    private void applyBorderStyle(double bw) {
        double opacity = zoomController.getBorderOpacity() / 100.0;
        Color stroke = parseHex(zoomController.getBorderColor(), opacity, Color.rgb(0, 255, 65, 0.85));
        borderCircle.setStroke(stroke);
        borderCircle.setStrokeWidth(bw);
        borderRect.setStroke(stroke);
        borderRect.setStrokeWidth(bw);
    }

    private void applyCrosshairStyle() {
        boolean show = zoomController.isShowCrosshair();
        if (!show) {
            crosshairV.setVisible(false);
            crosshairH.setVisible(false);
            return;
        }
        Color c = parseHex(zoomController.getCrosshairColor(), 0.85, Color.rgb(255, 0, 60, 0.7));
        crosshairV.setFill(c);
        crosshairH.setFill(c);
        crosshairV.setVisible(true);
        crosshairH.setVisible(true);
    }

    private void renderLens() {
        fullImageView.setVisible(false);
        fullImageView.setClip(null);
        dockPanel.setVisible(false);

        double zoom = zoomController.getZoomLevel();
        double lensW = zoomController.getLensWidth();
        double lensH = zoomController.getLensHeight();

        PhysPoint cursor = NativeScreen.cursorPos();
        int mx = cursor.x();
        int my = cursor.y();

        updateMonitor(mx, my);
        MonitorInfo mon = currentMonitor;
        if (mon == null) return;

        // Full-monitor stage; move lensHost with layoutX/Y (reliable). Do NOT move
        // the Windows window every frame — that left the glass stuck while capture followed the mouse.
        double monW = mon.fxBounds().getWidth();
        double monH = mon.fxBounds().getHeight();
        if (monW < 2) monW = 800;
        if (monH < 2) monH = 600;
        publishStageBounds(mon.fxBounds().getMinX(), mon.fxBounds().getMinY(), monW, monH);

        // Capture FOV from DPI-aware scale (not a wrong phys/fx ratio)
        double[] srcSize = ScreenGeometry.captureSizePhys(lensW, lensH, zoom, mon);
        java.awt.Rectangle region = ScreenGeometry.captureViewport(mx, my, srcSize[0], srcSize[1], mon);
        if (region == null) return;

        BufferedImage captured = capture.capture(region);
        if (captured == null) return;
        lastCaptured = captured;

        // Where the real cursor sits inside the buffer (≠ center after edge clamp)
        int[] hot = ScreenGeometry.cursorInViewport(mx, my, region);
        int hotX = hot[0];
        int hotY = hot[1];

        // Apply filters at capture resolution; hot-point draw scales into the lens
        BufferedImage processed = pipeline.scale(captured, captured.getWidth(), captured.getHeight());
        if (processed == null) processed = captured;

        double localMx = mon.toLocalX(mx);
        double localMy = mon.toLocalY(my);
        double lensX = localMx - lensW / 2.0;
        double lensY = localMy - lensH / 2.0;
        lensX = clamp(lensX, 0, Math.max(0, monW - lensW));
        lensY = clamp(lensY, 0, Math.max(0, monH - lensH));

        if (Math.abs(lensCanvas.getWidth() - lensW) > 0.5 || Math.abs(lensCanvas.getHeight() - lensH) > 0.5) {
            lensCanvas.setWidth(lensW);
            lensCanvas.setHeight(lensH);
        }
        lensHost.setPrefSize(lensW, lensH);
        lensHost.setMinSize(lensW, lensH);
        lensHost.setMaxSize(lensW, lensH);
        lensHost.resize(lensW, lensH);
        lensHost.setLayoutX(lensX);
        lensHost.setLayoutY(lensY);
        lensHost.setVisible(true);

        LensShape shape = zoomController.getLensShape();
        drawLensCanvasHot(processed, lensW, lensH, hotX, hotY, shape);

        double bw = zoomController.getBorderWidth();
        applyBorderStyle(bw);
        double cx = lensW / 2.0;
        double cy = lensH / 2.0;
        if (shape == LensShape.CIRCLE) {
            double radius = Math.min(lensW, lensH) / 2.0;
            borderCircle.setCenterX(cx);
            borderCircle.setCenterY(cy);
            borderCircle.setRadius(radius);
            borderCircle.setVisible(true);
            borderRect.setVisible(false);
        } else {
            borderRect.setX(0);
            borderRect.setY(0);
            borderRect.setWidth(lensW);
            borderRect.setHeight(lensH);
            double r = shape == LensShape.ROUNDED ? zoomController.getLensCornerRadius() : 0;
            borderRect.setArcWidth(r * 2);
            borderRect.setArcHeight(r * 2);
            borderRect.setVisible(true);
            borderCircle.setVisible(false);
        }

        crosshairV.setX(cx - 0.5);
        crosshairV.setY(cy - 10);
        crosshairH.setX(cx - 10);
        crosshairH.setY(cy - 0.5);
        applyCrosshairStyle();

        placeFps(8, 8);
    }

    private void renderDocked() {
        lensHost.setVisible(false);

        double zoom = Math.max(1.0, zoomController.getZoomLevel());

        PhysPoint cursor = NativeScreen.cursorPos();
        int mx = cursor.x();
        int my = cursor.y();

        updateMonitor(mx, my);
        MonitorInfo mon = currentMonitor;
        if (mon == null) return;

        // Work area (above taskbar) for dock placement
        Rectangle2D full = mon.fxBounds();
        Rectangle2D work = mon.fxVisualBounds();
        if (work == null || work.getWidth() < 2 || work.getHeight() < 2) {
            work = full;
        }
        double workW = work.getWidth() >= 2 ? work.getWidth() : 800;
        double workH = work.getHeight() >= 2 ? work.getHeight() : 600;

        double dockW = Math.max(180, workW * 0.38);
        double dockH = Math.max(140, workH * 0.28);
        double margin = 16;
        double[] pos = dockOrigin(zoomController.getDockPosition(), workW, workH, dockW, dockH, margin);
        // Screen coords of dock (work area origin + local offset)
        double stageX = work.getMinX() + pos[0];
        double stageY = work.getMinY() + pos[1];

        // CAPTURE FIRST while dock chrome is hidden — avoids black frames from self-cover
        // (full-screen glass + BitBlt was returning black for DOCKED).
        fullImageView.setVisible(false);
        dockPanel.setVisible(false);

        double[] srcSize = ScreenGeometry.captureSizePhys(dockW, dockH, zoom, mon);
        double srcW = Math.max(32, Math.min(srcSize[0], mon.physW()));
        double srcH = Math.max(32, Math.min(srcSize[1], mon.physH()));

        java.awt.Rectangle region = ScreenGeometry.captureViewport(mx, my, srcW, srcH, mon);
        if (region == null || region.width < 1 || region.height < 1) {
            logDockOnce("null/empty region");
            return;
        }

        BufferedImage captured = capture.capture(region);
        if (captured == null) {
            logDockOnce("capture null for " + region);
            return;
        }
        lastCaptured = captured;

        int[] hot = ScreenGeometry.cursorInViewport(mx, my, region);
        // Scale capture so the hot pixel lands at the center of the dock panel
        int outW = Math.max(1, (int) Math.round(dockW));
        int outH = Math.max(1, (int) Math.round(dockH));
        BufferedImage processed = scaleHotToCenter(captured, hot[0], hot[1], outW, outH);
        if (processed == null) {
            logDockOnce("pipeline null");
            return;
        }

        // Small stage = only the dock panel (does not cover the whole screen → clean capture)
        publishStageBounds(stageX, stageY, dockW, dockH);

        showFullImage(processed, outW, outH);
        // Content fills the small stage at (0,0)
        placeImageView(0, 0, dockW, dockH);
        fullImageView.setClip(null);
        fullImageView.setVisible(true);
        fullImageView.toFront();

        Color border = parseHex(zoomController.getBorderColor(), 0.85, Color.rgb(52, 211, 153, 0.9));
        dockPanel.setStroke(border);
        dockPanel.setFill(Color.rgb(5, 5, 6, 0.4));
        dockPanel.setX(0);
        dockPanel.setY(0);
        dockPanel.setWidth(dockW);
        dockPanel.setHeight(dockH);
        dockPanel.setVisible(true);
        // Image above panel background
        fullImageView.toFront();
        fpsLabel.toFront();

        placeFps(8, 8);
    }

    private void logDockOnce(String msg) {
        long now = System.nanoTime();
        if (now - lastDockLogNanos > 2_000_000_000L) {
            lastDockLogNanos = now;
            System.err.println("Docked: " + msg);
        }
    }

    private void placeImageView(double x, double y, double w, double h) {
        fullImageView.setVisible(true);
        // Use both ImageView x/y and layout for Pane reliability
        fullImageView.setX(0);
        fullImageView.setY(0);
        fullImageView.setLayoutX(x);
        fullImageView.setLayoutY(y);
        fullImageView.setFitWidth(w);
        fullImageView.setFitHeight(h);
        fullImageView.setPreserveRatio(false);
        fullImageView.setSmooth(zoomController.isSmoothScaling());
    }

    private static double[] dockOrigin(DockPosition pos, double screenW, double screenH,
                                       double dockW, double dockH, double margin) {
        // Default away from system tray (bottom-right on most setups)
        if (pos == null) pos = DockPosition.TOP_RIGHT;
        return switch (pos) {
            case TOP_LEFT -> new double[]{margin, margin};
            case TOP_RIGHT -> new double[]{screenW - dockW - margin, margin};
            case BOTTOM_LEFT -> new double[]{margin, screenH - dockH - margin};
            case CENTER -> new double[]{(screenW - dockW) / 2.0, (screenH - dockH) / 2.0};
            case BOTTOM_RIGHT -> new double[]{screenW - dockW - margin, screenH - dockH - margin};
        };
    }

    /**
     * Draw capture into the lens so that pixel (hotX, hotY) of the source lands at the
     * geometric center of the lens (under the crosshair). Fixes edge clamp + DPI
     * mismatch that looked like "wrong place" especially over browsers.
     */
    private void drawLensCanvasHot(BufferedImage processed, double lensW, double lensH,
                                   int hotX, int hotY, LensShape shape) {
        int srcW = processed.getWidth();
        int srcH = processed.getHeight();
        if (lensWritable == null
                || (int) lensWritable.getWidth() != srcW
                || (int) lensWritable.getHeight() != srcH) {
            lensWritable = new WritableImage(srcW, srcH);
        }
        SwingFXUtils.toFXImage(processed, lensWritable);

        // Uniform scale: full FOV width → lens width (hot point stays consistent)
        double scale = lensW / (double) Math.max(1, srcW);
        double destW = srcW * scale;
        double destH = srcH * scale;
        double destX = lensW / 2.0 - hotX * scale;
        double destY = lensH / 2.0 - hotY * scale;

        GraphicsContext gc = lensCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, lensW, lensH);
        gc.save();
        if (shape == LensShape.CIRCLE) {
            double radius = Math.min(lensW, lensH) / 2.0;
            gc.beginPath();
            gc.arc(lensW / 2.0, lensH / 2.0, radius, radius, 0, 360);
            gc.closePath();
            gc.clip();
        } else if (shape == LensShape.ROUNDED) {
            double r = Math.min(zoomController.getLensCornerRadius(), Math.min(lensW, lensH) / 2.0);
            gc.beginPath();
            roundRectPath(gc, 0, 0, lensW, lensH, r);
            gc.closePath();
            gc.clip();
        }
        gc.setImageSmoothing(zoomController.isSmoothScaling());
        gc.drawImage(lensWritable, destX, destY, destW, destH);
        gc.restore();
    }

    /**
     * Produce a dock-sized image where (hotX,hotY) of the capture is at the center.
     */
    private BufferedImage scaleHotToCenter(BufferedImage captured, int hotX, int hotY,
                                           int outW, int outH) {
        if (captured == null) return null;
        int srcW = captured.getWidth();
        int srcH = captured.getHeight();
        if (srcW < 1 || srcH < 1) return null;

        // First apply color filters at source size
        BufferedImage filtered = pipeline.scale(captured, srcW, srcH);
        if (filtered == null) filtered = captured;

        BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = out.createGraphics();
        if (zoomController.isSmoothScaling()) {
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        } else {
            g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
        g.setColor(java.awt.Color.BLACK);
        g.fillRect(0, 0, outW, outH);

        double scale = outW / (double) Math.max(1, srcW);
        double destW = srcW * scale;
        double destH = srcH * scale;
        double destX = outW / 2.0 - hotX * scale;
        double destY = outH / 2.0 - hotY * scale;
        g.drawImage(filtered, (int) Math.round(destX), (int) Math.round(destY),
                (int) Math.round(destW), (int) Math.round(destH), null);
        g.dispose();
        return out;
    }

    private static void roundRectPath(GraphicsContext gc, double x, double y, double w, double h, double r) {
        if (r <= 0) {
            gc.rect(x, y, w, h);
            return;
        }
        gc.moveTo(x + r, y);
        gc.lineTo(x + w - r, y);
        gc.arcTo(x + w, y, x + w, y + r, r);
        gc.lineTo(x + w, y + h - r);
        gc.arcTo(x + w, y + h, x + w - r, y + h, r);
        gc.lineTo(x + r, y + h);
        gc.arcTo(x, y + h, x, y + h - r, r);
        gc.lineTo(x, y + r);
        gc.arcTo(x, y, x + r, y, r);
    }

    private void showFullImage(BufferedImage processed, int outW, int outH) {
        if (fullWritable == null
                || (int) fullWritable.getWidth() != outW
                || (int) fullWritable.getHeight() != outH) {
            fullWritable = new WritableImage(outW, outH);
        }
        SwingFXUtils.toFXImage(processed, fullWritable);
        fullImageView.setImage(fullWritable);
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

    private static double clamp(double v, double min, double max) {
        if (max < min) return min;
        return Math.max(min, Math.min(v, max));
    }
}
