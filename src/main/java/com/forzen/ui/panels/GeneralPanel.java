package com.forzen.ui.panels;

import com.forzen.core.DockPosition;
import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;
import com.forzen.ui.theme.AppTheme;
import com.forzen.ui.theme.ThemeManager;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class GeneralPanel extends VBox {

    public GeneralPanel(ZoomController zoomController) {
        AppTheme theme = ThemeManager.get().current();
        setSpacing(0);
        setStyle("-fx-background-color: transparent;");

        // --- Zoom card ---
        Slider zoomSlider = new Slider(1, 8, zoomController.getZoomLevel());
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(1);
        zoomSlider.setBlockIncrement(0.5);
        zoomSlider.setMaxWidth(Double.MAX_VALUE);
        zoomSlider.valueProperty().bindBidirectional(zoomController.zoomLevelProperty());
        Label zoomValue = new Label(String.format("%.1fx", zoomController.getZoomLevel()));
        zoomValue.setStyle(theme.valueStyle());
        zoomController.zoomLevelProperty().addListener((obs, o, v) ->
                zoomValue.setText(String.format("%.1fx", v.doubleValue())));
        HBox zoomRow = new HBox(16, zoomSlider, zoomValue);
        zoomRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(zoomSlider, javafx.scene.layout.Priority.ALWAYS);

        VBox zoomCard = PanelSupport.card(theme, "Nivel de zoom", zoomRow,
                PanelSupport.hint(theme, "Usa Ctrl+Alt+↑ / ↓ o el menú de la bandeja."));

        // --- Mode segmented ---
        HBox segmentBar = new HBox(4);
        segmentBar.setStyle(theme.segmentBarStyle());
        segmentBar.setAlignment(Pos.CENTER_LEFT);

        Button lensBtn = segmentBtn("Lupa", theme);
        Button dockBtn = segmentBtn("Acoplado", theme);

        Runnable paint = () -> {
            AppTheme t = ThemeManager.get().current();
            ZoomMode m = zoomController.getMode();
            lensBtn.setStyle(m == ZoomMode.LENS ? t.segmentActiveStyle() : t.segmentIdleStyle());
            dockBtn.setStyle(m == ZoomMode.DOCKED ? t.segmentActiveStyle() : t.segmentIdleStyle());
        };
        lensBtn.setOnAction(e -> zoomController.setMode(ZoomMode.LENS));
        dockBtn.setOnAction(e -> zoomController.setMode(ZoomMode.DOCKED));
        zoomController.modeProperty().addListener((obs, o, m) -> paint.run());
        paint.run();

        segmentBar.getChildren().addAll(lensBtn, dockBtn);

        VBox modeCard = PanelSupport.card(theme, "Modo de ampliación",
                segmentBar,
                PanelSupport.hint(theme,
                        "Lupa: círculo/rectángulo que sigue el ratón. "
                                + "Acoplado: panel fijo en una esquina. Win oculta la lupa para el menú Inicio."));

        // --- Lens size ---
        Spinner<Integer> wSpin = new Spinner<>(100, 1200, (int) zoomController.getLensWidth(), 10);
        Spinner<Integer> hSpin = new Spinner<>(100, 1200, (int) zoomController.getLensHeight(), 10);
        wSpin.setEditable(true);
        hSpin.setEditable(true);
        wSpin.setPrefWidth(100);
        hSpin.setPrefWidth(100);
        wSpin.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                try { wSpin.commitValue(); } catch (Exception ignored) {}
            }
        });
        hSpin.focusedProperty().addListener((obs, was, is) -> {
            if (!is) {
                try { hSpin.commitValue(); } catch (Exception ignored) {}
            }
        });
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
        xLabel.setStyle(theme.bodyStyle());
        HBox spinRow = new HBox(10, wSpin, xLabel, hSpin);
        spinRow.setAlignment(Pos.CENTER_LEFT);

        VBox sizeCard = PanelSupport.card(theme, "Tamaño de la lupa (px)", spinRow);

        // --- Dock position ---
        ComboBox<DockPosition> dockCombo = new ComboBox<>();
        dockCombo.getItems().addAll(DockPosition.values());
        dockCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(DockPosition p) {
                return p == null ? "" : p.label();
            }
            @Override
            public DockPosition fromString(String s) { return null; }
        });
        dockCombo.getSelectionModel().select(zoomController.getDockPosition());
        dockCombo.setMaxWidth(Double.MAX_VALUE);
        dockCombo.valueProperty().addListener((obs, o, v) -> {
            if (v == null) return;
            zoomController.setDockPosition(v);
            if (zoomController.getMode() != ZoomMode.DOCKED) {
                zoomController.setMode(ZoomMode.DOCKED);
            }
        });
        zoomController.dockPositionProperty().addListener((obs, o, v) -> {
            if (v != null && dockCombo.getValue() != v) {
                dockCombo.getSelectionModel().select(v);
            }
        });

        VBox dockCard = PanelSupport.card(theme, "Posición del panel acoplado",
                dockCombo,
                PanelSupport.hint(theme,
                        "Al elegir posición se activa el modo Acoplado. Preferido: Arriba derecha."));

        getChildren().setAll(PanelSupport.page(theme,
                "General",
                "Zoom, modo de ampliación y posición del panel.",
                zoomCard, modeCard, sizeCard, dockCard));
    }

    private static Button segmentBtn(String text, AppTheme theme) {
        Button b = new Button(text);
        b.setStyle(theme.segmentIdleStyle());
        b.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(b, javafx.scene.layout.Priority.ALWAYS);
        return b;
    }
}
