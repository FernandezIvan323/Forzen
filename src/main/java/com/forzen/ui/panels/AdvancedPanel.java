package com.forzen.ui.panels;

import com.forzen.core.ZoomController;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AdvancedPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_MUTED = "#AAAAAA";

    public AdvancedPanel(ZoomController zoomController) {
        setSpacing(15);
        setStyle("-fx-padding: 30;");

        Label title = new Label("🖥️ AVANZADO");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label info = new Label("Opciones avanzadas disponibles en la versión completa.");
        info.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px;");

        getChildren().addAll(title, info);
    }
}
