package com.forzen.capture;

import java.awt.AWTException;

public class CaptureFactory {

    public static ScreenCapture create() {
        ScreenCapture primary = null;
        GdiCapture gdi = new GdiCapture();
        if (gdi.isAvailable()) {
            System.out.println("Primary capture: GdiCapture (BitBlt)");
            primary = gdi;
        }

        ScreenCapture fallback = null;
        FastRobotCapture fastRobot = new FastRobotCapture();
        if (fastRobot.isAvailable()) {
            System.out.println("Fallback capture: FastRobotCapture");
            fallback = fastRobot;
        } else {
            try {
                System.out.println("Fallback capture: RobotCapture (AWT)");
                fallback = new RobotCapture();
            } catch (AWTException e) {
                System.err.println("RobotCapture unavailable: " + e.getMessage());
            }
        }

        if (primary == null && fallback == null) {
            throw new RuntimeException("Failed to initialize screen capture");
        }
        if (primary == null) {
            return fallback;
        }
        if (fallback == null) {
            return primary;
        }
        System.out.println("Using ResilientCapture (GDI + Robot fallback on black frames)");
        return new ResilientCapture(primary, fallback);
    }
}
