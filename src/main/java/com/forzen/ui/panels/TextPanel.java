package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.ocr.OcrEngine;
import com.forzen.tts.TtsEngine;
import com.forzen.ui.theme.AppTheme;
import com.forzen.ui.theme.ThemeManager;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TextPanel extends VBox {

    public TextPanel(ZoomController zoomController, OcrEngine ocrEngine, TtsEngine ttsEngine) {
        AppTheme theme = ThemeManager.get().current();
        setStyle("-fx-background-color: transparent;");

        boolean ocrOk = ocrEngine != null && ocrEngine.isAvailable();
        Label ocrStatus = new Label(ocrOk
                ? "OCR disponible (Tess4J)"
                : "OCR no disponible — instala Tesseract + tessdata.");
        ocrStatus.setStyle("-fx-text-fill: " + (ocrOk ? theme.accent() : theme.danger()) + "; -fx-font-size: 13px;");
        ocrStatus.setWrapText(true);

        CheckBox autoOcr = new CheckBox("Preferencia OCR automático (experimental)");
        autoOcr.setStyle(theme.bodyStyle());
        autoOcr.selectedProperty().bindBidirectional(zoomController.autoOcrProperty());
        autoOcr.setDisable(!ocrOk);

        VBox ocrCard = PanelSupport.card(theme, "Reconocimiento de texto",
                ocrStatus, autoOcr,
                PanelSupport.hint(theme, "Atajo: Ctrl+Alt+T — reconoce el área de la lupa."));

        if (ocrEngine != null && !ocrOk && ocrEngine.getLastError() != null && !ocrEngine.getLastError().isBlank()) {
            ocrCard.getChildren().add(PanelSupport.hint(theme, "Detalle: " + ocrEngine.getLastError()));
        }

        boolean ttsOk = ttsEngine != null && ttsEngine.isAvailable();
        Label ttsStatus = new Label(ttsOk
                ? "TTS disponible (Windows Speech)"
                : "TTS no disponible en este sistema");
        ttsStatus.setStyle("-fx-text-fill: " + (ttsOk ? theme.accent() : theme.danger()) + "; -fx-font-size: 13px;");

        CheckBox autoTts = new CheckBox("Leer en voz alta tras OCR");
        autoTts.setStyle(theme.bodyStyle());
        autoTts.selectedProperty().bindBidirectional(zoomController.autoTtsProperty());
        autoTts.setDisable(!ttsOk);

        VBox ttsCard = PanelSupport.card(theme, "Texto a voz",
                ttsStatus, autoTts,
                PanelSupport.hint(theme, "Síntesis offline con System.Speech."));

        getChildren().setAll(PanelSupport.page(theme,
                "Texto",
                "OCR y lectura en voz alta.",
                ocrCard, ttsCard));
    }
}
