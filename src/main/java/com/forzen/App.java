package com.forzen;

import com.forzen.core.ZoomController;
import com.forzen.ui.ForzenOverlay;
import com.forzen.ui.ForzenTray;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class App extends Application {

    private ZoomController zoomController;
    private ForzenOverlay overlay;
    private ForzenTray tray;

    @Override
    public void start(Stage primaryStage) {
        zoomController = new ZoomController();

        Platform.setImplicitExit(false);

        overlay = new ForzenOverlay(zoomController);
        tray = new ForzenTray(zoomController, this);
    }

    @Override
    public void stop() {
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
