package com.forzen.capture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;

/**
 * Prefers GDI BitBlt (same coordinate space as GetCursorPos under Per-Monitor DPI).
 * <p>
 * Important: AWT {@code Robot} uses a different coordinate space on HiDPI Windows.
 * We may use Robot for a single emergency frame, but we must NEVER permanently
 * switch to it — that caused precision to work at startup then drift forever
 * after a few black GDI frames.
 */
public class ResilientCapture implements ScreenCapture {

    private static final int BLACK_STREAK_BEFORE_HIDE = 2;
    private static final int BLACK_STREAK_BEFORE_ROBOT = 8;

    private final ScreenCapture primary;
    private final ScreenCapture fallback;
    private volatile Supplier<Boolean> hideOverlayHook;
    private volatile Runnable showOverlayHook;
    private int blackStreak;
    private long lastFallbackLogMs;

    public ResilientCapture(ScreenCapture primary, ScreenCapture fallback) {
        this.primary = primary;
        this.fallback = fallback;
    }

    public void setOverlayHooks(Supplier<Boolean> hide, Runnable show) {
        this.hideOverlayHook = hide;
        this.showOverlayHook = show;
    }

    @Override
    public BufferedImage capture(Rectangle region) {
        // Always try GDI first (never sticky-prefer Robot)
        BufferedImage img = primary != null ? primary.capture(region) : null;
        if (img != null && !isMostlyBlack(img)) {
            blackStreak = 0;
            return img;
        }

        blackStreak++;
        if (blackStreak == 1 || blackStreak % 60 == 0) {
            System.err.println("ResilientCapture: GDI black/null frame #" + blackStreak
                    + " region=" + region);
        }

        // After a short streak: briefly hide overlay and retry GDI (same coords)
        if (blackStreak >= BLACK_STREAK_BEFORE_HIDE
                && hideOverlayHook != null && showOverlayHook != null) {
            try {
                Boolean hid = hideOverlayHook.get();
                if (Boolean.TRUE.equals(hid)) {
                    try {
                        Thread.sleep(6);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    img = primary != null ? primary.capture(region) : null;
                }
            } finally {
                try {
                    if (showOverlayHook != null) {
                        showOverlayHook.run();
                    }
                } catch (Throwable ignored) {
                }
            }
            if (img != null && !isMostlyBlack(img)) {
                blackStreak = 0;
                return img;
            }
        }

        // One-shot Robot only after longer streak — never permanent
        if (blackStreak >= BLACK_STREAK_BEFORE_ROBOT && fallback != null) {
            BufferedImage fb = fallback.capture(region);
            long now = System.currentTimeMillis();
            if (now - lastFallbackLogMs > 5_000L) {
                lastFallbackLogMs = now;
                System.err.println("ResilientCapture: one-shot Robot fallback (GDI still preferred next frame)");
            }
            if (fb != null && !isMostlyBlack(fb)) {
                // Do NOT set preferFallback — return to GDI on next call
                // Soften streak so we don't spam Robot every frame
                blackStreak = BLACK_STREAK_BEFORE_HIDE;
                return fb;
            }
            return fb;
        }

        // Last GDI attempt result (may be null/black)
        return img;
    }

    /**
     * True only if nearly all samples are pure black (0,0,0).
     * Dark UIs / wallpapers must NOT trip this.
     */
    private static boolean isMostlyBlack(BufferedImage img) {
        if (img == null) return true;
        int w = img.getWidth();
        int h = img.getHeight();
        if (w < 1 || h < 1) return true;
        int samples = 0;
        int pureBlack = 0;
        int nonBlack = 0;
        int stepX = Math.max(1, w / 12);
        int stepY = Math.max(1, h / 12);
        for (int y = 0; y < h; y += stepY) {
            for (int x = 0; x < w; x += stepX) {
                samples++;
                int rgb = img.getRGB(x, y) & 0x00FFFFFF;
                if (rgb == 0) {
                    pureBlack++;
                } else {
                    nonBlack++;
                    if (nonBlack >= 2) return false;
                }
            }
        }
        if (samples == 0) return true;
        return pureBlack >= samples * 0.98;
    }

    @Override
    public void dispose() {
        if (primary != null) primary.dispose();
        if (fallback != null) fallback.dispose();
    }
}
