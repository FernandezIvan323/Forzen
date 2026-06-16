package com.forzen.capture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class FastRobotCapture implements ScreenCapture {

    private boolean available;

    public FastRobotCapture() {
        this.available = false;
        try {
            Class<?> fastRobot = Class.forName("fastrobot.FastRobot");
            fastRobot.getMethod("init").invoke(null);
            this.available = true;
            System.out.println("FastRobot: DXGI acceleration available");
        } catch (Exception e) {
            System.out.println("FastRobot not available, use Robot fallback: " + e.getMessage());
        }
    }

    @Override
    public BufferedImage capture(Rectangle region) {
        if (!available) return null;
        try {
            Class<?> fastRobot = Class.forName("fastrobot.FastRobot");
            return (BufferedImage) fastRobot.getMethod("capture", int.class, int.class, int.class, int.class)
                .invoke(null, region.x, region.y, region.width, region.height);
        } catch (Exception e) {
            available = false;
            return null;
        }
    }

    @Override
    public void dispose() {
    }

    public boolean isAvailable() {
        return available;
    }
}
