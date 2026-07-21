package com.forzen.input;

import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

/**
 * A global hotkey: optional Ctrl/Alt/Shift + native key code (VC_*).
 */
public final class HotkeyBinding {

    private final boolean ctrl;
    private final boolean alt;
    private final boolean shift;
    private final int keyCode;

    public HotkeyBinding(boolean ctrl, boolean alt, boolean shift, int keyCode) {
        this.ctrl = ctrl;
        this.alt = alt;
        this.shift = shift;
        this.keyCode = keyCode;
    }

    public boolean isCtrl() { return ctrl; }
    public boolean isAlt() { return alt; }
    public boolean isShift() { return shift; }
    public int getKeyCode() { return keyCode; }

    public boolean matches(int modifiers, int pressedKey) {
        if (pressedKey != keyCode) return false;
        boolean hasCtrl = (modifiers & NativeInputEvent.CTRL_MASK) != 0
                || (modifiers & NativeInputEvent.CTRL_L_MASK) != 0
                || (modifiers & NativeInputEvent.CTRL_R_MASK) != 0;
        boolean hasAlt = (modifiers & NativeInputEvent.ALT_MASK) != 0
                || (modifiers & NativeInputEvent.ALT_L_MASK) != 0
                || (modifiers & NativeInputEvent.ALT_R_MASK) != 0;
        boolean hasShift = (modifiers & NativeInputEvent.SHIFT_MASK) != 0
                || (modifiers & NativeInputEvent.SHIFT_L_MASK) != 0
                || (modifiers & NativeInputEvent.SHIFT_R_MASK) != 0;
        return hasCtrl == ctrl && hasAlt == alt && hasShift == shift;
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();
        if (ctrl) sb.append("Ctrl + ");
        if (alt) sb.append("Alt + ");
        if (shift) sb.append("Shift + ");
        sb.append(keyCodeToLabel(keyCode));
        return sb.toString();
    }

    public String serialize() {
        return (ctrl ? "1" : "0") + (alt ? "1" : "0") + (shift ? "1" : "0") + ":" + keyCode;
    }

    public static HotkeyBinding deserialize(String s, HotkeyBinding fallback) {
        if (s == null || s.isBlank()) return fallback;
        try {
            String[] parts = s.split(":");
            if (parts.length != 2) return fallback;
            String mods = parts[0];
            int code = Integer.parseInt(parts[1]);
            boolean c = mods.length() > 0 && mods.charAt(0) == '1';
            boolean a = mods.length() > 1 && mods.charAt(1) == '1';
            boolean sh = mods.length() > 2 && mods.charAt(2) == '1';
            return new HotkeyBinding(c, a, sh, code);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static HotkeyBinding defaults(HotkeyAction action) {
        return switch (action) {
            case ZOOM_IN -> new HotkeyBinding(true, true, false, NativeKeyEvent.VC_UP);
            case ZOOM_OUT -> new HotkeyBinding(true, true, false, NativeKeyEvent.VC_DOWN);
            case TOGGLE_PAUSE -> new HotkeyBinding(true, true, false, NativeKeyEvent.VC_Z);
            case CYCLE_MODE -> new HotkeyBinding(true, true, false, NativeKeyEvent.VC_M);
            // Ctrl+Alt+, conflicts with Visual Studio / several editors (opens random files).
            // Ctrl+Alt+O = Options / Ajustes — uncommon in IDEs.
            case OPEN_SETTINGS -> new HotkeyBinding(true, true, false, NativeKeyEvent.VC_O);
            case EXIT -> new HotkeyBinding(true, true, false, NativeKeyEvent.VC_X);
            case OCR_READ -> new HotkeyBinding(true, true, false, NativeKeyEvent.VC_T);
        };
    }

    public static String keyCodeToLabel(int code) {
        return switch (code) {
            case NativeKeyEvent.VC_UP -> "↑";
            case NativeKeyEvent.VC_DOWN -> "↓";
            case NativeKeyEvent.VC_LEFT -> "←";
            case NativeKeyEvent.VC_RIGHT -> "→";
            case NativeKeyEvent.VC_COMMA -> ",";
            case NativeKeyEvent.VC_PERIOD -> ".";
            case NativeKeyEvent.VC_SLASH -> "/";
            case NativeKeyEvent.VC_SEMICOLON -> ";";
            case NativeKeyEvent.VC_SPACE -> "Space";
            case NativeKeyEvent.VC_ENTER -> "Enter";
            case NativeKeyEvent.VC_ESCAPE -> "Esc";
            case NativeKeyEvent.VC_F1 -> "F1";
            case NativeKeyEvent.VC_F2 -> "F2";
            case NativeKeyEvent.VC_F3 -> "F3";
            case NativeKeyEvent.VC_F4 -> "F4";
            case NativeKeyEvent.VC_F5 -> "F5";
            case NativeKeyEvent.VC_F6 -> "F6";
            case NativeKeyEvent.VC_F7 -> "F7";
            case NativeKeyEvent.VC_F8 -> "F8";
            case NativeKeyEvent.VC_F9 -> "F9";
            case NativeKeyEvent.VC_F10 -> "F10";
            case NativeKeyEvent.VC_F11 -> "F11";
            case NativeKeyEvent.VC_F12 -> "F12";
            default -> {
                String name = NativeKeyEvent.getKeyText(code);
                if (name != null && !name.isBlank() && !name.startsWith("Unknown")) {
                    yield name;
                }
                yield "Key#" + code;
            }
        };
    }
}
