package com.forzen.ui.panels;

import com.forzen.core.ZoomController;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class TextPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";
    private static final String BORDER_GREEN = "#005A2E";
    private static final String BG_PANEL = "#1A1A1A";

    public TextPanel(ZoomController zoomController) {
        setSpacing(20);
        setStyle("-fx-padding: 30;");

        Label title = new Label("📝 TEXTO");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        VBox ocrSection = section("Reconocimiento de texto (OCR)");
        CheckBox autoOcr = new CheckBox("Detectar texto automáticamente");
        autoOcr.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        autoOcr.setSelected(zoomController.isShowFps()); // placeholder
        Label ocrInfo = new Label("OCR offline con Tesseract. Idiomas: español, inglés.");
        ocrInfo.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        ocrSection.getChildren().addAll(autoOcr, ocrInfo);

        VBox ttsSection = section("Texto a voz (TTS)");
        CheckBox autoRead = new CheckBox("Leer texto automáticamente");
        autoRead.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        Label ttsInfo = new Label("Síntesis de voz offline usando Windows Speech API.");
        ttsInfo.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        ttsSection.getChildren().addAll(autoRead, ttsInfo);

        VBox zoomSection = section("Zoom de texto");
        Label zoomInfo = new Label("El aumento de texto se aplica automáticamente\nen los modos Lens, Full-Screen y Docked.");
        zoomInfo.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;");
        Slider textSize = new Slider(1, 5, 2);
        textSize.setShowTickLabels(true);
        textSize.setShowTickMarks(true);
        textSize.setMajorTickUnit(1);
        textSize.setStyle("-fx-control-inner-background: " + BORDER_GREEN + ";");
        zoomSection.getChildren().addAll(zoomInfo, textSize);

        getChildren().addAll(title, ocrSection, ttsSection, zoomSection);
    }

    private VBox section(String label) {
        VBox box = new VBox(8);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }
}
