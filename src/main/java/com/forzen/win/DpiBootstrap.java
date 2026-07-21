package com.forzen.win;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Enable Per-Monitor DPI awareness v2 before JavaFX starts.
 * Without this, GetCursorPos / BitBlt / JavaFX can disagree under browsers and HiDPI.
 */
public final class DpiBootstrap {

    private static final int DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2 = -4;

    public interface User32Dpi extends StdCallLibrary {
        User32Dpi INSTANCE = Native.load("user32", User32Dpi.class, W32APIOptions.DEFAULT_OPTIONS);

        /** Win10 1703+ */
        boolean SetProcessDpiAwarenessContext(WinNT.HANDLE value);
    }

    public interface Shcore extends StdCallLibrary {
        Shcore INSTANCE = Native.load("shcore", Shcore.class, W32APIOptions.DEFAULT_OPTIONS);

        int PROCESS_PER_MONITOR_DPI_AWARE = 2;

        /** Win8.1+ fallback */
        int SetProcessDpiAwareness(int value);
    }

    private DpiBootstrap() {}

    public static void enable() {
        try {
            // Prefer PMv2
            WinNT.HANDLE ctx = new WinNT.HANDLE();
            ctx.setPointer(com.sun.jna.Pointer.createConstant(DPI_AWARENESS_CONTEXT_PER_MONITOR_AWARE_V2));
            boolean ok = User32Dpi.INSTANCE.SetProcessDpiAwarenessContext(ctx);
            if (ok) {
                System.out.println("DPI: Per-Monitor Aware V2 enabled");
                return;
            }
        } catch (Throwable t) {
            System.out.println("DPI: SetProcessDpiAwarenessContext unavailable: " + t.getMessage());
        }
        try {
            int hr = Shcore.INSTANCE.SetProcessDpiAwareness(Shcore.PROCESS_PER_MONITOR_DPI_AWARE);
            System.out.println("DPI: SetProcessDpiAwareness(PER_MONITOR) hr=" + hr);
        } catch (Throwable t) {
            System.out.println("DPI: could not set awareness: " + t.getMessage());
        }
    }
}
