package com.forzen.capture;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Legacy name kept for compatibility. Delegates to {@link GdiCapture}.
 * True DXGI Desktop Duplication would require a native helper; GDI +
 * WDA_EXCLUDEFROMCAPTURE is the supported path on Windows 10/11.
 */
public class DxgiCapture implements ScreenCapture {

    private final GdiCapture delegate = new GdiCapture();

    @Override
    public BufferedImage capture(Rectangle region) {
        return delegate.capture(region);
    }

    @Override
    public void dispose() {
        delegate.dispose();
    }

    public boolean isAvailable() {
        return delegate.isAvailable();
    }
}
