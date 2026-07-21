package com.forzen.util;

import com.forzen.win.NativeScreen;
import com.forzen.win.NativeScreen.PhysMonitor;
import com.forzen.win.NativeScreen.PhysPoint;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.awt.Rectangle;
import java.util.List;

/**
 * Physical (Win32/GDI) ↔ logical (JavaFX) geometry.
 * Cursor and monitor pixel bounds come from {@link NativeScreen}; JavaFX only for display.
 */
public final class ScreenGeometry {

    public record MonitorInfo(
            int physX,
            int physY,
            int physW,
            int physH,
            double scaleX,
            double scaleY,
            Rectangle2D fxBounds,
            Rectangle2D fxVisualBounds,
            Screen fxScreen
    ) {
        /** Back-compat constructor used by tests (visual = full bounds). */
        public MonitorInfo(int physX, int physY, int physW, int physH,
                           double scaleX, double scaleY, Rectangle2D fxBounds, Screen fxScreen) {
            this(physX, physY, physW, physH, scaleX, scaleY, fxBounds,
                    fxBounds != null ? fxBounds : new Rectangle2D(0, 0, 1, 1), fxScreen);
        }

        /**
         * Physical pixels per logical (JavaFX) unit.
         * Prefer the explicit scaleX/Y (from GetDpiForMonitor when available).
         */
        public double physPerFxX() {
            if (scaleX > 0.01) return scaleX;
            if (fxBounds.getWidth() <= 0) return 1.0;
            return physW / fxBounds.getWidth();
        }

        public double physPerFxY() {
            if (scaleY > 0.01) return scaleY;
            if (fxBounds.getHeight() <= 0) return 1.0;
            return physH / fxBounds.getHeight();
        }

        /** Scene-local X on full monitor bounds (0 = left edge of full stage). */
        public double toLocalX(double mousePhysX) {
            if (physW <= 0) return 0;
            return (mousePhysX - physX) * (fxBounds.getWidth() / (double) physW);
        }

        public double toLocalY(double mousePhysY) {
            if (physH <= 0) return 0;
            return (mousePhysY - physY) * (fxBounds.getHeight() / (double) physH);
        }

        /**
         * Local coords relative to the work-area stage (visual bounds), so the taskbar
         * strip is outside the overlay and remains clickable.
         */
        public double toVisualLocalX(double mousePhysX) {
            double full = toLocalX(mousePhysX);
            return full - (fxVisualBounds.getMinX() - fxBounds.getMinX());
        }

        public double toVisualLocalY(double mousePhysY) {
            double full = toLocalY(mousePhysY);
            return full - (fxVisualBounds.getMinY() - fxBounds.getMinY());
        }

        public double toFxX(double physicalX) {
            return fxBounds.getMinX() + toLocalX(physicalX);
        }

        public double toFxY(double physicalY) {
            return fxBounds.getMinY() + toLocalY(physicalY);
        }

        public int toPhysicalX(double fxX) {
            if (fxBounds.getWidth() <= 0) return physX;
            return (int) Math.round(physX + (fxX - fxBounds.getMinX()) * physPerFxX());
        }

        public int toPhysicalY(double fxY) {
            if (fxBounds.getHeight() <= 0) return physY;
            return (int) Math.round(physY + (fxY - fxBounds.getMinY()) * physPerFxY());
        }
    }

    private ScreenGeometry() {}

    public static PhysPoint cursorPhysical() {
        return NativeScreen.cursorPos();
    }

    /**
     * Monitor under the physical cursor, with JavaFX bounds for the matching Screen.
     */
    public static MonitorInfo monitorAtPhysical(int physicalX, int physicalY) {
        PhysMonitor phys = NativeScreen.monitorAt(physicalX, physicalY);
        Screen fxScreen = matchFxScreen(phys);
        Rectangle2D fx = fxScreen != null ? fxScreen.getBounds() : fallbackFxBounds(phys);
        Rectangle2D visual = fxScreen != null ? fxScreen.getVisualBounds() : fx;
        if (visual.getWidth() < 2 || visual.getHeight() < 2) {
            visual = fx;
        }

        // Prefer Win32 effective DPI (matches BitBlt / GetCursorPos on HiDPI)
        double dpiScaleX = phys.scaleX();
        double dpiScaleY = phys.scaleY();
        double ratioX = fx.getWidth() > 0 ? phys.w() / fx.getWidth() : 1.0;
        double ratioY = fx.getHeight() > 0 ? phys.h() / fx.getHeight() : 1.0;
        double outScaleX = fxScreen != null && fxScreen.getOutputScaleX() > 0
                ? fxScreen.getOutputScaleX() : 0;
        double outScaleY = fxScreen != null && fxScreen.getOutputScaleY() > 0
                ? fxScreen.getOutputScaleY() : 0;

        // Consensus: DPI native first, then JavaFX output scale, then phys/fx ratio
        double scaleX = dpiScaleX;
        double scaleY = dpiScaleY;
        if (outScaleX > 0.01 && Math.abs(outScaleX - dpiScaleX) < 0.15) {
            scaleX = outScaleX;
        } else if (Math.abs(ratioX - dpiScaleX) < 0.15) {
            scaleX = ratioX;
        }
        if (outScaleY > 0.01 && Math.abs(outScaleY - dpiScaleY) < 0.15) {
            scaleY = outScaleY;
        } else if (Math.abs(ratioY - dpiScaleY) < 0.15) {
            scaleY = ratioY;
        }
        if (scaleX <= 0) scaleX = 1.0;
        if (scaleY <= 0) scaleY = 1.0;

        return new MonitorInfo(phys.x(), phys.y(), phys.w(), phys.h(), scaleX, scaleY, fx, visual, fxScreen);
    }

