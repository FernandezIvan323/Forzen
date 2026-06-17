package com.forzen.ocr;

import java.awt.image.BufferedImage;

public class OcrEngine {

    private boolean available;

    public OcrEngine() {
        try {
            Class.forName("net.sourceforge.tess4j.Tesseract");
            available = true;
            System.out.println("OCR: Tess4J available");
        } catch (ClassNotFoundException e) {
            System.out.println("OCR not available: Tess4J not in classpath");
            available = false;
        }
    }

    public String recognize(BufferedImage image) {
        if (!available || image == null) return "";
        try {
            Class<?> tesseractClass = Class.forName("net.sourceforge.tess4j.Tesseract");
            Object tesseract = tesseractClass.getMethod("getInstance").invoke(null);
            tesseractClass.getMethod("setLanguage", String.class).invoke(tesseract, "spa+eng");
            Object result = tesseractClass.getMethod("doOCR", BufferedImage.class).invoke(tesseract, image);
            return result != null ? result.toString().trim() : "";
        } catch (Exception e) {
            System.err.println("OCR error: " + e.getMessage());
            return "";
        }
    }

    public boolean isAvailable() {
        return available;
    }
}
