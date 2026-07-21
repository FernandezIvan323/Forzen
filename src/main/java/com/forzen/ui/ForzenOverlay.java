package com.forzen.ui;

import com.forzen.capture.ScreenCapture;
import com.forzen.core.ZoomController;
import com.forzen.core.ZoomMode;
import com.forzen.render.ImagePipeline;
import com.forzen.render.LensRenderer;
import com.forzen.util.ScreenGeometry;
import com.forzen.util.ScreenGeometry.MonitorInfo;
import com.forzen.win.NativeScreen;
import com.forzen.win.WindowNative;
import com.sun.jna.platform.win32.WinDef;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Full-monitor transparent overlay. LENS/DOCKED move content with layout coords
 * inside this stage (not by moving the Windows window every frame). Click-through
 * + exclude-from-capture keep the desktop usable; Win key yields for Start menu.
 */
public class ForzenOverlay extends Stage {

    private final ZoomController zoomController;
    private final LensRenderer lensRenderer;
    private final Pane root;
    private WinDef.HWND hwnd;
    private boolean clickThrough = true;
    private long lastClickThroughAssertNanos = 0;
    private long lastTopmostAssertNanos = 0;
    private MonitorInfo lastMonitor;
    private Rectangle2D lastAppliedBounds;
    /** When true, settings is open — keep overlay hidden / click-through. */
    private boolean settingsOpen;
    /** When shell UI (Start) is open, we hide so the menu is not duplicated/blocked. */
    private boolean shellUiYielded;
    /** Deadline (nanoTime) until which we keep yielding after Win key. */
    private long shellYieldUntilNanos;

    public ForzenOverlay(ZoomController zoomController, ScreenCapture screenCapture, ImagePipeline imagePipeline) {
        this.zoomController = zoomController;

        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);
        setTitle("Forzen");
        initOwner(null);
        setResizable(false);

        MonitorInfo boot = ScreenGeometry.monitorAtPhysical(
                NativeScreen.cursorPos().x(), NativeScreen.cursorPos().y());
        lastMonitor = boot;
        Rectangle2D stageBounds = fullBounds(boot);

        System.out.printf(
                "Overlay boot: stage=%.0fx%.0f @ %.0f,%.0f  phys=%dx%d%n",
                stageBounds.getWidth(), stageBounds.getHeight(),
                stageBounds.getMinX(), stageBounds.getMinY(),
                boot.physW(), boot.physH()
        );

        root = new Pane();
        root.setStyle("-fx-background-color: transparent;");
        root.setMouseTransparent(true);
        sizeRoot(stageBounds.getWidth(), stageBounds.getHeight());

        Scene scene = new Scene(root, stageBounds.getWidth(), stageBounds.getHeight());
        scene.setFill(Color.TRANSPARENT);
        setScene(scene);

        applyStageBounds(stageBounds);

        setOnShown(e -> Platform.runLater(() -> {
            initNativeWindow();
            forceClickThroughPolicy();
            raiseTopmost(true);
        }));

        // Wire capture fallback: briefly hide glass if GDI returns black.
        // Always restore opacity in show hook (never leave glass half-invisible).
        if (screenCapture instanceof com.forzen.capture.ResilientCapture resilient) {
            resilient.setOverlayHooks(
                    () -> {
                        try {
                            if (isShowing()) {
                                setOpacity(0.01);
                                return true;
                            }
                        } catch (Throwable t) {
                            try {
                                setOpacity(1.0);
                            } catch (Throwable ignored) {
                            }
                        }
                        return false;
                    },
                    () -> {
                        try {
                            setOpacity(1.0);
                        } catch (Throwable ignored) {
                        }
                    }
            );
        }

        lensRenderer = new LensRenderer(screenCapture, imagePipeline, zoomController, root);
        lensRenderer.setClickThroughHandler(this::applyClickThrough);
        lensRenderer.setMonitorChangeHandler(this::onMonitorChanged);
        lensRenderer.setOverlayVisibilityHandler(this::onVisibility);
        lensRenderer.setStageSizeSupplier(() -> new double[]{
                Math.max(getWidth(), root.getWidth()),
                Math.max(getHeight(), root.getHeight())
        });
        lensRenderer.setClickThroughReassertHandler(this::reassertClickThroughIfNeeded);
        lensRenderer.setStageBoundsHandler(this::onContentBounds);
        lensRenderer.start();

