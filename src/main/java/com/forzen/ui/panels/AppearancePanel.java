package com.forzen.ui.panels;

import com.forzen.core.ZoomController;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AppearancePanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";

    public AppearancePanel(ZoomController zoomController) {
        setSpacing(15);
        setStyle("-fx-padding: 30;");

        Label title = new Label("🎨 APARIENCIA");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label info = new Label("Opciones de apariencia disponibles en la versión completa.");
        info.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px;");

        getChildren().addAll(title, info);
    }
}
