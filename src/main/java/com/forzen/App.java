package com.forzen;

import com.forzen.capture.CaptureFactory;
import com.forzen.capture.ScreenCapture;
import com.forzen.config.ConfigStore;
import com.forzen.core.ZoomController;
import com.forzen.input.HotkeyManager;
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
    private HotkeyManager hotkeyManager;
    private ConfigStore configStore;
    private ForzenOverlay overlay;
    private ForzenTray tray;

    @Override
    public void start(Stage primaryStage) {
        configStore = new ConfigStore();

        zoomController = new ZoomController();
        configStore.applyTo(zoomController);

        screenCapture = CaptureFactory.create();
        imagePipeline = new ImagePipeline();

        System.out.println("Screen capture: " + screenCapture.getClass().getSimpleName());

        Platform.setImplicitExit(false);

        overlay = new ForzenOverlay(zoomController, screenCapture, imagePipeline);
        tray = new ForzenTray(zoomController, this);

        hotkeyManager = new HotkeyManager(zoomController, this);
        hotkeyManager.register();

        System.out.println("Hotkeys: Ctrl+Alt+Up/Down = zoom, Z = pause, M = mode, , = settings, X = exit");
    }

    @Override
    public void stop() {
        if (configStore != null) configStore.saveFrom(zoomController);
        if (hotkeyManager != null) hotkeyManager.unregister();
        if (screenCapture != null) screenCapture.dispose();
        if (imagePipeline != null) imagePipeline.dispose();
        if (tray != null) tray.shutdown();
    }

    public void shutdown() {
        if (configStore != null) configStore.saveFrom(zoomController);
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
