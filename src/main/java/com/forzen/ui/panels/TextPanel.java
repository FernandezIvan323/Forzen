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
        Label experimentalBanner = new Label(
                "Experimental — no forma parte del núcleo de la lupa. Requiere Tesseract en el sistema.");
        experimentalBanner.setStyle("-fx-text-fill: " + theme.muted() + "; -fx-font-size: 12px; -fx-font-style: italic;");
        experimentalBanner.setWrapText(true);

        Label ocrStatus = new Label(ocrOk
                ? "OCR disponible (Tess4J) — experimental"
                : "OCR no disponible — instala Tesseract + tessdata (opcional).");
        ocrStatus.setStyle("-fx-text-fill: " + (ocrOk ? theme.accent() : theme.danger()) + "; -fx-font-size: 13px;");
        ocrStatus.setWrapText(true);

        CheckBox autoOcr = new CheckBox("OCR automático (experimental)");
        autoOcr.setStyle(theme.bodyStyle());
        autoOcr.selectedProperty().bindBidirectional(zoomController.autoOcrProperty());
        autoOcr.setDisable(!ocrOk);

        VBox ocrCard = PanelSupport.card(theme, "Reconocimiento de texto (experimental)",
                experimentalBanner, ocrStatus, autoOcr,
                PanelSupport.hint(theme, "Atajo: Ctrl+Alt+T — reconoce el área de la lupa. La lupa funciona sin OCR."));

        if (ocrEngine != null && !ocrOk && ocrEngine.getLastError() != null && !ocrEngine.getLastError().isBlank()) {
            ocrCard.getChildren().add(PanelSupport.hint(theme, "Detalle: " + ocrEngine.getLastError()));
        }

        boolean ttsOk = ttsEngine != null && ttsEngine.isAvailable();
        Label ttsStatus = new Label(ttsOk
                ? "TTS disponible (Windows Speech)"
                : "TTS no disponible en este sistema");
        ttsStatus.setStyle("-fx-text-fill: " + (ttsOk ? theme.accent() : theme.danger()) + "; -fx-font-size: 13px;");

        CheckBox autoTts = new CheckBox("Leer en voz alta tras OCR (experimental)");
        autoTts.setStyle(theme.bodyStyle());
        autoTts.selectedProperty().bindBidirectional(zoomController.autoTtsProperty());
        autoTts.setDisable(!ttsOk);

        VBox ttsCard = PanelSupport.card(theme, "Texto a voz (experimental)",
                ttsStatus, autoTts,
                PanelSupport.hint(theme, "Síntesis offline con System.Speech. Opcional; no afecta el zoom."));

        getChildren().setAll(PanelSupport.page(theme,
                "Texto",
                "OCR y lectura en voz alta — funciones experimentales, no del núcleo de la lupa.",
                ocrCard, ttsCard));
    }
}
