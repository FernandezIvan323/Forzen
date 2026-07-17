package com.forzen.win;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;

import javafx.stage.Stage;

/**
 * Win32 helpers for click-through, capture exclusion and HWND lookup.
 */
public final class WindowNative {

    public static final int GWL_EXSTYLE = -20;
    public static final int WS_EX_TRANSPARENT = 0x00000020;
    public static final int WS_EX_LAYERED = 0x00080000;
    public static final int WS_EX_TOOLWINDOW = 0x00000080;
    public static final int WS_EX_NOACTIVATE = 0x08000000;

    /** Windows 10 2004+ — window not included in BitBlt / screenshots. */
    public static final int WDA_EXCLUDEFROMCAPTURE = 0x11;

    public interface User32Ex extends User32 {
        User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SetWindowDisplayAffinity(WinDef.HWND hWnd, int dwAffinity);
    }

    private WindowNative() {}

    /**
     * Resolve HWND for a JavaFX Stage by temporarily setting a unique title.
     */
    public static WinDef.HWND findHwnd(Stage stage) {
        if (stage == null) return null;
        String original = stage.getTitle() == null ? "" : stage.getTitle();
        String marker = "ForzenHWND-" + System.nanoTime();
        try {
            stage.setTitle(marker);
            for (int i = 0; i < 30; i++) {
                WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, marker);
                if (hwnd != null) {
                    return hwnd;
                }
                try {
                    Thread.sleep(15);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            return User32.INSTANCE.FindWindow(null, marker);
        } finally {
            stage.setTitle(original.isBlank() ? "Forzen" : original);
        }
    }

    public static void excludeFromCapture(WinDef.HWND hwnd) {
        if (hwnd == null) return;
        try {
            boolean ok = User32Ex.INSTANCE.SetWindowDisplayAffinity(hwnd, WDA_EXCLUDEFROMCAPTURE);
            if (!ok) {
                System.err.println("SetWindowDisplayAffinity returned false (need Win10 2004+)");
            }
        } catch (Throwable t) {
            System.err.println("SetWindowDisplayAffinity failed: " + t.getMessage());
        }
    }

    /**
     * When clickThrough is true, mouse events pass to windows below (LENS/DOCKED).
     * When false, the window receives input (FULL mode).
     */
    public static void setClickThrough(WinDef.HWND hwnd, boolean clickThrough) {
        if (hwnd == null) return;
        try {
            int style = User32.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);
            style |= WS_EX_LAYERED | WS_EX_TOOLWINDOW | WS_EX_NOACTIVATE;
            if (clickThrough) {
                style |= WS_EX_TRANSPARENT;
            } else {
                style &= ~WS_EX_TRANSPARENT;
            }
            User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, style);

            WinDef.HWND topmost = new WinDef.HWND(Pointer.createConstant(-1));
            User32.INSTANCE.SetWindowPos(
                    hwnd,
                    topmost,
                    0, 0, 0, 0,
                    WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE | WinUser.SWP_FRAMECHANGED
            );
        } catch (Throwable t) {
            System.err.println("setClickThrough failed: " + t.getMessage());
        }
    }
}
