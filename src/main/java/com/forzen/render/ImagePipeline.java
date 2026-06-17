package com.forzen.render;

import com.forzen.filter.FilterMode;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public class ImagePipeline {

    private BufferedImage scaledBuffer;
    private BufferedImage filteredBuffer;

    private float brightness = 1.0f;
    private float contrast = 1.0f;
    private FilterMode currentFilter = FilterMode.NONE;

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

        return applyFilter(scaledBuffer);
    }

    public BufferedImage applyFilter(BufferedImage image) {
        if (image == null || currentFilter == FilterMode.NONE && brightness == 1.0f && contrast == 1.0f) {
            return image;
        }

        int w = image.getWidth();
        int h = image.getHeight();

        if (filteredBuffer == null || filteredBuffer.getWidth() != w || filteredBuffer.getHeight() != h) {
            filteredBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }

        if (brightness != 1.0f || contrast != 1.0f) {
            RescaleOp rescaleOp = new RescaleOp(contrast, (1.0f - contrast) * 127.5f + brightness * 127.5f - 127.5f, null);
            rescaleOp.filter(image, filteredBuffer);
        } else {
            Graphics2D g = filteredBuffer.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
        }

        int[] pixels = new int[w * h];
        filteredBuffer.getRGB(0, 0, w, h, pixels, 0, w);

        switch (currentFilter) {
            case INVERT -> applyInvert(pixels);
            case HIGH_CONTRAST -> applyHighContrast(pixels);
            case GRAYSCALE -> applyGrayscale(pixels);
            case PROTANOPIA -> applyProtanopia(pixels);
            case DEUTERANOPIA -> applyDeuteranopia(pixels);
            case TRITANOPIA -> applyTritanopia(pixels);
        }

        filteredBuffer.setRGB(0, 0, w, h, pixels, 0, w);
        return filteredBuffer;
    }

    public void setFilter(FilterMode mode, double brightnessPct, double contrastPct, double saturationPct) {
        this.currentFilter = mode;
        this.brightness = (float) (brightnessPct / 100.0);
        this.contrast = (float) (contrastPct / 100.0);
    }

    private void applyInvert(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            int a = pixels[i] & 0xFF000000;
            int r = 255 - ((pixels[i] >> 16) & 0xFF);
            int g = 255 - ((pixels[i] >> 8) & 0xFF);
            int b = 255 - (pixels[i] & 0xFF);
            pixels[i] = a | (r << 16) | (g << 8) | b;
        }
    }

    private void applyHighContrast(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8) & 0xFF;
            int b = pixels[i] & 0xFF;
            int avg = (r + g + b) / 3;
            int v = avg > 128 ? 255 : 0;
            pixels[i] = 0xFF000000 | (v << 16) | (v << 8) | v;
        }
    }

    private void applyGrayscale(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8) & 0xFF;
            int b = pixels[i] & 0xFF;
            int gray = (r * 30 + g * 59 + b * 11) / 100;
            pixels[i] = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
        }
    }

    private void applyProtanopia(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8) & 0xFF;
            int b = pixels[i] & 0xFF;
            int nr = (int) (g * 0.7 + b * 0.3);
            int ng = g;
            int nb = b;
            pixels[i] = 0xFF000000 | (clamp(nr) << 16) | (clamp(ng) << 8) | clamp(nb);
        }
    }

    private void applyDeuteranopia(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8) & 0xFF;
            int b = pixels[i] & 0xFF;
            int nr = r;
            int ng = (int) (r * 0.7 + b * 0.3);
            int nb = b;
            pixels[i] = 0xFF000000 | (clamp(nr) << 16) | (clamp(ng) << 8) | clamp(nb);
        }
    }

    private void applyTritanopia(int[] pixels) {
        for (int i = 0; i < pixels.length; i++) {
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8) & 0xFF;
            int b = pixels[i] & 0xFF;
            int nr = r;
            int ng = g;
            int nb = (int) (r * 0.5 + g * 0.5);
            pixels[i] = 0xFF000000 | (clamp(nr) << 16) | (clamp(ng) << 8) | clamp(nb);
        }
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public void dispose() {
        scaledBuffer = null;
        filteredBuffer = null;
    }
}
