package com.forzen.input;

import com.forzen.App;
import com.forzen.core.ZoomController;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.awt.event.KeyEvent;

public class HotkeyManager implements NativeKeyListener {

    private final ZoomController zoomController;
    private final App app;

    public HotkeyManager(ZoomController zoomController, App app) {
        this.zoomController = zoomController;
        this.app = app;
    }

    public void register() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException e) {
            System.err.println("Failed to register global hotkeys: " + e.getMessage());
        }
    }

    public void unregister() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            System.err.println("Failed to unregister hotkeys: " + e.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int mods = e.getModifiers();
        int key = e.getKeyCode();

        boolean ctrlAlt = (mods & NativeKeyEvent.CTRL_MASK) != 0
                       && (mods & NativeKeyEvent.ALT_MASK) != 0;

        if (!ctrlAlt) return;

        switch (key) {
            case KeyEvent.VK_UP -> {
                zoomController.zoomIn();
                System.out.println("Zoom in: " + zoomController.getZoomLevel());
            }
            case KeyEvent.VK_DOWN -> {
                zoomController.zoomOut();
                System.out.println("Zoom out: " + zoomController.getZoomLevel());
            }
            case KeyEvent.VK_Z -> {
                zoomController.toggleRunning();
                System.out.println("Forzen " + (zoomController.isRunning() ? "resumed" : "paused"));
            }
            case KeyEvent.VK_M -> {
                com.forzen.core.ZoomMode[] modes = com.forzen.core.ZoomMode.values();
                int next = (zoomController.getMode().ordinal() + 1) % modes.length;
                zoomController.setMode(modes[next]);
                System.out.println("Mode: " + zoomController.getMode());
            }
            case KeyEvent.VK_COMMA -> {
                javafx.application.Platform.runLater(() -> {
                    new com.forzen.ui.SettingsWindow(zoomController);
                });
            }
            case KeyEvent.VK_X -> {
                app.shutdown();
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}
