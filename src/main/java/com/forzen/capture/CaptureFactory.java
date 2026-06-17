package com.forzen.capture;

import java.awt.AWTException;

public class CaptureFactory {

    public static ScreenCapture create() {
        DxgiCapture dxgi = new DxgiCapture();
        if (dxgi.isAvailable()) {
            System.out.println("Using DxgiCapture (GDI)");
            return dxgi;
        }
        FastRobotCapture fastRobot = new FastRobotCapture();
        if (fastRobot.isAvailable()) {
            System.out.println("Using FastRobotCapture (DXGI)");
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
