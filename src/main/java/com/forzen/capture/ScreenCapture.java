package com.forzen.capture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public interface ScreenCapture {
    BufferedImage capture(Rectangle region);
    void dispose();
}
