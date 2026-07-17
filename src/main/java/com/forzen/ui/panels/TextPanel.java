package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.ocr.OcrEngine;
import com.forzen.tts.TtsEngine;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class TextPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";

    public TextPanel(ZoomController zoomController, OcrEngine ocrEngine, TtsEngine ttsEngine) {
        setSpacing(20);
        setStyle("-fx-padding: 30;");

        Label title = new Label("📝 TEXTO");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        VBox ocrSection = section("Reconocimiento de texto (OCR)");
        boolean ocrOk = ocrEngine != null && ocrEngine.isAvailable();
        Label ocrStatus = new Label(ocrOk
                ? "Estado: disponible (Tess4J)"
                : "Estado: no disponible — instala Tesseract + tessdata (spa/eng).");
        ocrStatus.setStyle("-fx-text-fill: " + (ocrOk ? GREEN_NEON : "#FF003C") + "; -fx-font-size: 12px;");
        ocrStatus.setWrapText(true);

        CheckBox autoOcr = new CheckBox("Recordar preferencia OCR automático (experimental)");
        autoOcr.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        autoOcr.selectedProperty().bindBidirectional(zoomController.autoOcrProperty());
        autoOcr.setDisable(!ocrOk);

        Label ocrInfo = new Label("Atajo por defecto: Ctrl+Alt+T — reconoce el área actual de la lupa y muestra el texto.");
        ocrInfo.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        ocrInfo.setWrapText(true);
        ocrSection.getChildren().addAll(ocrStatus, autoOcr, ocrInfo);

        VBox ttsSection = section("Texto a voz (TTS)");
        boolean ttsOk = ttsEngine != null && ttsEngine.isAvailable();
        Label ttsStatus = new Label(ttsOk
                ? "Estado: Windows Speech API disponible"
                : "Estado: TTS no disponible en este sistema");
        ttsStatus.setStyle("-fx-text-fill: " + (ttsOk ? GREEN_NEON : "#FF003C") + "; -fx-font-size: 12px;");

        CheckBox autoTts = new CheckBox("Leer en voz alta tras OCR");
        autoTts.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        autoTts.selectedProperty().bindBidirectional(zoomController.autoTtsProperty());
        autoTts.setDisable(!ttsOk);

        Label ttsInfo = new Label("Síntesis offline con System.Speech (PowerShell). Se activa al usar OCR si esta opción está marcada.");
        ttsInfo.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        ttsInfo.setWrapText(true);
        ttsSection.getChildren().addAll(ttsStatus, autoTts, ttsInfo);

        getChildren().addAll(title, ocrSection, ttsSection);
    }

    private VBox section(String label) {
        VBox box = new VBox(8);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }
}
