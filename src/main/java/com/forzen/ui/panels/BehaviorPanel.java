package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.ui.theme.AppTheme;
import com.forzen.ui.theme.ThemeManager;
import com.forzen.win.AutostartService;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;

public class BehaviorPanel extends VBox {

    public BehaviorPanel(ZoomController zoomController) {
        AppTheme theme = ThemeManager.get().current();
        setStyle("-fx-background-color: transparent;");

        CheckBox startWithOs = new CheckBox("Iniciar con Windows");
        startWithOs.setStyle(theme.bodyStyle());
        startWithOs.selectedProperty().bindBidirectional(zoomController.startWithOsProperty());

        CheckBox showFps = new CheckBox("Mostrar FPS en pantalla");
        showFps.setStyle(theme.bodyStyle());
        showFps.selectedProperty().bindBidirectional(zoomController.showFpsProperty());

        Label status = new Label("Autostart en Registro: " + (AutostartService.isEnabled() ? "SÍ" : "NO"));
        status.setStyle(theme.mutedStyle());

        VBox generalCard = PanelSupport.card(theme, "General",
                startWithOs, showFps, status,
                PanelSupport.hint(theme, "Autostart escribe en HKCU\\…\\Run (Forzen.exe o classpath de dev)."));

        Spinner<Integer> fpsSpin = new Spinner<>(15, 120, zoomController.getTargetFps(), 5);
        fpsSpin.setEditable(true);
        fpsSpin.setPrefWidth(120);
        fpsSpin.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                try { fpsSpin.commitValue(); } catch (Exception ignored) {}
            }
        });
        fpsSpin.valueProperty().addListener((obs, o, v) -> {
            if (v != null) zoomController.setTargetFps(v);
        });
        zoomController.targetFpsProperty().addListener((obs, o, v) -> {
            if (fpsSpin.getValue() == null || !fpsSpin.getValue().equals(v.intValue())) {
                fpsSpin.getValueFactory().setValue(v.intValue());
            }
        });

        VBox fpsCard = PanelSupport.card(theme, "Rendimiento",
                fpsSpin,
                PanelSupport.hint(theme, "FPS objetivo de captura (lupa y panel acoplado)."));

        getChildren().setAll(PanelSupport.page(theme,
                "Comportamiento",
                "Inicio con Windows y rendimiento.",
                generalCard, fpsCard));
    }
}
