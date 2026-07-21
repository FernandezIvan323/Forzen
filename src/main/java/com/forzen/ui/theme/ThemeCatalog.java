package com.forzen.ui.theme;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Soft Dark catalog — 5 presets. Legacy theme ids map to the closest preset.
 */
public final class ThemeCatalog {

    private static final Map<String, AppTheme> THEMES = new LinkedHashMap<>();
    private static final Map<String, String> LEGACY = new LinkedHashMap<>();

    static {
        // Flagship deep dark (true oscuro — no gris elevado)
        add(new AppTheme(
                "forzen_dark", "Forzen Dark",
                "#050506", // bg — casi negro
                "#0C0C0E", // panel
                "#141416", // surface (controles)
                "#1C1C20", // surfaceHover
                "#34D399", // accent
                "#34D39933",
                "#F4F4F5", // text
                "#9CA3AF", // muted
                "#F87171", // danger
                "#27272A", // border
                "#050506"  // onAccent
        ));

        add(new AppTheme(
                "midnight", "Midnight",
                "#0B1220", "#121A2A", "#152238", "#1C2C44",
                "#5B9DFF", "#5B9DFF33",
                "#E8EEF8", "#8BA0BF", "#FF5C7A", "#2A3F66", "#0B1220"
        ));

        add(new AppTheme(
                "high_contrast", "Alto contraste",
                "#000000", "#0A0A0A", "#111111", "#1A1A1A",
                "#FFFF00", "#FFFF0033",
                "#FFFFFF", "#DDDDDD", "#FF3333", "#FFFFFF", "#000000"
        ));

        add(new AppTheme(
                "slate", "Slate",
                "#0F1419", "#161C24", "#1C2430", "#252E3C",
                "#94A3B8", "#94A3B833",
                "#F1F5F9", "#94A3B8", "#EF4444", "#334155", "#0F1419"
        ));

        add(new AppTheme(
                "solar", "Solar (claro)",
                "#FFF8F0", "#F5EDE3", "#FFFFFF", "#F0E6DA",
                "#C45C26", "#C45C2633",
                "#2A1810", "#6B5344", "#B00020", "#E0D0C0", "#FFFFFF"
        ));

        // Legacy id → new id
        LEGACY.put("neon_green", "forzen_dark");
        LEGACY.put("ocean", "midnight");
        LEGACY.put("forest", "forzen_dark");
        LEGACY.put("rose", "midnight");
        LEGACY.put("amber", "solar");
        LEGACY.put("violet", "midnight");
    }

    private ThemeCatalog() {}

    private static void add(AppTheme t) {
        THEMES.put(t.id(), t);
    }

    public static List<AppTheme> all() {
        return List.copyOf(THEMES.values());
    }

    public static Set<String> ids() {
        return Set.copyOf(THEMES.keySet());
    }

    public static AppTheme get(String id) {
        if (id == null || id.isBlank()) {
            return defaultTheme();
        }
        String resolved = LEGACY.getOrDefault(id, id);
        return Optional.ofNullable(THEMES.get(resolved)).orElse(defaultTheme());
    }

    /** Resolve legacy theme id for persistence. */
    public static String resolveId(String id) {
        if (id == null || id.isBlank()) return defaultTheme().id();
        String resolved = LEGACY.getOrDefault(id, id);
        return THEMES.containsKey(resolved) ? resolved : defaultTheme().id();
    }

    public static AppTheme defaultTheme() {
        return THEMES.get("forzen_dark");
    }
}