    private static Rectangle2D fallbackFxBounds(PhysMonitor phys) {
        return new Rectangle2D(phys.x(), phys.y(), phys.w(), phys.h());
    }

    private static Screen matchFxScreen(PhysMonitor phys) {
        List<Screen> screens = Screen.getScreens();
        if (screens.isEmpty()) return null;

        double physCx = phys.x() + phys.w() / 2.0;
        double physCy = phys.y() + phys.h() / 2.0;

        Screen best = null;
        double bestScore = Double.MAX_VALUE;

        for (Screen screen : screens) {
            Rectangle2D fx = screen.getBounds();
            double sx = screen.getOutputScaleX() > 0 ? screen.getOutputScaleX() : 1.0;
            double sy = screen.getOutputScaleY() > 0 ? screen.getOutputScaleY() : 1.0;

            double fxCx = fx.getMinX() + fx.getWidth() / 2.0;
            double fxCy = fx.getMinY() + fx.getHeight() / 2.0;
            double approxPhysCx = fxCx * sx;
            double approxPhysCy = fxCy * sy;

            double expectedW = fx.getWidth() * sx;
            double expectedH = fx.getHeight() * sy;
            double sizeErr = Math.abs(expectedW - phys.w()) + Math.abs(expectedH - phys.h());
            double sizeErrLog = Math.abs(fx.getWidth() - phys.w()) + Math.abs(fx.getHeight() - phys.h());

            double dist = Math.hypot(approxPhysCx - physCx, approxPhysCy - physCy);
            double score = Math.min(sizeErr, sizeErrLog) + dist * 0.01;

            if (sizeErr < 8 || sizeErrLog < 8) {
                score -= 10_000;
            }

            if (score < bestScore) {
                bestScore = score;
                best = screen;
            }
        }
        return best != null ? best : Screen.getPrimary();
    }

    /**
     * Fixed-size pan viewport clamped to the monitor (no black letterbox).
     * At edges the cursor is no longer at the buffer center — use
     * {@link #cursorInViewport(int, int, Rectangle)} when drawing so the
     * crosshair still tracks the true mouse pixel.
     */
    public static Rectangle captureViewport(int mouseX, int mouseY, double srcW, double srcH, MonitorInfo m) {
        if (m == null) return null;

        int w = Math.max(1, (int) Math.round(srcW));
        int h = Math.max(1, (int) Math.round(srcH));
        w = Math.min(w, m.physW());
        h = Math.min(h, m.physH());

        int x = (int) Math.round(mouseX - w / 2.0);
        int y = (int) Math.round(mouseY - h / 2.0);

        int minX = m.physX();
        int minY = m.physY();
        int maxX = m.physX() + m.physW() - w;
        int maxY = m.physY() + m.physH() - h;
        if (maxX < minX) maxX = minX;
        if (maxY < minY) maxY = minY;

        x = Math.max(minX, Math.min(x, maxX));
        y = Math.max(minY, Math.min(y, maxY));
        return new Rectangle(x, y, w, h);
    }

    /**
     * Cursor position inside a capture rectangle (physical pixels).
     * @return {@code int[]{hotX, hotY}} relative to region origin; clamped to the buffer
     */
    public static int[] cursorInViewport(int mouseX, int mouseY, Rectangle region) {
        if (region == null) return new int[]{0, 0};
        int hotX = mouseX - region.x;
        int hotY = mouseY - region.y;
        hotX = Math.max(0, Math.min(region.width - 1, hotX));
        hotY = Math.max(0, Math.min(region.height - 1, hotY));
        return new int[]{hotX, hotY};
    }

    /**
     * Capture FOV size in physical pixels for a logical lens size and zoom.
     * Uses monitor scale (DPI-aware).
     */
    public static double[] captureSizePhys(double lensLogicalW, double lensLogicalH,
                                           double zoom, MonitorInfo m) {
        double z = Math.max(0.01, zoom);
        double sx = m != null ? m.physPerFxX() : 1.0;
        double sy = m != null ? m.physPerFxY() : 1.0;
        return new double[]{
                Math.max(8, (lensLogicalW * sx) / z),
                Math.max(8, (lensLogicalH * sy) / z)
        };
    }

    /** Legacy shrink-clamp (tests / compat). */
    public static Rectangle clampRegion(int srcX, int srcY, int srcW, int srcH, MonitorInfo m) {
        if (srcW <= 0 || srcH <= 0) return null;
        int x1 = Math.max(srcX, m.physX());
        int y1 = Math.max(srcY, m.physY());
        int x2 = Math.min(srcX + srcW, m.physX() + m.physW());
        int y2 = Math.min(srcY + srcH, m.physY() + m.physH());
        int w = x2 - x1;
        int h = y2 - y1;
        if (w <= 0 || h <= 0) return null;
        return new Rectangle(x1, y1, w, h);
    }

    public static Rectangle captureAround(int mouseX, int mouseY, double srcW, double srcH, MonitorInfo m) {
        int w = Math.max(1, (int) Math.ceil(srcW));
        int h = Math.max(1, (int) Math.ceil(srcH));
        int x = (int) Math.round(mouseX - w / 2.0);
        int y = (int) Math.round(mouseY - h / 2.0);
        return clampRegion(x, y, w, h, m);
    }
}
