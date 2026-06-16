package com.forzen.ui;

import com.forzen.config.ConfigStore;
import com.forzen.core.ZoomController;
import com.forzen.ui.panels.*;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsWindow extends Stage {

    private static final String BG_DARK = "#0D0D0D";
    private static final String BG_PANEL = "#1A1A1A";
    private static final String GREEN_NEON = "#00FF41";
    private static final String RED_NEON = "#FF003C";
    private static final String TEXT_MUTED = "#AAAAAA";

    private final ZoomController zoomController;
    private final ConfigStore configStore;
    private final StackPane contentArea;

    public SettingsWindow(ZoomController zoomController) {
        this(zoomController, new ConfigStore());
    }

    public SettingsWindow(ZoomController zoomController, ConfigStore configStore) {
        this.zoomController = zoomController;
        this.configStore = configStore;

        initStyle(StageStyle.DECORATED);
        setTitle("Forzen — Ajustes");
        setWidth(800);
        setHeight(600);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        root.setLeft(createSidebar());

        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + BG_DARK + ";");
        setPanel(new GeneralPanel(zoomController));

        ScrollPane scroll = new ScrollPane(contentArea);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: " + BG_DARK + "; -fx-background: " + BG_DARK + ";");

        VBox bottomBar = new VBox(10);
        bottomBar.setStyle("-fx-background-color: " + BG_PANEL + "; -fx-padding: 10 20;");
        HBox btns = new HBox(10);
        Button applyBtn = new Button("APLICAR");
        applyBtn.setStyle("-fx-background-color: " + GREEN_NEON + "; -fx-text-fill: " + BG_DARK + "; -fx-font-weight: bold; -fx-padding: 10 30;");
        applyBtn.setOnAction(e -> {
            configStore.saveFrom(zoomController);
            close();
        });
        Button resetBtn = new Button("RESTAURAR");
        resetBtn.setStyle("-fx-background-color: " + RED_NEON + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30;");
        btns.getChildren().addAll(applyBtn, resetBtn);
        bottomBar.getChildren().add(btns);

        BorderPane mainArea = new BorderPane();
        mainArea.setCenter(scroll);
        mainArea.setBottom(bottomBar);
        root.setCenter(mainArea);

        Scene scene = new Scene(root);
        setScene(scene);
        show();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(180);
        sidebar.setPadding(new Insets(10));
        sidebar.setStyle("-fx-background-color: " + BG_PANEL + ";");

        String[][] items = {
            {"🔍 General", "GeneralPanel"},
            {"🎨 Apariencia", "AppearancePanel"},
            {"🌈 Filtros", "FiltersPanel"},
            {"⌨️ Controles", "ControlsPanel"},
            {"📝 Texto", "TextPanel"},
            {"⚙️ Comportamiento", "BehaviorPanel"},
            {"🖥️ Avanzado", "AdvancedPanel"}
        };

        for (String[] item : items) {
            Label lbl = new Label(item[0]);
            lbl.setUserData(item[1]);
            lbl.setStyle(MUTED_STYLE);
            lbl.setOnMouseEntered(e -> lbl.setStyle(HOVER_STYLE));
            lbl.setOnMouseExited(e -> lbl.setStyle(MUTED_STYLE));
            lbl.setOnMouseClicked(e -> switchPanel((String) lbl.getUserData()));
            sidebar.getChildren().add(lbl);
        }

        return sidebar;
    }

    private void switchPanel(String panelName) {
        switch (panelName) {
            case "GeneralPanel" -> setPanel(new GeneralPanel(zoomController));
            case "AppearancePanel" -> setPanel(new AppearancePanel(zoomController));
            case "FiltersPanel" -> setPanel(new FiltersPanel(zoomController));
            case "ControlsPanel" -> setPanel(new ControlsPanel(zoomController));
            case "TextPanel" -> setPanel(new TextPanel(zoomController));
            case "BehaviorPanel" -> setPanel(new BehaviorPanel(zoomController));
            case "AdvancedPanel" -> setPanel(new AdvancedPanel(zoomController));
        }
    }

    private void setPanel(Node panel) {
        contentArea.getChildren().setAll(panel);
    }

    private static final String MUTED_STYLE = "-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px; -fx-padding: 8 12;";
    private static final String HOVER_STYLE = "-fx-text-fill: " + GREEN_NEON + "; -fx-font-size: 14px; -fx-padding: 8 12; -fx-cursor: hand;";
}