        zoomController.runningProperty().addListener((obs, o, running) -> {
            if (running) {
                if (!settingsOpen && !isShowing()) show();
                Platform.runLater(() -> {
                    initNativeWindow();
                    forceClickThroughPolicy();
                    raiseTopmost(true);
                });
            } else {
                hide();
            }
        });

        zoomController.modeProperty().addListener((obs, o, mode) ->
                Platform.runLater(() -> {
                    lastAppliedBounds = null;
                    forceClickThroughPolicy();
                    // LENS uses full monitor; DOCKED uses a small stage (set every frame)
                    if (mode != ZoomMode.DOCKED && lastMonitor != null) {
                        applyStageBounds(fullBounds(lastMonitor));
                    }
                    Platform.runLater(() -> {
                        initNativeWindow();
                        if (!settingsOpen) {
                            raiseTopmost(true);
                        }
                    });
                }));

        zoomController.dockPositionProperty().addListener((obs, o, v) ->
                Platform.runLater(() -> lastAppliedBounds = null));

        show();
    }

    /** Full monitor bounds for LENS stage. */
    private Rectangle2D fullBounds(MonitorInfo mon) {
        if (mon != null && mon.fxBounds().getWidth() >= 2) {
            return mon.fxBounds();
        }
        return Screen.getPrimary().getBounds();
    }

    private void sizeRoot(double w, double h) {
        root.setPrefSize(w, h);
        root.setMinSize(w, h);
        root.setMaxSize(w, h);
        root.resize(w, h);
    }

    private void applyStageBounds(Rectangle2D bounds) {
        if (bounds == null || bounds.getWidth() < 2 || bounds.getHeight() < 2) return;
        if (lastAppliedBounds != null
                && Math.abs(lastAppliedBounds.getMinX() - bounds.getMinX()) < 0.5
                && Math.abs(lastAppliedBounds.getMinY() - bounds.getMinY()) < 0.5
                && Math.abs(lastAppliedBounds.getWidth() - bounds.getWidth()) < 0.5
                && Math.abs(lastAppliedBounds.getHeight() - bounds.getHeight()) < 0.5) {
            return;
        }
        lastAppliedBounds = bounds;
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        setX(bounds.getMinX());
        setY(bounds.getMinY());
        setWidth(w);
        setHeight(h);
        sizeRoot(w, h);
        if (getScene() != null) {
            getScene().setFill(Color.TRANSPARENT);
        }
    }

    /**
     * Desired stage rect (usually full monitor). Must keep applying even while
     * Settings is open — otherwise the glass freezes while capture still tracks the mouse.
     */
    private void onContentBounds(Rectangle2D bounds) {
        if (!zoomController.isRunning()) return;
        if (bounds == null) return;

        // Yield to Start / tray overflow: hide so system UI is not duplicated
        long now = System.nanoTime();
        boolean winKeyHold = now < shellYieldUntilNanos;
        boolean shell = !settingsOpen && (winKeyHold || WindowNative.isShellUiForeground());
        if (shell) {
            if (!shellUiYielded) {
                shellUiYielded = true;
                raiseTopmost(false);
                if (isShowing()) {
                    hide();
                }
            }
            return;
        } else if (shellUiYielded) {
            shellUiYielded = false;
            if (!isShowing() && zoomController.isRunning()) {
                show();
                Platform.runLater(() -> {
                    initNativeWindow();
                    forceClickThroughPolicy();
                    if (!settingsOpen) {
                        raiseTopmost(true);
                    }
                });
            } else if (!settingsOpen) {
                raiseTopmost(true);
            }
        }

        applyStageBounds(bounds);
    }

    /** Call when dock position / mode needs the stage to re-measure immediately. */
    public void invalidateStageBounds() {
        lastAppliedBounds = null;
    }

    /**
     * Called when the user presses the Windows key — hide magnifier so Start menu
     * is not covered by the always-on-top glass.
     */
    public void yieldToShellUi() {
        shellYieldUntilNanos = System.nanoTime() + 2_500_000_000L; // ~2.5s
        Platform.runLater(() -> {
            shellUiYielded = true;
            raiseTopmost(false);
            if (isShowing()) {
                hide();
            }
        });
    }

    private void onMonitorChanged(MonitorInfo mon) {
        if (mon == null) return;
        lastMonitor = mon;
        if (zoomController.getMode() == ZoomMode.LENS) {
            applyStageBounds(fullBounds(mon));
            Platform.runLater(() -> {
                initNativeWindow();
                forceClickThroughPolicy();
            });
        }
    }

    private void onVisibility(MonitorInfo mon, boolean running) {
        if (settingsOpen) return;
        if (running) {
            if (!isShowing() && !shellUiYielded) {
                show();
                Platform.runLater(() -> {
                    initNativeWindow();
                    forceClickThroughPolicy();
                    raiseTopmost(true);
                });
            }
        } else {
            hide();
        }
    }

    private void initNativeWindow() {
        try {
            hwnd = WindowNative.findHwnd(this);
            if (hwnd != null) {
                // Always re-apply: hwnd can change after show/resize; without this GDI goes black
                WindowNative.excludeFromCapture(hwnd);
            }
        } catch (Throwable t) {
            System.err.println("Native overlay setup failed: " + t.getMessage());
        }
    }

    private boolean shouldClickThrough() {
        // Always pass clicks — LENS and DOCKED never trap the mouse
        return true;
    }

    /**
     * While Settings is open: keep the magnifier running, but demote it so Settings
     * stays on top and clickable. (Hiding the lens made it feel like "commands died".)
     */
    public void setSettingsOpen(boolean open) {
        this.settingsOpen = open;
        Platform.runLater(() -> {
            if (open) {
                // Do NOT hide — user still needs to see/test zoom while configuring.
                shellUiYielded = false;
                shellYieldUntilNanos = 0;
                raiseTopmost(false);
                forceClickThroughPolicy();
                // Ensure still visible if a previous yield hid it
                if (zoomController.isRunning() && !isShowing()) {
                    show();
                    Platform.runLater(() -> {
                        initNativeWindow();
                        forceClickThroughPolicy();
                    });
                }
            } else {
                shellUiYielded = false;
                shellYieldUntilNanos = 0;
                restoreMagnifier();
            }
        });
    }

    /** Force-show the lens after Settings / shell yield. */
    public void restoreMagnifier() {
        Platform.runLater(() -> {
            shellUiYielded = false;
            shellYieldUntilNanos = 0;
            if (!zoomController.isRunning()) {
                zoomController.setRunning(true);
            }
            if (!isShowing()) {
                show();
            }
            Platform.runLater(() -> {
                if (lastMonitor != null && zoomController.getMode() == ZoomMode.LENS) {
                    applyStageBounds(fullBounds(lastMonitor));
                }
                initNativeWindow();
                forceClickThroughPolicy();
                raiseTopmost(true);
            });
        });
    }

    private void forceClickThroughPolicy() {
        applyClickThrough(shouldClickThrough());
        Platform.runLater(() -> applyClickThrough(shouldClickThrough()));
    }

    private void applyClickThrough(boolean enabled) {
        boolean want = shouldClickThrough();
        if (enabled != want) {
            enabled = want;
        }
        this.clickThrough = enabled;
        root.setMouseTransparent(true);
        if (getScene() != null) {
            getScene().setFill(Color.TRANSPARENT);
        }
        if (hwnd != null) {
            WindowNative.setClickThrough(hwnd, enabled);
            lastClickThroughAssertNanos = System.nanoTime();
        } else {
            Platform.runLater(() -> {
                initNativeWindow();
                if (hwnd != null) {
                    WindowNative.setClickThrough(hwnd, shouldClickThrough());
                    lastClickThroughAssertNanos = System.nanoTime();
                }
            });
        }
    }

    private void reassertClickThroughIfNeeded() {
        long now = System.nanoTime();
        if (now - lastClickThroughAssertNanos < 500_000_000L) {
            return;
        }
        lastClickThroughAssertNanos = now;
        boolean want = shouldClickThrough();
        if (hwnd == null) {
            initNativeWindow();
        }
        if (hwnd != null) {
            WindowNative.setClickThrough(hwnd, want);
            // Do NOT reassert topmost every half-second — that steals Start menu.
            // Only gently re-raise if shell is not open and we haven't raised recently.
            if (!shellUiYielded && !WindowNative.isShellUiForeground()
                    && now - lastTopmostAssertNanos > 3_000_000_000L) {
                raiseTopmost(true);
            }
        }
        this.clickThrough = want;
        root.setMouseTransparent(true);
    }

    private void raiseTopmost(boolean topmost) {
        lastTopmostAssertNanos = System.nanoTime();
        setAlwaysOnTop(topmost);
        if (hwnd == null) {
            initNativeWindow();
        }
        if (hwnd != null) {
            WindowNative.setTopmost(hwnd, topmost);
        }
    }

    public LensRenderer getLensRenderer() {
        return lensRenderer;
    }

    public Pane getRoot() {
        return root;
    }

    public void shutdown() {
        lensRenderer.stop();
        hide();
    }
}
