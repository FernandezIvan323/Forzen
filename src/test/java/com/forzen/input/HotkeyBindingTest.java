package com.forzen.input;

import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HotkeyBindingTest {

    @Test
    void serializeRoundTrip() {
        HotkeyBinding b = new HotkeyBinding(true, true, false, NativeKeyEvent.VC_T);
        HotkeyBinding again = HotkeyBinding.deserialize(b.serialize(), HotkeyBinding.defaults(HotkeyAction.EXIT));
        assertTrue(again.isCtrl());
        assertTrue(again.isAlt());
        assertFalse(again.isShift());
        assertEquals(NativeKeyEvent.VC_T, again.getKeyCode());
    }

    @Test
    void matchesCtrlAltUp() {
        HotkeyBinding b = HotkeyBinding.defaults(HotkeyAction.ZOOM_IN);
        int mods = NativeInputEvent.CTRL_MASK | NativeInputEvent.ALT_MASK;
        assertTrue(b.matches(mods, NativeKeyEvent.VC_UP));
        assertFalse(b.matches(mods, NativeKeyEvent.VC_DOWN));
        assertFalse(b.matches(NativeInputEvent.CTRL_MASK, NativeKeyEvent.VC_UP));
    }

    @Test
    void displayStringContainsCtrlAlt() {
        String s = HotkeyBinding.defaults(HotkeyAction.ZOOM_IN).toDisplayString();
        assertTrue(s.contains("Ctrl"));
        assertTrue(s.contains("Alt"));
    }
}
