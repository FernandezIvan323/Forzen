package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.input.HotkeyAction;
import com.forzen.input.HotkeyBinding;
import com.forzen.input.HotkeyManager;
import com.forzen.ui.theme.AppTheme;
import com.forzen.ui.theme.ThemeManager;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

public class ControlsPanel extends VBox {

    private final HotkeyManager hotkeyManager;
    private final Map<HotkeyAction, Label> labels = new EnumMap<>(HotkeyAction.class);
    private final AppTheme theme;
    private Label captureHint;
    private VBox listCard;

    public ControlsPanel(ZoomController zoomController, HotkeyManager hotkeyManager) {
        this.hotkeyManager = hotkeyManager;
        this.theme = ThemeManager.get().current();
        setStyle("-fx-background-color: transparent;");

        captureHint = new Label("Haz clic en Cambiar y pulsa un combo (Ctrl/Alt/Shift + tecla). Esc cancela.");
        captureHint.setStyle(theme.mutedStyle());
        captureHint.setWrapText(true);

        listCard = PanelSupport.card(theme, "Atajos globales");
        listCard.setSpacing(6);

        if (hotkeyManager == null) {
            Label missing = new Label("Hotkeys no disponibles");
            missing.setStyle(theme.bodyStyle());
            listCard.getChildren().add(missing);
        } else {
            addRow(HotkeyAction.ZOOM_IN, "Zoom +");
            addRow(HotkeyAction.ZOOM_OUT, "Zoom −");
            addRow(HotkeyAction.TOGGLE_PAUSE, "Pausar / Reanudar");
            addRow(HotkeyAction.CYCLE_MODE, "Cambiar modo");
            addRow(HotkeyAction.OPEN_SETTINGS, "Abrir ajustes");
            addRow(HotkeyAction.OCR_READ, "OCR + leer (experimental)");
            addRow(HotkeyAction.EXIT, "Salir");
        }

        Button reset = new Button("Restaurar atajos por defecto");
        reset.setStyle(theme.secondaryBtnStyle());
        reset.setOnAction(e -> {
            if (hotkeyManager == null) return;
            for (HotkeyAction a : HotkeyAction.values()) {
                hotkeyManager.setBinding(a, HotkeyBinding.defaults(a));
            }
            hotkeyManager.reloadBindings();
            refreshLabels();
            captureHint.setText("Atajos restaurados. Ctrl+Alt+M = modo · Ctrl+Alt+O = ajustes.");
            captureHint.setStyle(theme.valueStyle());
        });

        VBox helpCard = PanelSupport.card(theme, "Ayuda",
                captureHint,
                PanelSupport.hint(theme, "Si fallan: Ctrl+Alt+Shift+R o bandeja → Restaurar atajos."),
                reset);

        getChildren().setAll(PanelSupport.page(theme,
                "Controles",
                "Personaliza los atajos de teclado globales.",
                listCard, helpCard));
    }

    private void addRow(HotkeyAction action, String title) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + theme.panel() + "; -fx-padding: 10 12; "
                + "-fx-background-radius: 8;");

        Label name = new Label(title);
        name.setPrefWidth(160);
        name.setStyle(theme.bodyStyle() + " -fx-font-size: 13px;");

        Label keys = new Label(hotkeyManager.getBinding(action).toDisplayString());
        keys.setStyle("-fx-text-fill: " + theme.accent() + "; -fx-font-size: 13px; -fx-font-family: 'Consolas', monospace;");
        keys.setPrefWidth(200);
        labels.put(action, keys);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button change = new Button("Cambiar");
        change.setStyle(theme.secondaryBtnStyle() + " -fx-padding: 6 14;");
        change.setOnAction(e -> {
            captureHint.setText("Escuchando… " + title);
            captureHint.setStyle(theme.valueStyle());
            hotkeyManager.beginCapture(action, (ok, msg) -> {
                keys.setText(hotkeyManager.getBinding(action).toDisplayString());
                captureHint.setText(msg != null ? msg : (ok ? "OK" : "Error"));
                if (ok) {
                    captureHint.setStyle(theme.valueStyle());
                } else {
                    captureHint.setStyle("-fx-text-fill: " + theme.danger() + "; -fx-font-size: 12px;");
                }
            });
        });

        row.getChildren().addAll(name, keys, spacer, change);
        listCard.getChildren().add(row);
    }

    private void refreshLabels() {
        for (Map.Entry<HotkeyAction, Label> e : labels.entrySet()) {
            e.getValue().setText(hotkeyManager.getBinding(e.getKey()).toDisplayString());
        }
    }
}
