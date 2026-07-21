package com.forzen.ui;

import com.forzen.config.ConfigStore;
import com.forzen.core.ZoomController;
import com.forzen.input.HotkeyManager;
import com.forzen.ocr.OcrEngine;
import com.forzen.tts.TtsEngine;
import com.forzen.ui.panels.*;
import com.forzen.ui.theme.AppTheme;
import com.forzen.ui.theme.ThemeCatalog;
import com.forzen.ui.theme.ThemeManager;
import com.forzen.win.AutostartService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.HashMap;
import java.util.Map;

public class SettingsWindow extends Stage {

    private final ZoomController zoomController;
    private final ConfigStore configStore;
    private final HotkeyManager hotkeyManager;
    private final OcrEngine ocrEngine;
    private final TtsEngine ttsEngine;
    private final StackPane contentArea;
    private final BorderPane root;
    private final VBox sidebar;
    private final VBox bottomBar;
    private final ScrollPane scroll;
    private final BorderPane mainArea;
    private final HBox titleBar;
    private Label titleLabel;
    private Label statusLabel;
    private Button closeBtn;
    private Button minBtn;
    private Button applyBtn;
    private Button resetBtn;
    private Button closeSaveBtn;
    private ImageView logoView;
    private String activePanel = "GeneralPanel";
    private final Map<String, Label> navLabels = new HashMap<>();

    private double dragOffsetX;
    private double dragOffsetY;

    public SettingsWindow(ZoomController zoomController, ConfigStore configStore,
                          HotkeyManager hotkeyManager, OcrEngine ocrEngine, TtsEngine ttsEngine) {
        this.zoomController = zoomController;
        this.configStore = configStore;
        this.hotkeyManager = hotkeyManager;
        this.ocrEngine = ocrEngine;
        this.ttsEngine = ttsEngine;

        String themeId = ThemeCatalog.resolveId(zoomController.getUiTheme());
        zoomController.setUiTheme(themeId);
        ThemeManager.get().setThemeId(themeId);

        // UNDECORATED but normal app window (not UTILITY) so it appears on the taskbar
        initStyle(StageStyle.UNDECORATED);
        initOwner(null);
        setTitle("Forzen — Ajustes");
        setWidth(920);
        setHeight(660);
        setMinWidth(720);
        setMinHeight(520);
        // Not always-on-top: user can Alt+Tab / taskbar like a normal app
        setAlwaysOnTop(false);

        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/forzen-256.png"));
            getIcons().add(icon);
        } catch (Exception ignored) {
        }

        root = new BorderPane();
        root.getStyleClass().add("root-settings");

        titleBar = createTitleBar();
        root.setTop(titleBar);

        sidebar = createSidebar();
        root.setLeft(sidebar);

        contentArea = new StackPane();
        contentArea.setAlignment(Pos.TOP_LEFT);

        scroll = new ScrollPane(contentArea);
        scroll.getStyleClass().add("settings-scroll");
        scroll.setFitToWidth(true);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.addEventFilter(ScrollEvent.SCROLL, e -> {
            if (e.getDeltaY() == 0) return;
            double contentH = contentArea.getBoundsInLocal().getHeight();
            double viewH = scroll.getViewportBounds().getHeight();
            if (contentH <= viewH) return;
            double step = e.getDeltaY() / Math.max(120.0, contentH * 0.15);
            scroll.setVvalue(clamp(scroll.getVvalue() - step, 0, 1));
            e.consume();
        });

        showPanel("GeneralPanel");

        bottomBar = createFooter();

        mainArea = new BorderPane();
        mainArea.setCenter(scroll);
        mainArea.setBottom(bottomBar);
        root.setCenter(mainArea);

        Scene scene = new Scene(root);
        try {
            var css = getClass().getResource("/styles/settings.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
        } catch (Exception ignored) {
        }
        setScene(scene);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(28);
        shadow.setOffsetY(8);
        shadow.setColor(Color.rgb(0, 0, 0, 0.45));
        root.setEffect(shadow);

        applyChrome();
        highlightNav(activePanel);

        ThemeManager.get().currentProperty().addListener((obs, o, t) -> {
            applyChrome();
            highlightNav(activePanel);
        });

        setOnCloseRequest(e -> {
            commitPendingEditors();
            persistToDisk();
        });

        setOnShown(e -> Platform.runLater(() -> {
            try {
                var hwnd = com.forzen.win.WindowNative.findHwnd(this);
                com.forzen.win.WindowNative.ensureTaskbarAppWindow(hwnd);
            } catch (Throwable ignored) {
            }
        }));
    }

