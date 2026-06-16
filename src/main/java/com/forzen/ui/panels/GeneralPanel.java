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
    private static final String BORDER_GREEN = "#005A2E";
    private static final String BG_PANEL = "#1A1A1A";

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
        zoomController.zoomLevelProperty().addListener((obs, o, v) -> zoomValue.setText(String.format("%.1fx", v.doubleValue())));
        HBox zoomRow = new HBox(15, zoomSlider, zoomValue);

        VBox modeSection = section("Modo de Zoom");
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton lensMode = new RadioButton("Lens (Lupa)");
        RadioButton fullMode = new RadioButton("Full-Screen");
        RadioButton dockMode = new RadioButton("Docked (Acoplado)");
        for (RadioButton rb : new RadioButton[]{lensMode, fullMode, dockMode}) {
            rb.setToggleGroup(modeGroup);
            rb.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        }
        lensMode.setSelected(zoomController.getMode() == ZoomMode.LENS);
        fullMode.setSelected(zoomController.getMode() == ZoomMode.FULL);
        dockMode.setSelected(zoomController.getMode() == ZoomMode.DOCKED);
        lensMode.setOnAction(e -> zoomController.setMode(ZoomMode.LENS));
        fullMode.setOnAction(e -> zoomController.setMode(ZoomMode.FULL));
        dockMode.setOnAction(e -> zoomController.setMode(ZoomMode.DOCKED));
        HBox modeRow = new HBox(20, lensMode, fullMode, dockMode);

        VBox lensSection = section("Tamaño de Lupa");
        Spinner<Integer> wSpin = new Spinner<>(100, 800, (int) zoomController.getLensWidth(), 10);
        Spinner<Integer> hSpin = new Spinner<>(100, 800, (int) zoomController.getLensHeight(), 10);
        wSpin.valueProperty().addListener((obs, o, v) -> zoomController.setLensWidth(v.doubleValue()));
        hSpin.valueProperty().addListener((obs, o, v) -> zoomController.setLensHeight(v.doubleValue()));
        HBox spinRow = new HBox(10, wSpin, new Label("×"), hSpin);
        spinRow.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");

        getChildren().addAll(title, zoomSection, zoomRow, modeSection, modeRow, lensSection, spinRow);
    }

    private VBox section(String label) {
        VBox box = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }
}
