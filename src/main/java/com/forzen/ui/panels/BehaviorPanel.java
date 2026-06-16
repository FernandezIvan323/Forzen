package com.forzen.ui.panels;

import com.forzen.core.ZoomController;

import javafx.scene.control.*;
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

        CheckBox showFps = new CheckBox("Mostrar FPS en pantalla");
        showFps.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        showFps.selectedProperty().bindBidirectional(zoomController.showFpsProperty());

        CheckBox rememberPos = new CheckBox("Recordar última posición");
        rememberPos.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");

        getChildren().addAll(title, startWithOs, showFps, rememberPos);
    }
}