    private VBox createFooter() {
        VBox bar = new VBox(10);
        bar.setPadding(new Insets(12, 20, 16, 20));

        statusLabel = new Label("Los cambios se aplican en vivo. Aplicar guarda en disco.");
        statusLabel.setWrapText(true);

        HBox btns = new HBox(10);
        btns.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        resetBtn = new Button("Restaurar");
        resetBtn.setOnAction(e -> {
            zoomController.resetDefaults();
            zoomController.setUiTheme(ThemeCatalog.defaultTheme().id());
            if (hotkeyManager != null) {
                hotkeyManager.resetAllToDefaults();
            } else {
                configStore.resetHotkeys();
            }
            ThemeManager.get().setThemeId(zoomController.getUiTheme());
            applyChrome();
            showPanel(activePanel);
            statusLabel.setText("Valores por defecto restaurados. Pulsa Aplicar para guardar.");
        });

        applyBtn = new Button("Aplicar");
        applyBtn.setOnAction(e -> saveAll(false));

        closeSaveBtn = new Button("Cerrar");
        closeSaveBtn.setOnAction(e -> saveAll(true));

        btns.getChildren().addAll(spacer, resetBtn, applyBtn, closeSaveBtn);
        bar.getChildren().addAll(statusLabel, btns);
        return bar;
    }

    private void saveAll(boolean closeAfter) {
        try {
            commitPendingEditors();
            if (hotkeyManager != null) {
                hotkeyManager.cancelCapture();
                hotkeyManager.reloadBindings();
            }
            persistToDisk();
            AutostartService.setEnabled(zoomController.isStartWithOs());
            statusLabel.setText("Guardado · " + zoomController.getMode()
                    + " · dock " + zoomController.getDockPosition()
                    + " · " + String.format("%.1fx", zoomController.getZoomLevel()));
            System.out.println("Settings saved: mode=" + zoomController.getMode()
                    + " dock=" + zoomController.getDockPosition());
            if (closeAfter) {
                close();
            }
        } catch (Throwable t) {
            statusLabel.setText("Error al guardar: " + t.getMessage());
            t.printStackTrace();
        }
    }

    private void persistToDisk() {
        configStore.saveFrom(zoomController);
    }

    private void commitPendingEditors() {
        if (getScene() != null && getScene().getRoot() != null) {
            getScene().getRoot().requestFocus();
        }
        commitNodeTree(contentArea);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void commitNodeTree(Node node) {
        if (node == null) return;
        if (node instanceof Spinner<?> spinner) {
            try {
                if (spinner.isEditable()) {
                    spinner.commitValue();
                }
            } catch (Exception ex) {
                try {
                    String text = spinner.getEditor().getText();
                    if (text != null && !text.isBlank() && spinner.getValueFactory() != null) {
                        Object parsed = spinner.getValueFactory().getConverter().fromString(text.trim());
                        if (parsed != null) {
                            ((Spinner) spinner).getValueFactory().setValue(parsed);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        if (node instanceof ComboBox<?> combo) {
            Object selected = combo.getSelectionModel() != null
                    ? combo.getSelectionModel().getSelectedItem()
                    : null;
            if (selected != null && combo.getValue() == null) {
                @SuppressWarnings("unchecked")
                ComboBox<Object> raw = (ComboBox<Object>) combo;
                raw.setValue(selected);
            }
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                commitNodeTree(child);
            }
        }
    }

    private HBox createTitleBar() {
        HBox bar = new HBox(12);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(12, 14, 12, 16));
        bar.setPrefHeight(52);

        logoView = new ImageView();
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/forzen-256.png"), 24, 24, true, true);
            logoView.setImage(icon);
        } catch (Exception ignored) {
        }
        logoView.setFitWidth(24);
        logoView.setFitHeight(24);
        logoView.setPreserveRatio(true);
        logoView.setMouseTransparent(true);

        titleLabel = new Label("Forzen  ·  Ajustes");
        titleLabel.setMouseTransparent(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        minBtn = new Button("−");
        minBtn.setOnAction(e -> setIconified(true));

        closeBtn = new Button("✕");
        closeBtn.setOnAction(e -> saveAll(true));

        bar.getChildren().addAll(logoView, titleLabel, spacer, minBtn, closeBtn);

        bar.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getTarget() == closeBtn || closeBtn.isHover()
                    || e.getTarget() == minBtn || minBtn.isHover()) return;
            dragOffsetX = e.getScreenX() - getX();
            dragOffsetY = e.getScreenY() - getY();
        });
        bar.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            if (e.getTarget() == closeBtn || e.getTarget() == minBtn) return;
            setX(e.getScreenX() - dragOffsetX);
            setY(e.getScreenY() - dragOffsetY);
        });

