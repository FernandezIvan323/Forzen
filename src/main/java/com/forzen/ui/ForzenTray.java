package com.forzen.ui;

import com.forzen.App;
import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;

import javafx.application.Platform;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ForzenTray {

    private final ZoomController zoomController;
    private final App app;
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private MenuItem toggleItem;
    private MenuItem modeItem;

    public ForzenTray(ZoomController zoomController, App app) {
        this.zoomController = zoomController;
        this.app = app;
        init();
    }

    private void init() {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray not supported");
            return;
        }

        systemTray = SystemTray.getSystemTray();
        Image image = createTrayImage();
        trayIcon = new TrayIcon(image, "Forzen — Lupa inteligente");
        trayIcon.setImageAutoSize(true);

        PopupMenu popup = new PopupMenu();

        toggleItem = new MenuItem(zoomController.isRunning() ? "Pausar lupa" : "Reanudar lupa");
        toggleItem.addActionListener(e -> Platform.runLater(() -> {
            zoomController.toggleRunning();
            if (zoomController.isRunning() && app.getOverlay() != null) {
                app.getOverlay().restoreMagnifier();
            }
            updateLabels();
        }));
        popup.add(toggleItem);

        MenuItem restoreItem = new MenuItem("Restaurar lupa (si desapareció)");
        restoreItem.addActionListener(e -> Platform.runLater(() -> {
            zoomController.setRunning(true);
            if (app.getOverlay() != null) {
                app.getOverlay().restoreMagnifier();
            }
            updateLabels();
        }));
        popup.add(restoreItem);

        popup.addSeparator();

        modeItem = new MenuItem(modeLabel());
        modeItem.addActionListener(e -> Platform.runLater(() -> {
            ZoomMode[] modes = ZoomMode.values();
            int next = (zoomController.getMode().ordinal() + 1) % modes.length;
            zoomController.setMode(modes[next]);
            if (app.getOverlay() != null) {
                app.getOverlay().restoreMagnifier();
            }
            updateLabels();
            System.out.println("Tray: Mode → " + modes[next]);
        }));
        popup.add(modeItem);

        MenuItem zoomInItem = new MenuItem("Zoom +");
        zoomInItem.addActionListener(e -> Platform.runLater(zoomController::zoomIn));
        popup.add(zoomInItem);

        MenuItem zoomOutItem = new MenuItem("Zoom -");
        zoomOutItem.addActionListener(e -> Platform.runLater(zoomController::zoomOut));
        popup.add(zoomOutItem);

        popup.addSeparator();

        MenuItem settingsItem = new MenuItem("Ajustes (Ctrl+Alt+O)");
        settingsItem.addActionListener(e -> Platform.runLater(app::openSettings));
        popup.add(settingsItem);

        MenuItem ocrItem = new MenuItem("OCR ahora — experimental (Ctrl+Alt+T)");
        ocrItem.addActionListener(e -> Platform.runLater(app::runOcrOnce));
        popup.add(ocrItem);

        MenuItem resetHotkeysItem = new MenuItem("Restaurar atajos (Ctrl+Alt+Shift+R)");
        resetHotkeysItem.addActionListener(e -> Platform.runLater(() -> {
            if (app.getHotkeyManager() != null) {
                app.getHotkeyManager().resetAllToDefaults();
            }
        }));
        popup.add(resetHotkeysItem);

        popup.addSeparator();

        MenuItem exitItem = new MenuItem("Salir");
        exitItem.addActionListener(e -> app.shutdown());
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.addActionListener(e -> Platform.runLater(app::openSettings));

        zoomController.runningProperty().addListener((obs, o, v) ->
                Platform.runLater(this::updateLabels));
        zoomController.modeProperty().addListener((obs, o, v) ->
                Platform.runLater(this::updateLabels));

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Could not add tray icon: " + e.getMessage());
        }
    }

    private String modeLabel() {
        ZoomMode m = zoomController.getMode();
        String name = m == null ? "LENS" : m.name();
        return "Cambiar modo (ahora: " + name + ")";
    }

    private void updateLabels() {
        if (toggleItem != null) {
            toggleItem.setLabel(zoomController.isRunning() ? "Pausar lupa" : "Reanudar lupa");
        }
        if (modeItem != null) {
            modeItem.setLabel(modeLabel());
        }
    }

    private Image createTrayImage() {
        int size = 16;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0, 255, 65));
        g.fillOval(1, 1, size - 2, size - 2);
        g.setColor(new Color(13, 13, 13));
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.drawString("Z", 4, 12);
        g.dispose();
        return img;
    }

    public void shutdown() {
        if (trayIcon != null && systemTray != null) {
            systemTray.remove(trayIcon);
        }
    }
}
