package com.forzen.ui.panels;

import com.forzen.core.LensShape;
import com.forzen.core.ZoomController;
import com.forzen.ui.theme.AppTheme;
import com.forzen.ui.theme.ThemeCatalog;
import com.forzen.ui.theme.ThemeManager;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class AppearancePanel extends VBox {

    public AppearancePanel(ZoomController zoomController) {
        AppTheme theme = ThemeManager.get().current();
        setStyle("-fx-background-color: transparent;");

        ComboBox<AppTheme> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll(ThemeCatalog.all());
        themeCombo.setMaxWidth(Double.MAX_VALUE);
        themeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(AppTheme t) { return t == null ? "" : t.displayName(); }
            @Override public AppTheme fromString(String s) { return null; }
        });
        themeCombo.setCellFactory(list -> new ListCell<>() {
            private final Rectangle swatch = new Rectangle(14, 14);
            {
                swatch.setArcWidth(4);
                swatch.setArcHeight(4);
            }
            @Override
            protected void updateItem(AppTheme item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.displayName());
                    try {
                        swatch.setFill(Color.web(item.accent()));
                    } catch (Exception e) {
                        swatch.setFill(Color.GRAY);
                    }
                    setGraphic(swatch);
                }
            }
        });
        themeCombo.getSelectionModel().select(ThemeCatalog.get(zoomController.getUiTheme()));
        themeCombo.valueProperty().addListener((obs, o, t) -> {
            if (t != null) {
                zoomController.setUiTheme(t.id());
                ThemeManager.get().setTheme(t);
            }
        });

        VBox themeCard = PanelSupport.card(theme, "Tema de la interfaz",
                themeCombo,
                PanelSupport.hint(theme, "5 presets. Por defecto: Forzen Dark."));

        Slider borderSlider = new Slider(0, 10, zoomController.getBorderWidth());
        borderSlider.setShowTickLabels(true);
        borderSlider.setMajorTickUnit(2);
        Label borderValue = new Label(String.format("%.1f px", zoomController.getBorderWidth()));
        borderValue.setStyle(theme.valueStyle());
        borderSlider.valueProperty().bindBidirectional(zoomController.borderWidthProperty());
        borderSlider.valueProperty().addListener((obs, o, v) ->
                borderValue.setText(String.format("%.1f px", v.doubleValue())));
        HBox borderRow = new HBox(15, borderSlider, borderValue);
        borderRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(borderSlider, javafx.scene.layout.Priority.ALWAYS);

        ColorPicker borderColorPicker = new ColorPicker(parseColor(zoomController.getBorderColor(), Color.web("#34D399")));
        borderColorPicker.setOnAction(e ->
                zoomController.setBorderColor(toHex(borderColorPicker.getValue())));
        zoomController.borderColorProperty().addListener((obs, o, v) -> {
            Color c = parseColor(v, Color.web("#34D399"));
            if (!colorsClose(borderColorPicker.getValue(), c)) {
                borderColorPicker.setValue(c);
            }
        });

        Slider opacitySlider = new Slider(0, 100, zoomController.getBorderOpacity());
        opacitySlider.setMajorTickUnit(25);
        Label opacityValue = new Label(String.format("%.0f%%", zoomController.getBorderOpacity()));
        opacityValue.setStyle(theme.valueStyle());
        opacitySlider.valueProperty().bindBidirectional(zoomController.borderOpacityProperty());
        opacitySlider.valueProperty().addListener((obs, o, v) ->
                opacityValue.setText(String.format("%.0f%%", v.doubleValue())));
        HBox opacityRow = new HBox(15, opacitySlider, opacityValue);
        opacityRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(opacitySlider, javafx.scene.layout.Priority.ALWAYS);

        VBox borderCard = PanelSupport.card(theme, "Borde de la lupa",
                borderRow,
                new HBox(12, label(theme, "Color"), borderColorPicker),
                label(theme, "Opacidad"),
                opacityRow);

        ToggleGroup shapeGroup = new ToggleGroup();
        RadioButton circular = shapeRadio("Circular", theme);
        RadioButton rectangular = shapeRadio("Rectangular", theme);
        RadioButton rounded = shapeRadio("Redondeado", theme);
        circular.setToggleGroup(shapeGroup);
        rectangular.setToggleGroup(shapeGroup);
        rounded.setToggleGroup(shapeGroup);
        selectShape(zoomController.getLensShape(), circular, rectangular, rounded);
        zoomController.lensShapeProperty().addListener((obs, o, v) ->
                selectShape(v, circular, rectangular, rounded));
        circular.setOnAction(e -> zoomController.setLensShape(LensShape.CIRCLE));
        rectangular.setOnAction(e -> zoomController.setLensShape(LensShape.RECTANGLE));
        rounded.setOnAction(e -> zoomController.setLensShape(LensShape.ROUNDED));
        HBox shapeRow = new HBox(16, circular, rectangular, rounded);

        Slider radiusSlider = new Slider(0, 80, zoomController.getLensCornerRadius());
        radiusSlider.setMajorTickUnit(20);
        Label radiusValue = new Label(String.format("%.0f px", zoomController.getLensCornerRadius()));
        radiusValue.setStyle(theme.valueStyle());
        radiusSlider.valueProperty().bindBidirectional(zoomController.lensCornerRadiusProperty());
        radiusSlider.valueProperty().addListener((obs, o, v) ->
                radiusValue.setText(String.format("%.0f px", v.doubleValue())));
        radiusSlider.disableProperty().bind(
                zoomController.lensShapeProperty().isNotEqualTo(LensShape.ROUNDED));
        HBox radiusRow = new HBox(15, radiusSlider, radiusValue);
        radiusRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(radiusSlider, javafx.scene.layout.Priority.ALWAYS);

        VBox shapeCard = PanelSupport.card(theme, "Forma",
                shapeRow,
                label(theme, "Radio de esquinas"),
                radiusRow);

        CheckBox showCross = new CheckBox("Mostrar crosshair");
        showCross.setStyle(theme.bodyStyle());
        showCross.selectedProperty().bindBidirectional(zoomController.showCrosshairProperty());
        ColorPicker crossColor = new ColorPicker(parseColor(zoomController.getCrosshairColor(), Color.web("#FF003C")));
        crossColor.setOnAction(e -> zoomController.setCrosshairColor(toHex(crossColor.getValue())));

        VBox crossCard = PanelSupport.card(theme, "Punto de mira",
                showCross,
                new HBox(12, label(theme, "Color"), crossColor));

        CheckBox smooth = new CheckBox("Suavizado bilinear (mejor calidad)");
        smooth.setStyle(theme.bodyStyle());
        smooth.selectedProperty().bindBidirectional(zoomController.smoothScalingProperty());
        VBox qualityCard = PanelSupport.card(theme, "Escalado", smooth);

        getChildren().setAll(PanelSupport.page(theme,
                "Apariencia",
                "Tema de ajustes y aspecto de la lupa.",
                themeCard, borderCard, shapeCard, crossCard, qualityCard));
    }

    private static Label label(AppTheme theme, String text) {
        Label l = new Label(text);
        l.setStyle(theme.bodyStyle());
        return l;
    }

    private static RadioButton shapeRadio(String text, AppTheme theme) {
        RadioButton rb = new RadioButton(text);
        rb.setStyle(theme.bodyStyle());
        return rb;
    }

    private static void selectShape(LensShape shape, RadioButton circle, RadioButton rect, RadioButton round) {
        if (shape == null) shape = LensShape.CIRCLE;
        switch (shape) {
            case RECTANGLE -> rect.setSelected(true);
            case ROUNDED -> round.setSelected(true);
            default -> circle.setSelected(true);
        }
    }

    private static Color parseColor(String hex, Color fallback) {
        try {
            return Color.web(hex);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) Math.round(c.getRed() * 255),
                (int) Math.round(c.getGreen() * 255),
                (int) Math.round(c.getBlue() * 255));
    }

    private static boolean colorsClose(Color a, Color b) {
        if (a == null || b == null) return false;
        return Math.abs(a.getRed() - b.getRed()) < 0.01
                && Math.abs(a.getGreen() - b.getGreen()) < 0.01
                && Math.abs(a.getBlue() - b.getBlue()) < 0.01;
    }
}
