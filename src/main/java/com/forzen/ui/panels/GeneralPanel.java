package com.forzen.ui.panels;

import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class GeneralPanel extends VBox {

    private static final String GREEN_NEON = "#00FF41";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";

    public GeneralPanel(ZoomController zoomController) {
        setSpacing(20);
        setStyle("-fx-padding: 30;");

        Label title = new Label("⚙ AJUSTES GENERALES");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        VBox zoomSection = section("Nivel de Zoom");
        Slider zoomSlider = new Slider(1, 8, zoomController.getZoomLevel());
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(1);
        zoomSlider.setBlockIncrement(0.5);
        zoomSlider.valueProperty().bindBidirectional(zoomController.zoomLevelProperty());
        Label zoomValue = new Label(String.format("%.1fx", zoomController.getZoomLevel()));
        zoomValue.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        zoomController.zoomLevelProperty().addListener((obs, o, v) ->
                zoomValue.setText(String.format("%.1fx", v.doubleValue())));
        HBox zoomRow = new HBox(15, zoomSlider, zoomValue);
        zoomSection.getChildren().add(zoomRow);

        VBox modeSection = section("Modo de Zoom");
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton lensMode = new RadioButton("Lens (Lupa)");
        RadioButton fullMode = new RadioButton("Full-Screen");
        RadioButton dockMode = new RadioButton("Docked (Acoplado)");
        for (RadioButton rb : new RadioButton[]{lensMode, fullMode, dockMode}) {
            rb.setToggleGroup(modeGroup);
            rb.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        }
        selectMode(zoomController.getMode(), lensMode, fullMode, dockMode);
        zoomController.modeProperty().addListener((obs, o, m) ->
                selectMode(m, lensMode, fullMode, dockMode));
        lensMode.setOnAction(e -> zoomController.setMode(ZoomMode.LENS));
        fullMode.setOnAction(e -> zoomController.setMode(ZoomMode.FULL));
        dockMode.setOnAction(e -> zoomController.setMode(ZoomMode.DOCKED));
        HBox modeRow = new HBox(20, lensMode, fullMode, dockMode);
        modeSection.getChildren().add(modeRow);

        VBox lensSection = section("Tamaño de Lupa (px lógicos)");
        Spinner<Integer> wSpin = new Spinner<>(100, 1200, (int) zoomController.getLensWidth(), 10);
        Spinner<Integer> hSpin = new Spinner<>(100, 1200, (int) zoomController.getLensHeight(), 10);
        wSpin.setEditable(true);
        hSpin.setEditable(true);
        wSpin.valueProperty().addListener((obs, o, v) -> {
            if (v != null) zoomController.setLensWidth(v.doubleValue());
        });
        hSpin.valueProperty().addListener((obs, o, v) -> {
            if (v != null) zoomController.setLensHeight(v.doubleValue());
        });
        zoomController.lensWidthProperty().addListener((obs, o, v) -> {
            int iv = v.intValue();
            if (wSpin.getValue() == null || wSpin.getValue() != iv) {
                wSpin.getValueFactory().setValue(iv);
            }
        });
        zoomController.lensHeightProperty().addListener((obs, o, v) -> {
            int iv = v.intValue();
            if (hSpin.getValue() == null || hSpin.getValue() != iv) {
                hSpin.getValueFactory().setValue(iv);
            }
        });
        Label xLabel = new Label("×");
        xLabel.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        HBox spinRow = new HBox(10, wSpin, xLabel, hSpin);
        lensSection.getChildren().add(spinRow);

        getChildren().addAll(title, zoomSection, modeSection, lensSection);
    }

    private void selectMode(ZoomMode mode, RadioButton lens, RadioButton full, RadioButton dock) {
        if (mode == null) return;
        switch (mode) {
            case FULL -> full.setSelected(true);
            case DOCKED -> dock.setSelected(true);
            default -> lens.setSelected(true);
        }
    }

    private VBox section(String label) {
        VBox box = new VBox(8);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }
}
