package com.forzen.ocr;

import java.awt.image.BufferedImage;

/**
 * Optional Tess4J wrapper. Works only when Tess4J + Tesseract data are present.
 */
public class OcrEngine {

    private boolean available;
    private Object tesseract;
    private String lastError = "";

    public OcrEngine() {
        try {
            Class<?> tesseractClass = Class.forName("net.sourceforge.tess4j.Tesseract");
            // Tess4J 5.x: public constructor (getInstance is legacy)
            try {
                tesseract = tesseractClass.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                tesseract = tesseractClass.getMethod("getInstance").invoke(null);
            }
            tesseractClass.getMethod("setLanguage", String.class).invoke(tesseract, "spa+eng");
            // Prefer TESSDATA_PREFIX env if set
            String prefix = System.getenv("TESSDATA_PREFIX");
            if (prefix != null && !prefix.isBlank()) {
                try {
                    tesseractClass.getMethod("setDatapath", String.class).invoke(tesseract, prefix);
                } catch (Exception ignored) {
                }
            }
            available = true;
            System.out.println("OCR: Tess4J available");
        } catch (Throwable e) {
            lastError = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            System.out.println("OCR not available: " + lastError);
            available = false;
        }
    }

    public String recognize(BufferedImage image) {
        if (!available || image == null || tesseract == null) return "";
        try {
            Object result = tesseract.getClass()
                    .getMethod("doOCR", BufferedImage.class)
                    .invoke(tesseract, image);
            return result != null ? result.toString().trim() : "";
        } catch (Exception e) {
            lastError = e.getMessage() != null ? e.getMessage() : "OCR error";
            System.err.println("OCR error: " + lastError);
            return "";
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public String getLastError() {
        return lastError;
    }
}
