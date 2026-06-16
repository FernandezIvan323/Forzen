package com.forzen.ui;

import com.forzen.capture.ScreenCapture;
import com.forzen.core.ZoomController;
import com.forzen.render.ImagePipeline;

import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ForzenOverlay extends Stage {

    private final ZoomController zoomController;
    private final ScreenCapture screenCapture;
    private final ImagePipeline imagePipeline;
    private final Pane root;
    private final Circle lensCircle;

    public ForzenOverlay(ZoomController zoomController, ScreenCapture screenCapture, ImagePipeline imagePipeline) {
        this.zoomController = zoomController;
        this.screenCapture = screenCapture;
        this.imagePipeline = imagePipeline;

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

        lensCircle = new Circle(150, Color.rgb(0, 255, 65, 0.15));
        lensCircle.setStroke(Color.rgb(0, 255, 65, 0.8));
        lensCircle.setStrokeWidth(2);
        lensCircle.setVisible(true);
        root.getChildren().add(lensCircle);

        scene.setOnMouseMoved(e -> {
            lensCircle.setCenterX(e.getX());
            lensCircle.setCenterY(e.getY());
        });

        show();
    }

    public ScreenCapture getScreenCapture() {
        return screenCapture;
    }

    public ImagePipeline getImagePipeline() {
        return imagePipeline;
    }

    public Pane getRoot() {
        return root;
    }
}
