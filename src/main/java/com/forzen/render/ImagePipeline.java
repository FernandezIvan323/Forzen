package com.forzen.render;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImagePipeline {

    private BufferedImage scaledBuffer;

    public BufferedImage scale(BufferedImage source, int targetWidth, int targetHeight) {
        if (source == null) return null;

        if (scaledBuffer == null || scaledBuffer.getWidth() != targetWidth || scaledBuffer.getHeight() != targetHeight) {
            scaledBuffer = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g = scaledBuffer.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g.dispose();

        return scaledBuffer;
    }

    public void dispose() {
        scaledBuffer = null;
    }
}
