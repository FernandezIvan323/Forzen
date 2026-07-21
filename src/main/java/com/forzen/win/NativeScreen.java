package com.forzen.win;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * Win32 physical screen coordinates — single source of truth for capture + cursor.
 * GetCursorPos / GetMonitorInfo / GetDpiForMonitor use virtual-screen pixel space
 * that GDI BitBlt expects.
 */
public final class NativeScreen {

    public record PhysPoint(int x, int y) {}

    /**
     * Full monitor rectangle in virtual-screen pixels (includes taskbar strip).
     * {@code dpiX}/{@code dpiY} are effective DPI (96 = 100% scale).
     */
    public record PhysMonitor(int x, int y, int w, int h,
                              int workX, int workY, int workW, int workH,
                              int dpiX, int dpiY) {
        public boolean contains(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }

        public double scaleX() {
            return dpiX > 0 ? dpiX / 96.0 : 1.0;
        }

        public double scaleY() {
            return dpiY > 0 ? dpiY / 96.0 : 1.0;
        }
    }

    private static final int MDT_EFFECTIVE_DPI = 0;

    public interface ShcoreDpi extends StdCallLibrary {
        ShcoreDpi INSTANCE = Native.load("shcore", ShcoreDpi.class, W32APIOptions.DEFAULT_OPTIONS);

        WinNT.HRESULT GetDpiForMonitor(WinUser.HMONITOR hmonitor, int dpiType,
                                       IntByReference dpiX, IntByReference dpiY);
    }

    private NativeScreen() {}

    public static PhysPoint cursorPos() {
        WinDef.POINT pt = new WinDef.POINT();
        if (!User32.INSTANCE.GetCursorPos(pt)) {
            return new PhysPoint(0, 0);
        }
        return new PhysPoint(pt.x, pt.y);
    }

    public static PhysMonitor monitorAt(int physX, int physY) {
        WinDef.POINT.ByValue pt = new WinDef.POINT.ByValue();
        pt.x = physX;
        pt.y = physY;
        WinUser.HMONITOR hMon = User32.INSTANCE.MonitorFromPoint(pt, WinUser.MONITOR_DEFAULTTONEAREST);
        return readMonitor(hMon);
    }

    public static PhysMonitor primaryMonitor() {
        WinDef.POINT.ByValue pt = new WinDef.POINT.ByValue();
        pt.x = 0;
        pt.y = 0;
        WinUser.HMONITOR hMon = User32.INSTANCE.MonitorFromPoint(pt, WinUser.MONITOR_DEFAULTTOPRIMARY);
        return readMonitor(hMon);
    }

    private static PhysMonitor readMonitor(WinUser.HMONITOR hMon) {
        WinUser.MONITORINFO info = new WinUser.MONITORINFO();
        info.cbSize = info.size();
        boolean ok = hMon != null && User32.INSTANCE.GetMonitorInfo(hMon, info).booleanValue();
        if (!ok) {
            return new PhysMonitor(0, 0, 1920, 1080, 0, 0, 1920, 1040, 96, 96);
        }
        WinDef.RECT rc = info.rcMonitor;
        WinDef.RECT work = info.rcWork;
        int x = rc.left;
        int y = rc.top;
        int w = Math.max(1, rc.right - rc.left);
        int h = Math.max(1, rc.bottom - rc.top);
        int wx = work.left;
        int wy = work.top;
        int ww = Math.max(1, work.right - work.left);
        int wh = Math.max(1, work.bottom - work.top);

        int dpiX = 96;
        int dpiY = 96;
        try {
            IntByReference dx = new IntByReference();
            IntByReference dy = new IntByReference();
            WinNT.HRESULT hr = ShcoreDpi.INSTANCE.GetDpiForMonitor(hMon, MDT_EFFECTIVE_DPI, dx, dy);
            if (hr != null && hr.intValue() == 0) {
                dpiX = Math.max(96, dx.getValue());
                dpiY = Math.max(96, dy.getValue());
            }
        } catch (Throwable ignored) {
            // Keep 96
        }
        return new PhysMonitor(x, y, w, h, wx, wy, ww, wh, dpiX, dpiY);
    }
}
