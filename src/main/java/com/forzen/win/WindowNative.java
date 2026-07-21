package com.forzen.win;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.W32APIOptions;

import javafx.stage.Stage;

/**
 * Win32 helpers for click-through, capture exclusion, HWND lookup and shell UI.
 */
public final class WindowNative {

    public static final int GWL_EXSTYLE = -20;
    public static final int WS_EX_TRANSPARENT = 0x00000020;
    public static final int WS_EX_LAYERED = 0x00080000;
    public static final int WS_EX_TOOLWINDOW = 0x00000080;
    public static final int WS_EX_NOACTIVATE = 0x08000000;

    /** Windows 10 2004+ — window not included in BitBlt / screenshots. */
    public static final int WDA_EXCLUDEFROMCAPTURE = 0x11;
    public static final int WDA_NONE = 0x0;
    public static final int WS_EX_APPWINDOW = 0x00040000;

    public interface User32Ex extends User32 {
        User32Ex INSTANCE = Native.load("user32", User32Ex.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SetWindowDisplayAffinity(WinDef.HWND hWnd, int dwAffinity);

        boolean EnumChildWindows(WinDef.HWND hWndParent, WinUser.WNDENUMPROC lpEnumFunc, Pointer lParam);
    }

    private WindowNative() {}

    /**
     * Resolve HWND for a JavaFX Stage by temporarily setting a unique title.
     * Non-blocking-ish: short retries only (must not freeze the FX thread for long).
     */
    public static WinDef.HWND findHwnd(Stage stage) {
        if (stage == null) return null;
        String original = stage.getTitle() == null ? "" : stage.getTitle();
        String marker = "ForzenHWND-" + System.nanoTime();
        try {
            stage.setTitle(marker);
            // Few quick attempts; avoid long sleeps on the FX thread
            for (int i = 0; i < 8; i++) {
                WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, marker);
                if (hwnd != null) {
                    return hwnd;
                }
                try {
                    Thread.sleep(5);
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
     * Settings window: appear on the taskbar like a normal app (not a tool window).
     */
    public static void ensureTaskbarAppWindow(WinDef.HWND hwnd) {
        if (hwnd == null) return;
        try {
            int style = User32.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);
            // Clear toolwindow / noactivate so it shows in taskbar and Alt+Tab
            style &= ~WS_EX_TOOLWINDOW;
            style &= ~WS_EX_NOACTIVATE;
            style |= WS_EX_APPWINDOW;
            User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, style);
            User32.INSTANCE.SetWindowPos(
                    hwnd,
                    null,
                    0, 0, 0, 0,
                    WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOZORDER
                            | WinUser.SWP_NOACTIVATE | WinUser.SWP_FRAMECHANGED
            );
        } catch (Throwable t) {
            System.err.println("ensureTaskbarAppWindow failed: " + t.getMessage());
        }
    }

    /**
     * When clickThrough is true, mouse events pass to windows below.
     * Does NOT force topmost — use {@link #setTopmost} separately so we don't
     * fight the Start menu / taskbar every frame.
     */
    public static void setClickThrough(WinDef.HWND hwnd, boolean clickThrough) {
        if (hwnd == null) return;
        try {
            applyClickThroughStyle(hwnd, clickThrough);
            try {
                User32Ex.INSTANCE.EnumChildWindows(hwnd, (child, data) -> {
                    applyClickThroughStyle(child, clickThrough);
                    return true;
                }, null);
            } catch (Throwable ignored) {
            }
        } catch (Throwable t) {
            System.err.println("setClickThrough failed: " + t.getMessage());
        }
    }

    private static void applyClickThroughStyle(WinDef.HWND hwnd, boolean clickThrough) {
        if (hwnd == null) return;
        int style = User32.INSTANCE.GetWindowLong(hwnd, GWL_EXSTYLE);
        style |= WS_EX_LAYERED | WS_EX_TOOLWINDOW | WS_EX_NOACTIVATE;
        if (clickThrough) {
            style |= WS_EX_TRANSPARENT;
        } else {
            style &= ~WS_EX_TRANSPARENT;
        }
        User32.INSTANCE.SetWindowLong(hwnd, GWL_EXSTYLE, style);
        // Apply style change without re-stacking above the shell
        User32.INSTANCE.SetWindowPos(
                hwnd,
                null,
                0, 0, 0, 0,
                WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOZORDER
                        | WinUser.SWP_NOACTIVATE | WinUser.SWP_FRAMECHANGED
        );
    }

    /**
     * Raise or lower the overlay in the topmost band.
     * Call sparingly — reasserting every frame steals the Start menu.
     */
    public static void setTopmost(WinDef.HWND hwnd, boolean topmost) {
        if (hwnd == null) return;
        try {
            WinDef.HWND insertAfter = new WinDef.HWND(
                    Pointer.createConstant(topmost ? -1 : -2)); // HWND_TOPMOST / HWND_NOTOPMOST
            User32.INSTANCE.SetWindowPos(
                    hwnd,
                    insertAfter,
                    0, 0, 0, 0,
                    WinUser.SWP_NOMOVE | WinUser.SWP_NOSIZE | WinUser.SWP_NOACTIVATE
            );
        } catch (Throwable t) {
            System.err.println("setTopmost failed: " + t.getMessage());
        }
    }

    /**
     * True when Start / Search / tray overflow / shell flyout should take over.
     * Used to hide the magnifier so system UI is not covered or shown twice.
     */
    public static boolean isShellUiForeground() {
        try {
            if (isTrayOverflowVisible()) {
                return true;
            }
            WinDef.HWND fg = User32.INSTANCE.GetForegroundWindow();
            if (fg == null) return false;
            return isShellClassOrTitle(fg);
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * System tray overflow (hidden icons / background apps). Visible even when
     * not strictly the foreground window — conflicts with dock at bottom-right.
     */
    public static boolean isTrayOverflowVisible() {
        try {
            String[] classes = {
                    "NotifyIconOverflowWindow",
                    "TopLevelWindowForOverflowXamlIsland"
            };
            for (String cls : classes) {
                WinDef.HWND h = User32.INSTANCE.FindWindow(cls, null);
                if (h != null && User32.INSTANCE.IsWindowVisible(h)) {
                    return true;
                }
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isShellClassOrTitle(WinDef.HWND hwnd) {
        if (hwnd == null) return false;
        char[] cls = new char[256];
        int n = User32.INSTANCE.GetClassName(hwnd, cls, cls.length);
        if (n <= 0) return false;
        String className = Native.toString(cls);
        if (className == null || className.isBlank()) return false;

        // Narrow list — blanket CoreWindow hid the lens too often on Win11
        if (className.contains("ImmersiveLauncher")) return true;
        if (className.equals("SearchPane")) return true;
        if (className.equals("NotifyIconOverflowWindow")) return true;
        if (className.equals("TopLevelWindowForOverflowXamlIsland")) return true;

        char[] title = new char[256];
        int tn = User32.INSTANCE.GetWindowText(hwnd, title, title.length);
        if (tn > 0) {
            String t = Native.toString(title);
            if ("Search".equalsIgnoreCase(t) || "Start".equalsIgnoreCase(t)) {
                return true;
            }
        }
        return false;
    }
}
