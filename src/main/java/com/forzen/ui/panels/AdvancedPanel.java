package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.ui.theme.AppTheme;
import com.forzen.ui.theme.ThemeManager;
import com.forzen.win.AutostartService;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AdvancedPanel extends VBox {

    public AdvancedPanel(ZoomController zoomController) {
        AppTheme theme = ThemeManager.get().current();
        setStyle("-fx-background-color: transparent;");

        CheckBox startWithOs = new CheckBox("Iniciar con Windows al iniciar sesión");
        startWithOs.setStyle(theme.bodyStyle());
        startWithOs.selectedProperty().bindBidirectional(zoomController.startWithOsProperty());
        Label startInfo = new Label("Comando: " + AutostartService.resolveLaunchCommand());
        startInfo.setStyle(theme.mutedStyle() + " -fx-font-size: 11px;");
        startInfo.setWrapText(true);

        VBox systemCard = PanelSupport.card(theme, "Sistema", startWithOs, startInfo);

        Label capInfo = new Label(
                "Captura GDI (BitBlt) + exclusión de ventana (WDA_EXCLUDEFROMCAPTURE).\n"
                        + "Click-through nativo (WS_EX_TRANSPARENT) para no bloquear el escritorio."
        );
        capInfo.setStyle(theme.mutedStyle());
        capInfo.setWrapText(true);
        VBox captureCard = PanelSupport.card(theme, "Captura", capInfo);

        Label version = new Label("Forzen v1.2.0");
        version.setStyle(theme.bodyStyle() + " -fx-font-size: 14px; -fx-font-weight: bold;");
        Label license = new Label("MIT · github.com/FernandezIvan323/Forzen");
        license.setStyle(theme.mutedStyle());
        Label java = new Label("Java " + System.getProperty("java.version") + " · " + System.getProperty("os.name"));
        java.setStyle(theme.mutedStyle());

        VBox infoCard = PanelSupport.card(theme, "Acerca de", version, license, java);

        getChildren().setAll(PanelSupport.page(theme,
                "Avanzado",
                "Sistema, captura e información.",
                systemCard, captureCard, infoCard));
    }
}
