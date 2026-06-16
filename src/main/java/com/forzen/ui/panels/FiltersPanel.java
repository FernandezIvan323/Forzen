package com.forzen.ui.panels;

import com.forzen.core.ZoomController;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class FiltersPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";
    private static final String BORDER_GREEN = "#005A2E";

    public FiltersPanel(ZoomController zoomController) {
        setSpacing(20);
        setStyle("-fx-padding: 30;");

        Label title = new Label("🌈 FILTROS DE COLOR");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        CheckBox invertColors = new CheckBox("Invertir colores");
        invertColors.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");

        CheckBox highContrast = new CheckBox("Alto contraste");
        highContrast.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");

        CheckBox grayscale = new CheckBox("Escala de grises");
        grayscale.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");

        VBox colorBlind = section("Filtros de daltonismo");
        CheckBox protanopia = new CheckBox("Protanopía (rojo)");
        protanopia.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        CheckBox deuteranopia = new CheckBox("Deuteranopía (verde)");
        deuteranopia.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        CheckBox tritanopia = new CheckBox("Tritanopía (azul)");
        tritanopia.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");

        VBox adjustments = section("Ajustes de imagen");
        Slider brightness = slider("Brillo", zoomController.getZoomLevel(), 0, 200);
        brightness.setValue(100);
        Slider contrast = slider("Contraste", zoomController.getZoomLevel(), 0, 200);
        contrast.setValue(100);
        Slider saturation = slider("Saturación", zoomController.getZoomLevel(), 0, 200);
        saturation.setValue(100);

        getChildren().addAll(title, invertColors, highContrast, grayscale, colorBlind,
            protanopia, deuteranopia, tritanopia, adjustments, brightness, contrast, saturation);
    }

    private VBox section(String label) {
        VBox box = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }

    private Slider slider(String name, double val, double min, double max) {
        Slider s = new Slider(min, max, val);
        Label l = new Label(name + ": " + (int) val + "%");
        l.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        s.valueProperty().addListener((obs, o, v) -> l.setText(name + ": " + v.intValue() + "%"));
        s.setStyle("-fx-control-inner-background: " + BORDER_GREEN + ";");
        return s;
    }
}
