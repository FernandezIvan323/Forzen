package com.forzen.capture;

import java.awt.AWTException;

public class CaptureFactory {

    public static ScreenCapture create() {
        FastRobotCapture fastRobot = new FastRobotCapture();
        if (fastRobot.isAvailable()) {
            return fastRobot;
        }
        try {
            return new RobotCapture();
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize screen capture", e);
        }
    }
}
