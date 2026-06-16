package com.forzen.capture;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class RobotCapture implements ScreenCapture {

    private final Robot robot;

    public RobotCapture() throws AWTException {
        this.robot = new Robot();
    }

    @Override
    public BufferedImage capture(Rectangle region) {
        return robot.createScreenCapture(region);
    }

    @Override
    public void dispose() {
    }
}
