package com.forzen;

import com.forzen.capture.CaptureFactory;
import com.forzen.capture.ScreenCapture;
import com.forzen.core.ZoomController;
import com.forzen.render.ImagePipeline;
import com.forzen.ui.ForzenOverlay;
import com.forzen.ui.ForzenTray;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class App extends Application {

    private ZoomController zoomController;
    private ScreenCapture screenCapture;
    private ImagePipeline imagePipeline;
    private ForzenOverlay overlay;
    private ForzenTray tray;

    @Override
    public void start(Stage primaryStage) {
        zoomController = new ZoomController();
        screenCapture = CaptureFactory.create();
        imagePipeline = new ImagePipeline();

        System.out.println("Screen capture: " + screenCapture.getClass().getSimpleName());

        Platform.setImplicitExit(false);

        overlay = new ForzenOverlay(zoomController, screenCapture, imagePipeline);
        tray = new ForzenTray(zoomController, this);
    }

    @Override
    public void stop() {
        if (screenCapture != null) screenCapture.dispose();
        if (imagePipeline != null) imagePipeline.dispose();
        if (tray != null) tray.shutdown();
    }

    public void shutdown() {
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
