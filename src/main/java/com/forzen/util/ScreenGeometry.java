package com.forzen.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.List;

/**
 * Coordinate helpers: physical (AWT/GDI) ↔ logical (JavaFX), clamp, multi-monitor.
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
            Screen fxScreen
    ) {
        public double toFxX(double physicalX) {
            return fxBounds.getMinX() + (physicalX - physX) / scaleX;
        }

        public double toFxY(double physicalY) {
            return fxBounds.getMinY() + (physicalY - physY) / scaleY;
        }

        public int toPhysicalX(double fxX) {
            return (int) Math.round(physX + (fxX - fxBounds.getMinX()) * scaleX);
        }

        public int toPhysicalY(double fxY) {
            return (int) Math.round(physY + (fxY - fxBounds.getMinY()) * scaleY);
        }
    }

    private ScreenGeometry() {}

    /**
     * Find the monitor that contains the physical mouse point.
     */
    public static MonitorInfo monitorAtPhysical(int physicalX, int physicalY) {
        List<Screen> screens = Screen.getScreens();
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();

        // Match by overlapping bounds: prefer screen whose physical bounds contain the point
        MonitorInfo best = null;
        double bestArea = Double.MAX_VALUE;

        for (int i = 0; i < screens.size(); i++) {
            Screen screen = screens.get(i);
            Rectangle2D fx = screen.getBounds();
            double scaleX = screen.getOutputScaleX();
            double scaleY = screen.getOutputScaleY();

            int physX;
            int physY;
            int physW;
            int physH;

            if (i < devices.length) {
                GraphicsConfiguration gc = devices[i].getDefaultConfiguration();
                Rectangle r = gc.getBounds();
                physX = r.x;
                physY = r.y;
                physW = r.width;
                physH = r.height;
                // Prefer device scale if available
                if (scaleX <= 0) scaleX = 1.0;
                if (scaleY <= 0) scaleY = 1.0;
            } else {
                physX = (int) Math.round(fx.getMinX() * scaleX);
                physY = (int) Math.round(fx.getMinY() * scaleY);
                physW = (int) Math.round(fx.getWidth() * scaleX);
                physH = (int) Math.round(fx.getHeight() * scaleY);
            }

            boolean contains = physicalX >= physX && physicalX < physX + physW
                    && physicalY >= physY && physicalY < physY + physH;

            MonitorInfo info = new MonitorInfo(physX, physY, physW, physH, scaleX, scaleY, fx, screen);
            if (contains) {
                // Smallest containing monitor wins (handles overlapping virtual configs)
                double area = (double) physW * physH;
                if (area < bestArea) {
                    bestArea = area;
                    best = info;
                }
            }
            if (best == null && i == 0) {
                best = info; // fallback primary-ish
            }
        }

        if (best == null) {
            Screen primary = Screen.getPrimary();
            Rectangle2D fx = primary.getBounds();
            double sx = primary.getOutputScaleX();
            double sy = primary.getOutputScaleY();
            return new MonitorInfo(
                    (int) Math.round(fx.getMinX() * sx),
                    (int) Math.round(fx.getMinY() * sy),
                    (int) Math.round(fx.getWidth() * sx),
                    (int) Math.round(fx.getHeight() * sy),
                    sx, sy, fx, primary
            );
        }
        return best;
    }

    /**
     * Clamp a capture rectangle to monitor physical bounds. Returns null if invalid.
     */
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

    /**
     * Centered capture region around physical mouse, clamped to monitor.
     */
    public static Rectangle captureAround(int mouseX, int mouseY, double srcW, double srcH, MonitorInfo m) {
        int w = Math.max(1, (int) Math.ceil(srcW));
        int h = Math.max(1, (int) Math.ceil(srcH));
        int x = (int) Math.round(mouseX - w / 2.0);
        int y = (int) Math.round(mouseY - h / 2.0);
        return clampRegion(x, y, w, h, m);
    }
}
