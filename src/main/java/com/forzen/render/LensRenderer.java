package com.forzen.render;

import com.forzen.capture.ScreenCapture;
import com.forzen.core.ZoomController;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class LensRenderer {

    private final ScreenCapture capture;
    private final ImagePipeline pipeline;
    private final ZoomController zoomController;
    private final ImageView imageView;
    private final Circle clipShape;
    private final Circle border;
    private final Rectangle crosshairV;
    private final Rectangle crosshairH;
    private final Rectangle dockPanel;
    private final Pane root;
    private AnimationTimer timer;

    private long lastFrameTime = 0;
    private int frameCount = 0;
    private double currentFps = 0;
    private double screenW;
    private double screenH;

    public LensRenderer(ScreenCapture capture, ImagePipeline pipeline, ZoomController zoomController, Pane root) {
        this.capture = capture;
        this.pipeline = pipeline;
        this.zoomController = zoomController;
        this.root = root;

        Rectangle2D bounds = Screen.getPrimary().getBounds();
        screenW = bounds.getWidth();
        screenH = bounds.getHeight();

        imageView = new ImageView();
        imageView.setPreserveRatio(false);

        clipShape = new Circle();
        imageView.setClip(clipShape);

        border = new Circle();
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.rgb(0, 255, 65, 0.85));
        border.setStrokeWidth(2.5);

        crosshairV = new Rectangle(1, 20, Color.rgb(255, 0, 60, 0.7));
        crosshairH = new Rectangle(20, 1, Color.rgb(255, 0, 60, 0.7));

        dockPanel = new Rectangle();
        dockPanel.setFill(Color.rgb(13, 13, 13, 0.9));
        dockPanel.setStroke(Color.rgb(0, 255, 65, 0.6));
        dockPanel.setStrokeWidth(1.5);
        dockPanel.setVisible(false);

        root.getChildren().addAll(imageView, border, crosshairV, crosshairH, dockPanel);
    }

    public void start() {
        if (timer != null) return;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!zoomController.isRunning()) return;
                render();
                if (zoomController.isShowFps()) {
                    frameCount++;
                    if (now - lastFrameTime >= 1_000_000_000) {
                        currentFps = frameCount;
                        frameCount = 0;
                        lastFrameTime = now;
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

    private void renderLens() {
        double zoom = zoomController.getZoomLevel();
        double lensW = zoomController.getLensWidth();
        double lensH = zoomController.getLensHeight();

        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return;
        Point mouse = pi.getLocation();
        double mx = mouse.getX();
        double my = mouse.getY();

        double srcW = lensW / zoom;
        double srcH = lensH / zoom;
        int srcX = (int) Math.round(mx - srcW / 2);
        int srcY = (int) Math.round(my - srcH / 2);

        java.awt.Rectangle screenRect = new java.awt.Rectangle(srcX, srcY, (int) Math.ceil(srcW), (int) Math.ceil(srcH));
        BufferedImage captured = capture.capture(screenRect);
        if (captured == null) return;

        BufferedImage processed = pipeline.scale(captured, (int) Math.round(lensW), (int) Math.round(lensH));
        if (processed == null) return;

        javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(processed, null);
        imageView.setImage(fxImage);

        double lensX = mx - lensW / 2;
        double lensY = my - lensH / 2;
        imageView.setX(lensX);
        imageView.setY(lensY);
        imageView.setFitWidth(lensW);
        imageView.setFitHeight(lensH);

        double radius = Math.min(lensW, lensH) / 2;
        clipShape.setCenterX(lensW / 2);
        clipShape.setCenterY(lensH / 2);
        clipShape.setRadius(radius);
        imageView.setClip(clipShape);

        border.setCenterX(mx);
        border.setCenterY(my);
        border.setRadius(radius + 1);
        border.setVisible(true);

        crosshairV.setX(mx - 0.5);
        crosshairV.setY(my - 10);
        crosshairH.setX(mx - 10);
        crosshairH.setY(my - 0.5);
        crosshairV.setVisible(true);
        crosshairH.setVisible(true);

        dockPanel.setVisible(false);
    }

    private void renderFull() {
        double zoom = zoomController.getZoomLevel();

        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return;
        Point mouse = pi.getLocation();
        double mx = mouse.getX();
        double my = mouse.getY();

        double srcW = screenW / zoom;
        double srcH = screenH / zoom;
        int srcX = (int) Math.round(mx - srcW / 2);
        int srcY = (int) Math.round(my - srcH / 2);

        java.awt.Rectangle screenRect = new java.awt.Rectangle(srcX, srcY, (int) Math.ceil(srcW), (int) Math.ceil(srcH));
        BufferedImage captured = capture.capture(screenRect);
        if (captured == null) return;

        BufferedImage processed = pipeline.scale(captured, (int) Math.round(screenW), (int) Math.round(screenH));
        if (processed == null) return;

        javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(processed, null);
        imageView.setImage(fxImage);
        imageView.setX(0);
        imageView.setY(0);
        imageView.setFitWidth(screenW);
        imageView.setFitHeight(screenH);
        imageView.setClip(null);

        border.setVisible(false);
        crosshairV.setVisible(false);
        crosshairH.setVisible(false);
        dockPanel.setVisible(false);
    }

    private void renderDocked() {
        double zoom = zoomController.getZoomLevel();
        double dockW = screenW * 0.4;
        double dockH = screenH * 0.3;
        double dockX = screenW - dockW - 10;
        double dockY = screenH - dockH - 10;
        double cx = dockX + dockW / 2;
        double cy = dockY + dockH / 2;

        PointerInfo pi = MouseInfo.getPointerInfo();
        if (pi == null) return;
        Point mouse = pi.getLocation();
        double mx = mouse.getX();
        double my = mouse.getY();

        double srcW = dockW / zoom;
        double srcH = dockH / zoom;
        int srcX = (int) Math.round(mx - srcW / 2);
        int srcY = (int) Math.round(my - srcH / 2);

        java.awt.Rectangle screenRect = new java.awt.Rectangle(srcX, srcY, (int) Math.ceil(srcW), (int) Math.ceil(srcH));
        BufferedImage captured = capture.capture(screenRect);
        if (captured == null) return;

        BufferedImage processed = pipeline.scale(captured, (int) Math.round(dockW), (int) Math.round(dockH));
        if (processed == null) return;

        javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(processed, null);
        imageView.setImage(fxImage);
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

        border.setVisible(false);
        crosshairV.setVisible(false);
        crosshairH.setVisible(false);
    }
}