        return bar;
    }

    private void applyChrome() {
        AppTheme theme = ThemeManager.get().current();
        // Base chrome first
        root.setStyle(theme.cssBg() + " -fx-border-color: " + theme.border()
                + "; -fx-border-width: 1; -fx-background-radius: 12; -fx-border-radius: 12;");
        // Then control palette (combo/spinner dark fills) — merges onto root style
        if (getScene() != null) {
            ThemeManager.get().applyControlPalette(getScene());
        }

        contentArea.setStyle("-fx-background-color: transparent; -fx-padding: 12 20 8 8;");
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        bottomBar.setStyle("-fx-background-color: " + theme.panel()
                + "; -fx-border-color: " + theme.border() + "; -fx-border-width: 1 0 0 0;");
        sidebar.setStyle("-fx-background-color: " + theme.panel()
                + "; -fx-border-color: " + theme.border() + "; -fx-border-width: 0 1 0 0; -fx-padding: 12 10;");

        titleBar.setStyle("-fx-background-color: " + theme.panel()
                + "; -fx-border-color: " + theme.border() + "; -fx-border-width: 0 0 1 0; "
                + "-fx-background-radius: 12 12 0 0;");
        titleLabel.setStyle("-fx-text-fill: " + theme.text()
                + "; -fx-font-size: 14px; -fx-font-weight: bold;");
        if (statusLabel != null) {
            statusLabel.setStyle(theme.mutedStyle());
        }

        styleWindowBtn(minBtn, theme, false);
        styleWindowBtn(closeBtn, theme, true);

        if (applyBtn != null) applyBtn.setStyle(theme.applyBtnStyle());
        if (resetBtn != null) resetBtn.setStyle(theme.resetBtnStyle());
        if (closeSaveBtn != null) closeSaveBtn.setStyle(theme.secondaryBtnStyle());

        highlightNav(activePanel);
    }

    private void styleWindowBtn(Button btn, AppTheme theme, boolean isClose) {
        if (btn == null) return;
        String idle = "-fx-background-color: transparent; -fx-text-fill: " + theme.muted()
                + "; -fx-font-size: 14px; -fx-padding: 6 12; -fx-background-radius: 8; -fx-cursor: hand;";
        btn.setStyle(idle);
        String hover = isClose
                ? "-fx-background-color: " + theme.danger()
                + "; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 6 12; "
                + "-fx-background-radius: 8; -fx-cursor: hand;"
                : "-fx-background-color: " + theme.surfaceHover()
                + "; -fx-text-fill: " + theme.text() + "; -fx-font-size: 14px; -fx-padding: 6 12; "
                + "-fx-background-radius: 8; -fx-cursor: hand;";
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(idle));
    }

    private VBox createSidebar() {
        VBox sb = new VBox(4);
        sb.setPrefWidth(210);
        sb.setMinWidth(200);

        Label brand = new Label("SECCIONES");
        brand.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 4 12 8 12;");
        sb.getChildren().add(brand);

        String[][] items = {
                {"General", "GeneralPanel"},
                {"Apariencia", "AppearancePanel"},
                {"Filtros", "FiltersPanel"},
                {"Controles", "ControlsPanel"},
                {"Texto", "TextPanel"},
                {"Comportamiento", "BehaviorPanel"},
                {"Avanzado", "AdvancedPanel"}
        };

        for (String[] item : items) {
            Label lbl = new Label(item[0]);
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setUserData(item[1]);
            lbl.getStyleClass().add("nav-item");
            navLabels.put(item[1], lbl);
            lbl.setOnMouseClicked(e -> {
                commitPendingEditors();
                showPanel((String) lbl.getUserData());
            });
            sb.getChildren().add(lbl);
        }
        return sb;
    }

    private void highlightNav(String panelName) {
        AppTheme theme = ThemeManager.get().current();
        // brand label
        if (!sidebar.getChildren().isEmpty() && sidebar.getChildren().get(0) instanceof Label brand) {
            brand.setStyle(theme.sectionLabelStyle() + " -fx-padding: 4 12 8 12;");
        }
        for (Map.Entry<String, Label> e : navLabels.entrySet()) {
            boolean active = e.getKey().equals(panelName);
            Label lbl = e.getValue();
            if (active) {
                lbl.setStyle(theme.sidebarItemActiveStyle());
            } else {
                lbl.setStyle(theme.sidebarItemStyle());
                lbl.setOnMouseEntered(ev -> {
                    if (!e.getKey().equals(activePanel)) {
                        lbl.setStyle(theme.sidebarItemHoverStyle());
                    }
                });
                lbl.setOnMouseExited(ev -> {
                    if (!e.getKey().equals(activePanel)) {
                        lbl.setStyle(theme.sidebarItemStyle());
                    }
                });
            }
        }
    }

    private void showPanel(String panelName) {
        commitPendingEditors();
        activePanel = panelName;
        Node panel = switch (panelName) {
            case "AppearancePanel" -> new AppearancePanel(zoomController);
            case "FiltersPanel" -> new FiltersPanel(zoomController);
            case "ControlsPanel" -> new ControlsPanel(zoomController, hotkeyManager);
            case "TextPanel" -> new TextPanel(zoomController, ocrEngine, ttsEngine);
            case "BehaviorPanel" -> new BehaviorPanel(zoomController);
            case "AdvancedPanel" -> new AdvancedPanel(zoomController);
            default -> new GeneralPanel(zoomController);
        };
        contentArea.getChildren().setAll(panel);
        if (scroll != null) {
            scroll.setVvalue(0);
        }
        highlightNav(panelName);
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
