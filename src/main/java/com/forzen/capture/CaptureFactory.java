package com.forzen.capture;

import java.awt.AWTException;

public class CaptureFactory {

    public static ScreenCapture create() {
        GdiCapture gdi = new GdiCapture();
        if (gdi.isAvailable()) {
            System.out.println("Using GdiCapture (BitBlt + exclude-from-capture overlay)");
            return gdi;
        }
        FastRobotCapture fastRobot = new FastRobotCapture();
        if (fastRobot.isAvailable()) {
            System.out.println("Using FastRobotCapture");
            return fastRobot;
        }
        try {
            System.out.println("Using RobotCapture (AWT fallback)");
            return new RobotCapture();
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize screen capture", e);
        }
    }
}
