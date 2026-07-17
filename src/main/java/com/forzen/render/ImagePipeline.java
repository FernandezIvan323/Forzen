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
    private float saturation = 1.0f;
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
        if (image == null
                || (currentFilter == FilterMode.NONE
                && brightness == 1.0f
                && contrast == 1.0f
                && saturation == 1.0f)) {
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

        if (saturation != 1.0f) {
            applySaturation(pixels, saturation);
        }

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
        this.saturation = (float) (saturationPct / 100.0);
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

    private void applySaturation(int[] pixels, float factor) {
        for (int i = 0; i < pixels.length; i++) {
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8) & 0xFF;
            int b = pixels[i] & 0xFF;

            float rf = r / 255.0f;
            float gf = g / 255.0f;
            float bf = b / 255.0f;

            float max = Math.max(rf, Math.max(gf, bf));
            float min = Math.min(rf, Math.min(gf, bf));
            float l = (max + min) / 2.0f;

            if (max == min) {
                continue;
            }

            float d = max - min;
            float s = l > 0.5f ? d / (2.0f - max - min) : d / (max + min);

            s = Math.max(0.0f, Math.min(1.0f, s * factor));

            float h = 0;
            if (max == rf) {
                h = (gf - bf) / d + (gf < bf ? 6 : 0);
            } else if (max == gf) {
                h = (bf - rf) / d + 2;
            } else {
                h = (rf - gf) / d + 4;
            }
            h /= 6.0f;

            float nr, ng, nb;
            if (s == 0) {
                nr = ng = nb = l;
            } else {
                float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
                float p = 2 * l - q;
                nr = hueToRgb(p, q, h + 1.0f / 3.0f);
                ng = hueToRgb(p, q, h);
                nb = hueToRgb(p, q, h - 1.0f / 3.0f);
            }

            int ir = clamp((int) Math.round(nr * 255));
            int ig = clamp((int) Math.round(ng * 255));
            int ib = clamp((int) Math.round(nb * 255));
            pixels[i] = 0xFF000000 | (ir << 16) | (ig << 8) | ib;
        }
    }

    private float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1.0f / 6.0f) return p + (q - p) * 6 * t;
        if (t < 1.0f / 2.0f) return q;
        if (t < 2.0f / 3.0f) return p + (q - p) * (2.0f / 3.0f - t) * 6;
        return p;
    }

    private int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    public void dispose() {
        scaledBuffer = null;
        filteredBuffer = null;
    }
}
