package com.forzen.ui.theme;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Parent;
import javafx.scene.Scene;

/**
 * Holds the active Settings UI theme. Panels read {@link #current()} for colors.
 * Also paints JavaFX control palette so ComboBox/Spinner are not white on dark.
 */
public final class ThemeManager {

    private static final ThemeManager INSTANCE = new ThemeManager();

    private final ObjectProperty<AppTheme> current = new SimpleObjectProperty<>(ThemeCatalog.defaultTheme());

    private ThemeManager() {}

    public static ThemeManager get() {
        return INSTANCE;
    }

    public AppTheme current() {
        return current.get();
    }

    public ObjectProperty<AppTheme> currentProperty() {
        return current;
    }

    public void setThemeId(String id) {
        current.set(ThemeCatalog.get(ThemeCatalog.resolveId(id)));
    }

    public void setTheme(AppTheme theme) {
        current.set(theme == null ? ThemeCatalog.defaultTheme() : theme);
    }

    /**
     * Apply looked-up colors so default controls (combo, spinner, etc.) match Soft Dark.
     * Call after setScene and whenever the theme changes.
     */
    public void applyControlPalette(Scene scene) {
        if (scene == null) return;
        AppTheme t = current();
        Parent root = scene.getRoot();
        if (root == null) return;

        // JavaFX cascade: -fx-base drives most controls; inner/text fix combo/spinner popups
        String palette = String.join("; ",
                "-fx-base: " + t.surface(),
                "-fx-background: " + t.bg(),
                "-fx-control-inner-background: " + t.surface(),
                "-fx-text-base-color: " + t.text(),
                "-fx-text-inner-color: " + t.text(),
                "-fx-text-background-color: " + t.text(),
                "-fx-mark-color: " + t.muted(),
                "-fx-accent: " + t.accent(),
                "-fx-default-button: " + t.accent(),
                "-fx-focus-color: " + t.accent() + "88",
                "-fx-faint-focus-color: " + t.accent() + "22",
                "-fx-hover-base: " + t.surfaceHover(),
                "-fx-pressed-base: " + t.panel(),
                "-fx-color: " + t.surface()
        );

        String existing = root.getStyle() == null ? "" : root.getStyle();
        // Strip previous palette keys then append
        String cleaned = existing
                .replaceAll("-fx-base:[^;]*;?", "")
                .replaceAll("-fx-background:[^;]*;?", "")
                .replaceAll("-fx-control-inner-background:[^;]*;?", "")
                .replaceAll("-fx-text-base-color:[^;]*;?", "")
                .replaceAll("-fx-text-inner-color:[^;]*;?", "")
                .replaceAll("-fx-text-background-color:[^;]*;?", "")
                .replaceAll("-fx-mark-color:[^;]*;?", "")
                .replaceAll("-fx-accent:[^;]*;?", "")
                .replaceAll("-fx-default-button:[^;]*;?", "")
                .replaceAll("-fx-focus-color:[^;]*;?", "")
                .replaceAll("-fx-faint-focus-color:[^;]*;?", "")
                .replaceAll("-fx-hover-base:[^;]*;?", "")
                .replaceAll("-fx-pressed-base:[^;]*;?", "")
                .replaceAll("-fx-color:[^;]*;?", "")
                .replaceAll(";+", ";")
                .trim();
        if (cleaned.startsWith(";")) cleaned = cleaned.substring(1).trim();
        if (!cleaned.isEmpty() && !cleaned.endsWith(";")) cleaned = cleaned + "; ";
        root.setStyle(cleaned + palette);
    }
}
