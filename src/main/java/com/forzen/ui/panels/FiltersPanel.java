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
        setSpacing(16);
        setStyle("-fx-padding: 30;");

        Label title = new Label("🌈 FILTROS DE COLOR");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        ToggleGroup filterGroup = new ToggleGroup();

        RadioButton none = filterRadio("Ninguno", FilterMode.NONE, filterGroup, zoomController);
        RadioButton invert = filterRadio("Invertir colores", FilterMode.INVERT, filterGroup, zoomController);
        RadioButton highContrast = filterRadio("Alto contraste", FilterMode.HIGH_CONTRAST, filterGroup, zoomController);
        RadioButton grayscale = filterRadio("Escala de grises", FilterMode.GRAYSCALE, filterGroup, zoomController);

        VBox colorBlind = section("Filtros de daltonismo");
        RadioButton protanopia = filterRadio("Protanopía (rojo)", FilterMode.PROTANOPIA, filterGroup, zoomController);
        RadioButton deuteranopia = filterRadio("Deuteranopía (verde)", FilterMode.DEUTERANOPIA, filterGroup, zoomController);
        RadioButton tritanopia = filterRadio("Tritanopía (azul)", FilterMode.TRITANOPIA, filterGroup, zoomController);
        colorBlind.getChildren().addAll(protanopia, deuteranopia, tritanopia);

        selectFilter(zoomController.getFilterMode(), none, invert, highContrast, grayscale,
                protanopia, deuteranopia, tritanopia);
        zoomController.filterModeProperty().addListener((obs, o, m) ->
                selectFilter(m, none, invert, highContrast, grayscale, protanopia, deuteranopia, tritanopia));

        VBox adjustments = section("Ajustes de imagen");
        VBox brightness = labeledSlider("Brillo", zoomController.getBrightness(), 0, 200, zoomController.brightnessProperty());
        VBox contrast = labeledSlider("Contraste", zoomController.getContrast(), 0, 200, zoomController.contrastProperty());
        VBox saturation = labeledSlider("Saturación", zoomController.getSaturation(), 0, 200, zoomController.saturationProperty());
        adjustments.getChildren().addAll(brightness, contrast, saturation);

        getChildren().addAll(title, none, invert, highContrast, grayscale, colorBlind, adjustments);
    }

    private void selectFilter(FilterMode mode, RadioButton none, RadioButton invert,
                              RadioButton highContrast, RadioButton grayscale,
                              RadioButton protanopia, RadioButton deuteranopia, RadioButton tritanopia) {
        if (mode == null) mode = FilterMode.NONE;
        switch (mode) {
            case INVERT -> invert.setSelected(true);
            case HIGH_CONTRAST -> highContrast.setSelected(true);
            case GRAYSCALE -> grayscale.setSelected(true);
            case PROTANOPIA -> protanopia.setSelected(true);
            case DEUTERANOPIA -> deuteranopia.setSelected(true);
            case TRITANOPIA -> tritanopia.setSelected(true);
            default -> none.setSelected(true);
        }
    }

    private RadioButton filterRadio(String label, FilterMode mode, ToggleGroup group, ZoomController zc) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(group);
        rb.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        rb.setOnAction(e -> zc.setFilterMode(mode));
        return rb;
    }

    private VBox section(String label) {
        VBox box = new VBox(8);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }

    private VBox labeledSlider(String name, double val, double min, double max,
                               javafx.beans.property.DoubleProperty property) {
        VBox box = new VBox(4);
        Label l = new Label(name + ": " + (int) val + "%");
        l.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        Slider s = new Slider(min, max, val);
        s.setStyle("-fx-control-inner-background: " + BORDER_GREEN + ";");
        s.valueProperty().bindBidirectional(property);
        s.valueProperty().addListener((obs, o, v) -> l.setText(name + ": " + v.intValue() + "%"));
        box.getChildren().addAll(l, s);
        return box;
    }
}
