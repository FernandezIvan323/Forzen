package com.forzen.ui;

import com.forzen.App;
import com.forzen.core.ZoomController;

import javafx.application.Platform;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ForzenTray {

    private final ZoomController zoomController;
    private final App app;
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private MenuItem toggleItem;

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

        toggleItem = new MenuItem(zoomController.isRunning() ? "Pausar" : "Reanudar");
        toggleItem.addActionListener(e -> {
            zoomController.toggleRunning();
            updateToggleLabel();
        });
        popup.add(toggleItem);

        MenuItem settingsItem = new MenuItem("Ajustes");
        settingsItem.addActionListener(e -> Platform.runLater(app::openSettings));
        popup.add(settingsItem);

        MenuItem ocrItem = new MenuItem("OCR ahora (Ctrl+Alt+T)");
        ocrItem.addActionListener(e -> Platform.runLater(app::runOcrOnce));
        popup.add(ocrItem);

        popup.addSeparator();

        MenuItem exitItem = new MenuItem("Salir");
        exitItem.addActionListener(e -> app.shutdown());
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);
        trayIcon.addActionListener(e -> Platform.runLater(app::openSettings));

        zoomController.runningProperty().addListener((obs, o, v) ->
                Platform.runLater(this::updateToggleLabel));

        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Could not add tray icon: " + e.getMessage());
        }
    }

    private void updateToggleLabel() {
        if (toggleItem != null) {
            toggleItem.setLabel(zoomController.isRunning() ? "Pausar" : "Reanudar");
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
