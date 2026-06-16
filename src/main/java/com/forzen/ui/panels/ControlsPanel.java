package com.forzen.ui.panels;

import com.forzen.core.ZoomController;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ControlsPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";

    public ControlsPanel(ZoomController zoomController) {
        setSpacing(15);
        setStyle("-fx-padding: 30;");

        Label title = new Label("⌨️ CONTROLES");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        addHotkey("Zoom +", "Ctrl + Alt + ↑");
        addHotkey("Zoom -", "Ctrl + Alt + ↓");
        addHotkey("Pausar / Reanudar", "Ctrl + Alt + Z");
        addHotkey("Cambiar modo", "Ctrl + Alt + M");
        addHotkey("Abrir ajustes", "Ctrl + Alt + ,");
        addHotkey("Salir", "Ctrl + Alt + X");

        Label note = new Label("Los atajos personalizables estarán disponibles en la versión completa.");
        note.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px; -fx-padding: 10 0 0 0;");

        getChildren().addAll(title, note);
    }

    private void addHotkey(String action, String keys) {
        String style = "-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 14px; -fx-padding: 5 0;";
        Label lbl = new Label(action + "   [ " + keys + " ]");
        lbl.setStyle(style);
        getChildren().add(lbl);
    }
}
