package com.forzen.ui;

import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsWindow extends Stage {

    private static final String BG_DARK = "#0D0D0D";
    private static final String BG_PANEL = "#1A1A1A";
    private static final String GREEN_NEON = "#00FF41";
    private static final String RED_NEON = "#FF003C";
    private static final String TEXT_LIGHT = "#EAEAEA";
    private static final String TEXT_MUTED = "#AAAAAA";
    private static final String BORDER_GREEN = "#005A2E";

    private final ZoomController zoomController;

    public SettingsWindow(ZoomController zoomController) {
        this.zoomController = zoomController;

        initStyle(StageStyle.DECORATED);
        setTitle("Forzen — Ajustes");
        setWidth(800);
        setHeight(600);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        root.setLeft(createSidebar());
        root.setCenter(createGeneralPanel());

        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(
            "data:text/css," + getInlineStyles()
        );
        setScene(scene);
        show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(180);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: " + BG_PANEL + ";");

        String[] items = {
            "🔍 General", "🎨 Apariencia", "🌈 Filtros",
            "⌨️ Controles", "📝 Texto", "⚙️ Comportamiento", "🖥️ Avanzado"
        };

        for (String item : items) {
            Label lbl = new Label(item);
            lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px; -fx-padding: 8 12;");
            lbl.setOnMouseEntered(e -> lbl.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 14px; -fx-padding: 8 12; -fx-cursor: hand;"));
            lbl.setOnMouseExited(e -> lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px; -fx-padding: 8 12;"));
            sidebar.getChildren().add(lbl);
        }

        return sidebar;
    }

    private VBox createGeneralPanel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(30));

        Label title = new Label("⚙ AJUSTES GENERALES");
        title.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Zoom level slider
        VBox zoomSection = sectionBox("Nivel de Zoom");
        Slider zoomSlider = new Slider(1, 8, zoomController.getZoomLevel());
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(1);
        zoomSlider.setBlockIncrement(0.5);
        zoomSlider.valueProperty().bindBidirectional(zoomController.zoomLevelProperty());
        zoomSlider.setStyle("-fx-control-inner-background: " + BORDER_GREEN + ";");
        Label zoomValue = new Label(String.format("%.1fx", zoomController.getZoomLevel()));
        zoomValue.setStyle("-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 24px; -fx-font-weight: bold;");
        zoomController.zoomLevelProperty().addListener((obs, old, val) ->
            zoomValue.setText(String.format("%.1fx", val.doubleValue()))
        );
        HBox zoomRow = new HBox(15, zoomSlider, zoomValue);

        // Mode selector
        VBox modeSection = sectionBox("Modo de Zoom");
        ToggleGroup modeGroup = new ToggleGroup();
        RadioButton lensMode = new RadioButton("Lens (Lupa)");
        RadioButton fullMode = new RadioButton("Full-Screen");
        RadioButton dockMode = new RadioButton("Docked (Acoplado)");
        for (RadioButton rb : new RadioButton[]{lensMode, fullMode, dockMode}) {
            rb.setToggleGroup(modeGroup);
            rb.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");
        }
        lensMode.setSelected(true);
        lensMode.setOnAction(e -> zoomController.setMode(ZoomMode.LENS));
        fullMode.setOnAction(e -> zoomController.setMode(ZoomMode.FULL));
        dockMode.setOnAction(e -> zoomController.setMode(ZoomMode.DOCKED));

        // Lens size
        VBox lensSection = sectionBox("Tamaño de Lupa (ancho × alto)");
        Spinner<Integer> widthSpinner = new Spinner<>(100, 800, (int) zoomController.getLensWidth(), 10);
        Spinner<Integer> heightSpinner = new Spinner<>(100, 800, (int) zoomController.getLensHeight(), 10);
        widthSpinner.valueProperty().addListener((obs, old, val) -> zoomController.setLensWidth(val.doubleValue()));
        heightSpinner.valueProperty().addListener((obs, old, val) -> zoomController.setLensHeight(val.doubleValue()));
        HBox lensSizeRow = new HBox(10, widthSpinner, new Label("×"), heightSpinner);
        lensSizeRow.setStyle("-fx-text-fill: " + TEXT_LIGHT + ";");

        HBox bottomBtns = new HBox(10);
        Button applyBtn = new Button("APLICAR");
        applyBtn.setStyle("-fx-background-color: " + GREEN_NEON + "; -fx-text-fill: " + BG_DARK + "; -fx-font-weight: bold; -fx-padding: 10 30;");
        applyBtn.setOnAction(e -> close());
        Button resetBtn = new Button("RESTAURAR");
        resetBtn.setStyle("-fx-background-color: " + RED_NEON + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");
        bottomBtns.getChildren().addAll(applyBtn, resetBtn);

        panel.getChildren().addAll(title, zoomSection, zoomRow, modeSection, lensSection, lensSizeRow, bottomBtns);
        return panel;
    }

    private VBox sectionBox(String label) {
        VBox box = new VBox(5);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        box.getChildren().add(lbl);
        return box;
    }

    private String getInlineStyles() {
        return """
            .slider .axis { -fx-tick-label-fill: %s; }
            .slider .thumb { -fx-background-color: %s; }
            .slider .track { -fx-background-color: %s; }
            .spinner { -fx-text-fill: %s; }
            .spinner .increment-arrow-button, .spinner .decrement-arrow-button {
                -fx-background-color: %s;
            }
            .radio-button .radio { -fx-mark-color: %s; -fx-background-color: %s; }
            """.formatted(TEXT_MUTED, GREEN_NEON, BORDER_GREEN, TEXT_LIGHT, BG_PANEL, GREEN_NEON, BG_PANEL);
    }
}
