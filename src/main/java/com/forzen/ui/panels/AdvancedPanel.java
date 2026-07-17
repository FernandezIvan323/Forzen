package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.win.AutostartService;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AdvancedPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";

    public AdvancedPanel(ZoomController zoomController) {
        setSpacing(20);
        setStyle("-fx-padding: 30;");

        Label title = new Label("🖥️ AVANZADO");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        VBox systemSection = section("Sistema");
        CheckBox startWithOs = new CheckBox("Iniciar con Windows al iniciar sesión");
        startWithOs.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        startWithOs.selectedProperty().bindBidirectional(zoomController.startWithOsProperty());
        Label startInfo = new Label("Comando: " + AutostartService.resolveLaunchCommand());
        startInfo.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        startInfo.setWrapText(true);
        systemSection.getChildren().addAll(startWithOs, startInfo);

        VBox captureSection = section("Captura");
        Label capInfo = new Label(
                "Captura GDI (BitBlt) con exclusión de ventana (WDA_EXCLUDEFROMCAPTURE).\n"
                        + "Si hay aceleración DXGI Desktop Duplication disponible se usará en el futuro "
                        + "como backend preferente; el backend actual es estable y multi-monitor."
        );
        capInfo.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        capInfo.setWrapText(true);
        captureSection.getChildren().add(capInfo);

        VBox infoSection = section("Información");
        Label version = new Label("Forzen v1.1.0");
        version.setStyle("-fx-text-fill: " + TEXT_LIGHT + "; -fx-font-size: 14px;");
        Label license = new Label("Licencia MIT — github.com/FernandezIvan323/Forzen");
        license.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        Label java = new Label("Java " + System.getProperty("java.version") + " / " + System.getProperty("os.name"));
        java.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        infoSection.getChildren().addAll(version, license, java);

        getChildren().addAll(title, systemSection, captureSection, infoSection);
    }

    private VBox section(String label) {
        VBox box = new VBox(8);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }
}
