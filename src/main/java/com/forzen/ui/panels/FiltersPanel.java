package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.filter.FilterMode;
import com.forzen.ui.theme.AppTheme;
import com.forzen.ui.theme.ThemeManager;

import javafx.beans.property.DoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class FiltersPanel extends VBox {

    public FiltersPanel(ZoomController zoomController) {
        AppTheme theme = ThemeManager.get().current();
        setStyle("-fx-background-color: transparent;");

        ToggleGroup filterGroup = new ToggleGroup();
        RadioButton none = filterRadio("Ninguno", FilterMode.NONE, filterGroup, zoomController, theme);
        RadioButton invert = filterRadio("Invertir colores", FilterMode.INVERT, filterGroup, zoomController, theme);
        RadioButton highContrast = filterRadio("Alto contraste", FilterMode.HIGH_CONTRAST, filterGroup, zoomController, theme);
        RadioButton grayscale = filterRadio("Escala de grises", FilterMode.GRAYSCALE, filterGroup, zoomController, theme);
        RadioButton protanopia = filterRadio("Protanopía (rojo)", FilterMode.PROTANOPIA, filterGroup, zoomController, theme);
        RadioButton deuteranopia = filterRadio("Deuteranopía (verde)", FilterMode.DEUTERANOPIA, filterGroup, zoomController, theme);
        RadioButton tritanopia = filterRadio("Tritanopía (azul)", FilterMode.TRITANOPIA, filterGroup, zoomController, theme);

        selectFilter(zoomController.getFilterMode(), none, invert, highContrast, grayscale,
                protanopia, deuteranopia, tritanopia);
        zoomController.filterModeProperty().addListener((obs, o, m) ->
                selectFilter(m, none, invert, highContrast, grayscale, protanopia, deuteranopia, tritanopia));

        VBox modesCard = PanelSupport.card(theme, "Modo de filtro",
                none, invert, highContrast, grayscale,
                PanelSupport.hint(theme, "Daltonismo"),
                protanopia, deuteranopia, tritanopia);

        VBox brightness = labeledSlider("Brillo", zoomController.getBrightness(), 0, 200,
                zoomController.brightnessProperty(), theme);
        VBox contrast = labeledSlider("Contraste", zoomController.getContrast(), 0, 200,
                zoomController.contrastProperty(), theme);
        VBox saturation = labeledSlider("Saturación", zoomController.getSaturation(), 0, 200,
                zoomController.saturationProperty(), theme);

        VBox adjCard = PanelSupport.card(theme, "Ajustes de imagen",
                brightness, contrast, saturation,
                PanelSupport.hint(theme, "Se aplican en vivo a la lupa."));

        getChildren().setAll(PanelSupport.page(theme,
                "Filtros",
                "Color y legibilidad para baja visión.",
                modesCard, adjCard));
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

    private RadioButton filterRadio(String label, FilterMode mode, ToggleGroup group,
                                    ZoomController zc, AppTheme theme) {
        RadioButton rb = new RadioButton(label);
        rb.setToggleGroup(group);
        rb.setStyle(theme.bodyStyle());
        rb.setOnAction(e -> zc.setFilterMode(mode));
        return rb;
    }

    private VBox labeledSlider(String name, double value, double min, double max,
                               DoubleProperty property, AppTheme theme) {
        VBox box = new VBox(4);
        Label lbl = new Label(name + ": " + (int) value);
        lbl.setStyle(theme.bodyStyle());
        Slider slider = new Slider(min, max, value);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit((max - min) / 4);
        slider.valueProperty().bindBidirectional(property);
        slider.valueProperty().addListener((obs, o, v) ->
                lbl.setText(name + ": " + v.intValue()));
        box.getChildren().addAll(lbl, slider);
        return box;
    }
}
