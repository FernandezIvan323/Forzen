package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.filter.FilterMode;

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

        ToggleGroup filterGroup = new ToggleGroup();

        RadioButton none = filterRadio("Ninguno", FilterMode.NONE, filterGroup, zoomController);
        RadioButton invert = filterRadio("Invertir colores", FilterMode.INVERT, filterGroup, zoomController);
        RadioButton highContrast = filterRadio("Alto contraste", FilterMode.HIGH_CONTRAST, filterGroup, zoomController);
        RadioButton grayscale = filterRadio("Escala de grises", FilterMode.GRAYSCALE, filterGroup, zoomController);

        none.setSelected(zoomController.getFilterMode() == FilterMode.NONE);

        VBox colorBlind = section("Filtros de daltonismo");
        RadioButton protanopia = filterRadio("Protanopía (rojo)", FilterMode.PROTANOPIA, filterGroup, zoomController);
        RadioButton deuteranopia = filterRadio("Deuteranopía (verde)", FilterMode.DEUTERANOPIA, filterGroup, zoomController);
        RadioButton tritanopia = filterRadio("Tritanopía (azul)", FilterMode.TRITANOPIA, filterGroup, zoomController);

        VBox adjustments = section("Ajustes de imagen");
        Slider brightness = slider("Brillo", zoomController.getBrightness(), 0, 200);
        brightness.valueProperty().bindBidirectional(zoomController.brightnessProperty());
        Slider contrast = slider("Contraste", zoomController.getContrast(), 0, 200);
        contrast.valueProperty().bindBidirectional(zoomController.contrastProperty());
        Slider saturation = slider("Saturación", zoomController.getSaturation(), 0, 200);
        saturation.valueProperty().bindBidirectional(zoomController.saturationProperty());

        getChildren().addAll(title, none, invert, highContrast, grayscale, colorBlind,
            protanopia, deuteranopia, tritanopia, adjustments, brightness, contrast, saturation);
    }

    private RadioButton filterRadio(String label, FilterMode mode, ToggleGroup group, ZoomController zc) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(group);
        rb.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        rb.setOnAction(e -> zc.setFilterMode(mode));
        return rb;
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
