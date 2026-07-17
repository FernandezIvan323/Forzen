package com.forzen.capture;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinGDI;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Fast screen capture via GDI BitBlt. Combined with WDA_EXCLUDEFROMCAPTURE on the
 * overlay, this avoids self-feedback without true DXGI Desktop Duplication.
 */
public class GdiCapture implements ScreenCapture {

    private static final int SRCCOPY = 0x00CC0020;

    private WinDef.HDC hdcScreen;
    private WinDef.HDC hdcMem;
    private boolean available;

    public GdiCapture() {
        try {
            hdcScreen = User32.INSTANCE.GetDC(null);
            hdcMem = GDI32.INSTANCE.CreateCompatibleDC(hdcScreen);
            available = hdcScreen != null && hdcMem != null;
            if (available) {
                System.out.println("GdiCapture: GDI BitBlt available");
            }
        } catch (Exception e) {
            System.out.println("GdiCapture not available: " + e.getMessage());
            available = false;
        }
    }

    @Override
    public BufferedImage capture(Rectangle region) {
        if (!available || region == null) return null;

        int w = region.width;
        int h = region.height;
        if (w <= 0 || h <= 0) return null;

        WinDef.HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcScreen, w, h);
        if (hBitmap == null) return null;

        try {
            GDI32.INSTANCE.SelectObject(hdcMem, hBitmap);
            boolean success = GDI32.INSTANCE.BitBlt(hdcMem, 0, 0, w, h, hdcScreen, region.x, region.y, SRCCOPY);
            if (!success) {
                return null;
            }

            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

            WinGDI.BITMAPINFO bmi = new WinGDI.BITMAPINFO();
            bmi.bmiHeader.biWidth = w;
            bmi.bmiHeader.biHeight = -h; // top-down
            bmi.bmiHeader.biPlanes = 1;
            bmi.bmiHeader.biBitCount = 32;
            bmi.bmiHeader.biCompression = WinGDI.BI_RGB;
            bmi.bmiHeader.biSizeImage = w * h * 4;
            bmi.write();

            try (Memory buffer = new Memory((long) w * h * 4)) {
                int lines = GDI32.INSTANCE.GetDIBits(hdcMem, hBitmap, 0, h, buffer, bmi, WinGDI.DIB_RGB_COLORS);
                if (lines != 0) {
                    int[] buf = buffer.getIntArray(0, w * h);
                    // Strip alpha channel noise: force opaque RGB
                    for (int i = 0; i < buf.length; i++) {
                        pixels[i] = buf[i] & 0x00FFFFFF;
                    }
                }
            }

            return image;
        } finally {
            GDI32.INSTANCE.DeleteObject(hBitmap);
        }
    }

    @Override
    public void dispose() {
        if (hdcMem != null) {
            GDI32.INSTANCE.DeleteDC(hdcMem);
            hdcMem = null;
        }
        if (hdcScreen != null) {
            User32.INSTANCE.ReleaseDC(null, hdcScreen);
            hdcScreen = null;
        }
        available = false;
    }

    public boolean isAvailable() {
        return available;
    }
}
