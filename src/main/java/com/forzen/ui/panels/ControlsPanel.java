package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.input.HotkeyAction;
import com.forzen.input.HotkeyBinding;
import com.forzen.input.HotkeyManager;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

public class ControlsPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";
    private static final String BG_PANEL = "#1A1A1A";
    private static final String BORDER = "#005A2E";

    private final HotkeyManager hotkeyManager;
    private final Map<HotkeyAction, Label> labels = new EnumMap<>(HotkeyAction.class);
    private Label captureHint;

    public ControlsPanel(ZoomController zoomController, HotkeyManager hotkeyManager) {
        this.hotkeyManager = hotkeyManager;
        setSpacing(12);
        setStyle("-fx-padding: 30;");

        Label title = new Label("⌨️ CONTROLES");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        Label note = new Label("Haz clic en «Cambiar» y pulsa la combinación deseada (con modificadores).");
        note.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        note.setWrapText(true);

        captureHint = new Label("");
        captureHint.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 13px;");

        getChildren().addAll(title, note, captureHint);

        if (hotkeyManager == null) {
            getChildren().add(new Label("Hotkeys no disponibles"));
            return;
        }

        addRow(HotkeyAction.ZOOM_IN, "Zoom +");
        addRow(HotkeyAction.ZOOM_OUT, "Zoom -");
        addRow(HotkeyAction.TOGGLE_PAUSE, "Pausar / Reanudar");
        addRow(HotkeyAction.CYCLE_MODE, "Cambiar modo");
        addRow(HotkeyAction.OPEN_SETTINGS, "Abrir ajustes");
        addRow(HotkeyAction.OCR_READ, "OCR + leer");
        addRow(HotkeyAction.EXIT, "Salir");

        Button reset = new Button("Restaurar atajos por defecto");
        reset.setStyle("-fx-background-color: " + BORDER + "; -fx-text-fill: " + TEXT_LIGHT + "; -fx-padding: 8 14;");
        reset.setOnAction(e -> {
            for (HotkeyAction a : HotkeyAction.values()) {
                hotkeyManager.setBinding(a, HotkeyBinding.defaults(a));
            }
            refreshLabels();
            captureHint.setText("Atajos restaurados.");
        });
        getChildren().add(reset);
    }

    private void addRow(HotkeyAction action, String title) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-padding: 10 12; -fx-background-radius: 6;");

        Label name = new Label(title);
        name.setPrefWidth(180);
        name.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 14px;");

        Label keys = new Label(hotkeyManager.getBinding(action).toDisplayString());
        keys.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 14px; -fx-font-family: monospace;");
        keys.setPrefWidth(220);
        labels.put(action, keys);

        Button change = new Button("Cambiar");
        change.setStyle("-fx-background-color: " + GREEN_NEON + "; -fx-text-fill: #0D0D0D; -fx-font-weight: bold;");
        change.setOnAction(e -> {
            captureHint.setText("Pulsa la nueva combinación para: " + title + " …");
            hotkeyManager.beginCapture(action, () -> {
                keys.setText(hotkeyManager.getBinding(action).toDisplayString());
                captureHint.setText("Guardado: " + title + " → " + keys.getText());
            });
        });

        row.getChildren().addAll(name, keys, change);
        getChildren().add(row);
    }

    private void refreshLabels() {
        for (Map.Entry<HotkeyAction, Label> e : labels.entrySet()) {
            e.getValue().setText(hotkeyManager.getBinding(e.getKey()).toDisplayString());
        }
    }
}
