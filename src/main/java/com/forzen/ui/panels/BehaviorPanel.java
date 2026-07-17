package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.win.AutostartService;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;

public class BehaviorPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";

    public BehaviorPanel(ZoomController zoomController) {
        setSpacing(15);
        setStyle("-fx-padding: 30;");

        Label title = new Label("⚙️ COMPORTAMIENTO");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        CheckBox startWithOs = new CheckBox("Iniciar con Windows");
        startWithOs.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        startWithOs.selectedProperty().bindBidirectional(zoomController.startWithOsProperty());
        Label startHint = new Label("Escribe/borra la entrada en el Registro (HKCU\\...\\Run). "
                + "En dev usa javaw -cp; en MSI usa Forzen.exe.");
        startHint.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        startHint.setWrapText(true);

        CheckBox showFps = new CheckBox("Mostrar FPS en pantalla");
        showFps.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        showFps.selectedProperty().bindBidirectional(zoomController.showFpsProperty());

        Label fpsTitle = new Label("FPS objetivo (captura)");
        fpsTitle.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;");
        Spinner<Integer> fpsSpin = new Spinner<>(15, 120, zoomController.getTargetFps(), 5);
        fpsSpin.setEditable(true);
        fpsSpin.valueProperty().addListener((obs, o, v) -> {
            if (v != null) zoomController.setTargetFps(v);
        });
        zoomController.targetFpsProperty().addListener((obs, o, v) -> {
            if (fpsSpin.getValue() == null || !fpsSpin.getValue().equals(v.intValue())) {
                fpsSpin.getValueFactory().setValue(v.intValue());
            }
        });

        Label status = new Label("Autostart actual en Registro: " + (AutostartService.isEnabled() ? "SÍ" : "NO"));
        status.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        getChildren().addAll(title, startWithOs, startHint, showFps, fpsTitle, fpsSpin, status);
    }
}
