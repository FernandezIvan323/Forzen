package com.forzen.input;

import com.forzen.App;
import com.forzen.config.ConfigStore;
import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javafx.application.Platform;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HotkeyManager implements NativeKeyListener {

    private final ZoomController zoomController;
    private final App app;
    private final ConfigStore configStore;
    private final Map<HotkeyAction, HotkeyBinding> bindings = new EnumMap<>(HotkeyAction.class);

    /** When true, next key press is captured for rebinding (settings UI). */
    private volatile HotkeyAction capturingFor;
    private volatile Runnable onCaptureDone;

    public HotkeyManager(ZoomController zoomController, App app, ConfigStore configStore) {
        this.zoomController = zoomController;
        this.app = app;
        this.configStore = configStore;
        reloadBindings();
    }

    public void reloadBindings() {
        for (HotkeyAction action : HotkeyAction.values()) {
            bindings.put(action, configStore.getHotkey(action));
        }
    }

    public HotkeyBinding getBinding(HotkeyAction action) {
        return bindings.getOrDefault(action, HotkeyBinding.defaults(action));
    }

    public void setBinding(HotkeyAction action, HotkeyBinding binding) {
        bindings.put(action, binding);
        configStore.setHotkey(action, binding);
    }

    public void beginCapture(HotkeyAction action, Runnable onDone) {
        this.capturingFor = action;
        this.onCaptureDone = onDone;
    }

    public void cancelCapture() {
        capturingFor = null;
        onCaptureDone = null;
    }

    public boolean isCapturing() {
        return capturingFor != null;
    }

    public void register() {
        // Silence JNativeHook spam
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            GlobalScreen.addNativeKeyListener(this);
            System.out.println("Hotkeys registered (customizable in Settings → Controles)");
        } catch (NativeHookException e) {
            System.err.println("Failed to register global hotkeys: " + e.getMessage());
        }
    }

    public void unregister() {
        try {
            GlobalScreen.removeNativeKeyListener(this);
            if (GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.unregisterNativeHook();
            }
        } catch (NativeHookException e) {
            System.err.println("Failed to unregister hotkeys: " + e.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int key = e.getKeyCode();
        int mods = e.getModifiers();

        // Ignore pure modifier keys during normal use
        if (key == NativeKeyEvent.VC_CONTROL
                || key == NativeKeyEvent.VC_ALT
                || key == NativeKeyEvent.VC_SHIFT
                || key == NativeKeyEvent.VC_META) {
            return;
        }

        HotkeyAction capture = capturingFor;
        if (capture != null) {
            boolean ctrl = (mods & NativeInputEvent.CTRL_MASK) != 0
                    || (mods & NativeInputEvent.CTRL_L_MASK) != 0
                    || (mods & NativeInputEvent.CTRL_R_MASK) != 0;
            boolean alt = (mods & NativeInputEvent.ALT_MASK) != 0
                    || (mods & NativeInputEvent.ALT_L_MASK) != 0
                    || (mods & NativeInputEvent.ALT_R_MASK) != 0;
            boolean shift = (mods & NativeInputEvent.SHIFT_MASK) != 0
                    || (mods & NativeInputEvent.SHIFT_L_MASK) != 0
                    || (mods & NativeInputEvent.SHIFT_R_MASK) != 0;
            HotkeyBinding binding = new HotkeyBinding(ctrl, alt, shift, key);
            setBinding(capture, binding);
            capturingFor = null;
            Runnable done = onCaptureDone;
            onCaptureDone = null;
            if (done != null) {
                Platform.runLater(done);
            }
            return;
        }

        for (Map.Entry<HotkeyAction, HotkeyBinding> entry : bindings.entrySet()) {
            if (entry.getValue().matches(mods, key)) {
                dispatch(entry.getKey());
                return;
            }
        }
    }

    private void dispatch(HotkeyAction action) {
        switch (action) {
            case ZOOM_IN -> zoomController.zoomIn();
            case ZOOM_OUT -> zoomController.zoomOut();
            case TOGGLE_PAUSE -> zoomController.toggleRunning();
            case CYCLE_MODE -> {
                ZoomMode[] modes = ZoomMode.values();
                int next = (zoomController.getMode().ordinal() + 1) % modes.length;
                zoomController.setMode(modes[next]);
            }
            case OPEN_SETTINGS -> Platform.runLater(app::openSettings);
            case EXIT -> app.shutdown();
            case OCR_READ -> Platform.runLater(app::runOcrOnce);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}
}
