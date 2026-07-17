package com.forzen;

import com.forzen.capture.CaptureFactory;
import com.forzen.capture.ScreenCapture;
import com.forzen.config.ConfigStore;
import com.forzen.core.ZoomController;
import com.forzen.input.HotkeyManager;
import com.forzen.ocr.OcrEngine;
import com.forzen.render.ImagePipeline;
import com.forzen.tts.TtsEngine;
import com.forzen.ui.ForzenOverlay;
import com.forzen.ui.ForzenTray;
import com.forzen.ui.SettingsWindow;
import com.forzen.win.AutostartService;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;

public class App extends Application {

    private ZoomController zoomController;
    private ScreenCapture screenCapture;
    private ImagePipeline imagePipeline;
    private HotkeyManager hotkeyManager;
    private ConfigStore configStore;
    private ForzenOverlay overlay;
    private ForzenTray tray;
    private OcrEngine ocrEngine;
    private TtsEngine ttsEngine;
    private SettingsWindow settingsWindow;

    @Override
    public void start(Stage primaryStage) {
        // Headless primary stage — real UI is overlay + tray
        primaryStage.initStyle(javafx.stage.StageStyle.UTILITY);
        primaryStage.setOpacity(0);
        primaryStage.setWidth(1);
        primaryStage.setHeight(1);
        primaryStage.setX(-10000);
        primaryStage.setY(-10000);
        primaryStage.show();

        configStore = new ConfigStore();

        zoomController = new ZoomController();
        configStore.applyTo(zoomController);

        // If user disabled autostart in prefs, clear a leftover Run key.
        // Enabling only happens from Settings (avoid writing a huge dev classpath on every launch).
        if (!zoomController.isStartWithOs() && AutostartService.isEnabled()) {
            AutostartService.setEnabled(false);
        }
        zoomController.startWithOsProperty().addListener((obs, o, enabled) ->
                AutostartService.setEnabled(enabled));

        screenCapture = CaptureFactory.create();
        imagePipeline = new ImagePipeline();
        ocrEngine = new OcrEngine();
        ttsEngine = new TtsEngine();

        System.out.println("Screen capture: " + screenCapture.getClass().getSimpleName());

        Platform.setImplicitExit(false);

        overlay = new ForzenOverlay(zoomController, screenCapture, imagePipeline);
        tray = new ForzenTray(zoomController, this);

        hotkeyManager = new HotkeyManager(zoomController, this, configStore);
        hotkeyManager.register();

        System.out.println("Forzen ready. Tray icon + hotkeys active.");
    }

    public void openSettings() {
        if (settingsWindow != null && settingsWindow.isShowing()) {
            settingsWindow.toFront();
            settingsWindow.requestFocus();
            return;
        }
        settingsWindow = new SettingsWindow(zoomController, configStore, hotkeyManager, ocrEngine, ttsEngine);
        settingsWindow.setOnHidden(e -> {
            configStore.saveFrom(zoomController);
            if (hotkeyManager != null) hotkeyManager.reloadBindings();
        });
    }

    public void runOcrOnce() {
        if (ocrEngine == null || !ocrEngine.isAvailable()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Forzen OCR");
            alert.setHeaderText("OCR no disponible");
            alert.setContentText(
                    "Instala Tesseract OCR y asegúrate de que Tess4J esté en el classpath.\n"
                            + "Variable opcional: TESSDATA_PREFIX → carpeta tessdata.\n"
                            + (ocrEngine != null && !ocrEngine.getLastError().isBlank()
                            ? "Detalle: " + ocrEngine.getLastError() : "")
            );
            alert.show();
            return;
        }

        BufferedImage img = overlay != null && overlay.getLensRenderer() != null
                ? overlay.getLensRenderer().getLastCaptured()
                : null;
        if (img == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Forzen OCR");
            alert.setHeaderText("Sin imagen");
            alert.setContentText("Activa la lupa un momento y vuelve a intentar (Ctrl+Alt+T por defecto).");
            alert.show();
            return;
        }

        String text = ocrEngine.recognize(img);
        if (text == null || text.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Forzen OCR");
            alert.setHeaderText("Sin texto detectado");
            alert.setContentText("Prueba con más zoom o sobre una zona con texto nítido.");
            alert.show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forzen OCR");
        alert.setHeaderText("Texto reconocido");
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefRowCount(12);
        area.setPrefColumnCount(40);
        VBox box = new VBox(area);
        alert.getDialogPane().setContent(box);
        alert.show();

        if (zoomController.isAutoTts() && ttsEngine != null && ttsEngine.isAvailable()) {
            ttsEngine.speak(text);
        }
    }

    public ConfigStore getConfigStore() {
        return configStore;
    }

    public HotkeyManager getHotkeyManager() {
        return hotkeyManager;
    }

    public OcrEngine getOcrEngine() {
        return ocrEngine;
    }

    public TtsEngine getTtsEngine() {
        return ttsEngine;
    }

    public ZoomController getZoomController() {
        return zoomController;
    }

    @Override
    public void stop() {
        if (configStore != null && zoomController != null) {
            configStore.saveFrom(zoomController);
        }
        if (hotkeyManager != null) hotkeyManager.unregister();
        if (overlay != null) overlay.shutdown();
        if (screenCapture != null) screenCapture.dispose();
        if (imagePipeline != null) imagePipeline.dispose();
        if (tray != null) tray.shutdown();
        if (ttsEngine != null) ttsEngine.shutdown();
    }

    public void shutdown() {
        if (configStore != null && zoomController != null) {
            configStore.saveFrom(zoomController);
        }
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
