package com.forzen.ui;

import com.forzen.capture.ScreenCapture;
import com.forzen.core.ZoomController;
import com.forzen.render.ImagePipeline;
import com.forzen.render.LensRenderer;

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

    public ForzenOverlay(ZoomController zoomController, ScreenCapture screenCapture, ImagePipeline imagePipeline) {
        this.zoomController = zoomController;

        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getBounds();
        setX(bounds.getMinX());
        setY(bounds.getMinY());
        setWidth(bounds.getWidth());
        setHeight(bounds.getHeight());

        root = new Pane();
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        lensRenderer = new LensRenderer(screenCapture, imagePipeline, zoomController, root);
        lensRenderer.start();

        show();
    }

    public LensRenderer getLensRenderer() {
        return lensRenderer;
    }

    public Pane getRoot() {
        return root;
    }
}
