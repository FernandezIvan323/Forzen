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
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HotkeyManager implements NativeKeyListener {

    private final ZoomController zoomController;
    private final App app;
    private final ConfigStore configStore;
    private final Map<HotkeyAction, HotkeyBinding> bindings = new EnumMap<>(HotkeyAction.class);

    private volatile HotkeyAction capturingFor;
    private volatile BiConsumer<Boolean, String> onCaptureResult;
    private volatile long captureStartedAtMs;
    private volatile boolean registered;
    private long lastDebugLogMs;

    public HotkeyManager(ZoomController zoomController, App app, ConfigStore configStore) {
        this.zoomController = zoomController;
        this.app = app;
        this.configStore = configStore;
        reloadBindings();
    }

    public void reloadBindings() {
        for (HotkeyAction action : HotkeyAction.values()) {
            HotkeyBinding b = configStore.getHotkey(action);
            if (b == null) {
                b = HotkeyBinding.defaults(action);
            }
            bindings.put(action, b);
        }
        migrateLegacySettingsHotkey();
        printBindings();
    }

    private void printBindings() {
        System.out.println("Hotkey bindings:");
        for (HotkeyAction a : HotkeyAction.values()) {
            HotkeyBinding b = bindings.get(a);
            System.out.println("  " + a + " → " + (b != null ? b.toDisplayString() : "(null)"));
        }
    }

    /**
     * Ctrl+Alt+, was the old OPEN_SETTINGS default but Visual Studio / editors steal it
     * (often open a random .json). Migrate saved prefs that still use that combo.
     */
    private void migrateLegacySettingsHotkey() {
        HotkeyBinding current = bindings.get(HotkeyAction.OPEN_SETTINGS);
        if (current == null) return;
        boolean legacyComma = current.isCtrl() && current.isAlt() && !current.isShift()
                && current.getKeyCode() == NativeKeyEvent.VC_COMMA;
        if (!legacyComma) return;

        HotkeyBinding next = HotkeyBinding.defaults(HotkeyAction.OPEN_SETTINGS);
        bindings.put(HotkeyAction.OPEN_SETTINGS, next);
        configStore.setHotkey(HotkeyAction.OPEN_SETTINGS, next);
        System.out.println("Migrated OPEN_SETTINGS hotkey: Ctrl+Alt+, → " + next.toDisplayString()
                + " (avoid VS/editor conflict)");
    }

    public HotkeyBinding getBinding(HotkeyAction action) {
        return bindings.getOrDefault(action, HotkeyBinding.defaults(action));
    }

    public void setBinding(HotkeyAction action, HotkeyBinding binding) {
        bindings.put(action, binding);
        configStore.setHotkey(action, binding);
    }

    public void beginCapture(HotkeyAction action, BiConsumer<Boolean, String> onResult) {
        this.capturingFor = action;
        this.onCaptureResult = onResult;
        this.captureStartedAtMs = System.currentTimeMillis();
        System.out.println("Hotkey capture started for " + action);
    }

    @Deprecated
    public void beginCapture(HotkeyAction action, Runnable onDone) {
        beginCapture(action, (ok, msg) -> {
            if (onDone != null) onDone.run();
        });
    }

    public void cancelCapture() {
        if (capturingFor != null) {
            System.out.println("Hotkey capture cancelled (was " + capturingFor + ")");
        }
        capturingFor = null;
        onCaptureResult = null;
        captureStartedAtMs = 0;
    }

    public boolean isCapturing() {
        return capturingFor != null;
    }

    public HotkeyAction findConflict(HotkeyAction self, HotkeyBinding binding) {
        if (binding == null) return null;
        for (Map.Entry<HotkeyAction, HotkeyBinding> e : bindings.entrySet()) {
            if (e.getKey() == self) continue;
            HotkeyBinding other = e.getValue();
            if (other != null
                    && other.getKeyCode() == binding.getKeyCode()
                    && other.isCtrl() == binding.isCtrl()
                    && other.isAlt() == binding.isAlt()
                    && other.isShift() == binding.isShift()) {
                return e.getKey();
            }
        }
        return null;
    }

    /** Restore factory defaults and clear stuck capture. Call if hotkeys feel dead. */
    public void resetAllToDefaults() {
        cancelCapture();
        for (HotkeyAction a : HotkeyAction.values()) {
            setBinding(a, HotkeyBinding.defaults(a));
        }
        reloadBindings();
        // Re-register hook in case it was dropped
        register();
        System.out.println("Hotkeys reset to defaults + re-registered");
    }

    public void register() {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);

        try {
            if (!GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.registerNativeHook();
            }
            try {
                GlobalScreen.removeNativeKeyListener(this);
            } catch (Exception ignored) {
            }
            GlobalScreen.addNativeKeyListener(this);
            registered = true;
            System.out.println("Hotkeys registered (hook OK). Defaults: Ctrl+Alt+M mode, Ctrl+Alt+O settings, Ctrl+Alt+↑/↓ zoom");
            printBindings();
        } catch (NativeHookException e) {
            registered = false;
            System.err.println("Failed to register global hotkeys: " + e.getMessage());
            e.printStackTrace();
        } catch (Throwable t) {
            registered = false;
            System.err.println("Hotkey register crashed: " + t.getMessage());
            t.printStackTrace();
        }
    }

    public boolean isRegistered() {
        return registered && GlobalScreen.isNativeHookRegistered();
    }

    public void unregister() {
        cancelCapture();
        try {
            GlobalScreen.removeNativeKeyListener(this);
            if (GlobalScreen.isNativeHookRegistered()) {
                GlobalScreen.unregisterNativeHook();
            }
            registered = false;
        } catch (NativeHookException e) {
            System.err.println("Failed to unregister hotkeys: " + e.getMessage());
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        int key = e.getKeyCode();
        int mods = e.getModifiers();

        // Windows key alone → yield magnifier for Start menu (do not swallow other keys)
        if (key == NativeKeyEvent.VC_META) {
            Platform.runLater(() -> {
                try {
                    if (app != null && app.getOverlay() != null) {
                        app.getOverlay().yieldToShellUi();
                    }
                } catch (Throwable ignored) {
                }
            });
            return;
        }

        if (key == NativeKeyEvent.VC_CONTROL
                || key == NativeKeyEvent.VC_ALT
                || key == NativeKeyEvent.VC_SHIFT) {
            return;
        }

        // Auto-expire stuck rebind after 15s
        if (capturingFor != null && captureStartedAtMs > 0
                && System.currentTimeMillis() - captureStartedAtMs > 15_000L) {
            System.out.println("Hotkey capture timed out — cleared");
            cancelCapture();
        }

        HotkeyAction capture = capturingFor;
        if (capture != null) {
            handleCapture(capture, key, mods);
            return;
        }

        // Emergency: Ctrl+Alt+Shift+R restores default hotkeys if user is stuck
        if (hasCtrl(mods) && hasAlt(mods) && hasShift(mods) && key == NativeKeyEvent.VC_R) {
            Platform.runLater(this::resetAllToDefaults);
            return;
        }

        // Emergency: Ctrl+Alt+Shift+L force-restores the lens if it disappeared
        if (hasCtrl(mods) && hasAlt(mods) && hasShift(mods) && key == NativeKeyEvent.VC_L) {
            Platform.runLater(() -> {
                if (app != null && app.getOverlay() != null) {
                    app.getOverlay().restoreMagnifier();
                }
            });
            return;
        }

        boolean matched = false;
        for (Map.Entry<HotkeyAction, HotkeyBinding> entry : bindings.entrySet()) {
            HotkeyBinding b = entry.getValue();
            if (b != null && b.matches(mods, key)) {
                HotkeyAction action = entry.getKey();
                matched = true;
                System.out.println("Hotkey match: " + action + " (" + b.toDisplayString() + ")");
                Platform.runLater(() -> dispatchOnFx(action));
                return;
            }
        }

        // Debug: log Ctrl/Alt combos that didn't match (throttle)
        if (!matched && (hasCtrl(mods) || hasAlt(mods))) {
            long now = System.currentTimeMillis();
            if (now - lastDebugLogMs > 800) {
                lastDebugLogMs = now;
                System.out.printf(
                        "Hotkey no-match: key=%d (%s) mods=0x%X ctrl=%s alt=%s shift=%s hook=%s capture=%s%n",
                        key, NativeKeyEvent.getKeyText(key), mods,
                        hasCtrl(mods), hasAlt(mods), hasShift(mods),
                        isRegistered(), capturingFor);
            }
        }
    }

    private void handleCapture(HotkeyAction capture, int key, int mods) {
        if (key == NativeKeyEvent.VC_ESCAPE) {
            cancelCapture();
            BiConsumer<Boolean, String> cb = onCaptureResult;
            onCaptureResult = null;
            if (cb != null) {
                Platform.runLater(() -> cb.accept(false, "Captura cancelada."));
            }
            return;
        }

        boolean ctrl = hasCtrl(mods);
        boolean alt = hasAlt(mods);
        boolean shift = hasShift(mods);

        if (!ctrl && !alt && !shift) {
            BiConsumer<Boolean, String> cb = onCaptureResult;
            if (cb != null) {
                Platform.runLater(() -> cb.accept(false,
                        "Usa al menos un modificador (Ctrl / Alt / Shift) + tecla. Esc cancela."));
            }
            return;
        }

        HotkeyBinding binding = new HotkeyBinding(ctrl, alt, shift, key);
        HotkeyAction conflict = findConflict(capture, binding);
        if (conflict != null) {
            BiConsumer<Boolean, String> cb = onCaptureResult;
            if (cb != null) {
                Platform.runLater(() -> cb.accept(false,
                        "Conflicto con " + conflict.name() + ". Prueba otro combo (Esc cancela)."));
            }
            return;
        }

        setBinding(capture, binding);
        System.out.println("Hotkey saved: " + capture + " → " + binding.toDisplayString());

        capturingFor = null;
        captureStartedAtMs = 0;
        BiConsumer<Boolean, String> cb = onCaptureResult;
        onCaptureResult = null;
        if (cb != null) {
            Platform.runLater(() -> cb.accept(true, "Guardado: " + binding.toDisplayString()));
        }
    }

    private void dispatchOnFx(HotkeyAction action) {
        try {
            // Always clear accidental capture state before acting
            if (capturingFor != null && action != HotkeyAction.OPEN_SETTINGS) {
                // leave capture alone if rebinding
            }
            switch (action) {
                case ZOOM_IN -> {
                    zoomController.zoomIn();
                    System.out.println("Zoom → " + zoomController.getZoomLevel());
                }
                case ZOOM_OUT -> {
                    zoomController.zoomOut();
                    System.out.println("Zoom → " + zoomController.getZoomLevel());
                }
                case TOGGLE_PAUSE -> {
                    zoomController.toggleRunning();
                    if (zoomController.isRunning() && app != null && app.getOverlay() != null) {
                        app.getOverlay().restoreMagnifier();
                    }
                    System.out.println("Running → " + zoomController.isRunning());
                }
                case CYCLE_MODE -> {
                    ZoomMode[] modes = ZoomMode.values();
                    int next = (zoomController.getMode().ordinal() + 1) % modes.length;
                    zoomController.setMode(modes[next]);
                    if (app != null && app.getOverlay() != null) {
                        app.getOverlay().restoreMagnifier();
                    }
                    System.out.println("Mode → " + modes[next]);
                }
                case OPEN_SETTINGS -> {
                    System.out.println("Hotkey: OPEN_SETTINGS");
                    app.openSettings();
                }
                case EXIT -> app.shutdown();
                case OCR_READ -> app.runOcrOnce();
            }
        } catch (Throwable t) {
            System.err.println("Hotkey dispatch failed for " + action + ": " + t.getMessage());
            t.printStackTrace();
            cancelCapture();
        }
    }

    private static boolean hasCtrl(int mods) {
        return (mods & NativeInputEvent.CTRL_MASK) != 0
                || (mods & NativeInputEvent.CTRL_L_MASK) != 0
                || (mods & NativeInputEvent.CTRL_R_MASK) != 0;
    }

    private static boolean hasAlt(int mods) {
        return (mods & NativeInputEvent.ALT_MASK) != 0
                || (mods & NativeInputEvent.ALT_L_MASK) != 0
                || (mods & NativeInputEvent.ALT_R_MASK) != 0;
    }

    private static boolean hasShift(int mods) {
        return (mods & NativeInputEvent.SHIFT_MASK) != 0
                || (mods & NativeInputEvent.SHIFT_L_MASK) != 0
                || (mods & NativeInputEvent.SHIFT_R_MASK) != 0;
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {}

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}
}
