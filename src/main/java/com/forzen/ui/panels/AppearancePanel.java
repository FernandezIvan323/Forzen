package com.forzen.ui.panels;

import com.forzen.core.ZoomController;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AppearancePanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";

    public AppearancePanel(ZoomController zoomController) {
        setSpacing(20);
        setStyle("-fx-padding: 30;");

        Label title = new Label("🎨 APARIENCIA");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        VBox borderSection = section("Borde de la lupa");
        Slider borderSlider = new Slider(0, 10, zoomController.getBorderWidth());
        borderSlider.setShowTickLabels(true);
        borderSlider.setShowTickMarks(true);
        borderSlider.setMajorTickUnit(1);
        Label borderValue = new Label(String.format("%.1f px", zoomController.getBorderWidth()));
        borderValue.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        borderSlider.valueProperty().bindBidirectional(zoomController.borderWidthProperty());
        borderSlider.valueProperty().addListener((obs, o, v) ->
                borderValue.setText(String.format("%.1f px", v.doubleValue())));
        HBox borderRow = new HBox(15, borderSlider, borderValue);
        borderSection.getChildren().add(borderRow);

        VBox shapeSection = section("Forma de la lupa");
        ToggleGroup shapeGroup = new ToggleGroup();
        RadioButton circular = new RadioButton("Circular");
        RadioButton rectangular = new RadioButton("Rectangular");
        for (RadioButton rb : new RadioButton[]{circular, rectangular}) {
            rb.setToggleGroup(shapeGroup);
            rb.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        }
        circular.setSelected(zoomController.isLensCircular());
        rectangular.setSelected(!zoomController.isLensCircular());
        zoomController.lensCircularProperty().addListener((obs, o, v) -> {
            circular.setSelected(v);
            rectangular.setSelected(!v);
        });
        circular.setOnAction(e -> zoomController.setLensCircular(true));
        rectangular.setOnAction(e -> zoomController.setLensCircular(false));
        HBox shapeRow = new HBox(20, circular, rectangular);
        shapeSection.getChildren().add(shapeRow);

        getChildren().addAll(title, borderSection, shapeSection);
    }

    private VBox section(String label) {
        VBox box = new VBox(8);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }
}
