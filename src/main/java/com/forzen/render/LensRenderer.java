package com.forzen.render;

import com.forzen.capture.ScreenCapture;
import com.forzen.core.ZoomController;

import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

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
    private final Pane root;
    private AnimationTimer timer;

    private long lastFrameTime = 0;
    private int frameCount = 0;
    private double currentFps = 0;

    public LensRenderer(ScreenCapture capture, ImagePipeline pipeline, ZoomController zoomController, Pane root) {
        this.capture = capture;
        this.pipeline = pipeline;
        this.zoomController = zoomController;
        this.root = root;

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

        root.getChildren().addAll(imageView, border, crosshairV, crosshairH);
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

        BufferedImage scaled = pipeline.scale(captured, (int) Math.round(lensW), (int) Math.round(lensH));
        if (scaled == null) return;

        javafx.scene.image.Image fxImage = SwingFXUtils.toFXImage(scaled, null);
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

        border.setCenterX(mx);
        border.setCenterY(my);
        border.setRadius(radius + 1);

        crosshairV.setX(mx - 0.5);
        crosshairV.setY(my - 10);
        crosshairH.setX(mx - 10);
        crosshairH.setY(my - 0.5);
    }
}
