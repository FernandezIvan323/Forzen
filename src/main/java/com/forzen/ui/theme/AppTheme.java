package com.forzen.ui.theme;

/**
 * Color tokens for the Settings UI (Soft Dark system).
 */
public record AppTheme(
        String id,
        String displayName,
        String bg,
        String panel,
        String surface,
        String surfaceHover,
        String accent,
        String accentMuted,
        String text,
        String muted,
        String danger,
        String border,
        String onAccent
) {
    /** Compact constructor for themes that share surface = panel. */
    public AppTheme(String id, String displayName, String bg, String panel, String accent,
                    String text, String muted, String danger, String border) {
        this(id, displayName, bg, panel, panel, lightenHex(panel), accent, accent + "33",
                text, muted, danger, border, pickOnAccent(bg, accent));
    }

    public String cssBg() {
        return "-fx-background-color: " + bg + ";";
    }

    public String titleStyle() {
        return "-fx-text-fill: " + text + "; -fx-font-size: 22px; -fx-font-weight: bold;";
    }

    public String subtitleStyle() {
        return "-fx-text-fill: " + muted + "; -fx-font-size: 13px;";
    }

    public String sectionLabelStyle() {
        return "-fx-text-fill: " + muted + "; -fx-font-size: 11px; -fx-font-weight: bold; "
                + "-fx-letter-spacing: 0.04em;";
    }

    public String bodyStyle() {
        return "-fx-text-fill: " + text + "; -fx-font-size: 13px;";
    }

    public String mutedStyle() {
        return "-fx-text-fill: " + muted + "; -fx-font-size: 12px;";
    }

    public String valueStyle() {
        return "-fx-text-fill: " + accent + "; -fx-font-size: 15px; -fx-font-weight: bold;";
    }

    public String cardStyle() {
        return "-fx-background-color: " + surface + "; -fx-background-radius: 12; "
                + "-fx-border-color: " + border + "; -fx-border-radius: 12; -fx-border-width: 1; "
                + "-fx-padding: 16 18;";
    }

    public String sidebarItemStyle() {
        return "-fx-text-fill: " + muted + "; -fx-font-size: 13px; -fx-padding: 10 14; "
                + "-fx-background-radius: 8; -fx-cursor: hand;";
    }

    public String sidebarItemHoverStyle() {
        return "-fx-text-fill: " + text + "; -fx-font-size: 13px; -fx-padding: 10 14; "
                + "-fx-background-color: " + surfaceHover + "; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    public String sidebarItemActiveStyle() {
        return "-fx-text-fill: " + text + "; -fx-font-size: 13px; -fx-font-weight: bold; "
                + "-fx-padding: 10 14; -fx-background-color: " + surfaceHover + "; "
                + "-fx-background-radius: 8; -fx-border-color: " + accent + "; "
                + "-fx-border-width: 0 0 0 3; -fx-cursor: hand;";
    }

    /** @deprecated use {@link #sidebarItemStyle()} */
    public String sidebarMutedStyle() {
        return sidebarItemStyle();
    }

    /** @deprecated use {@link #sidebarItemHoverStyle()} */
    public String sidebarHoverStyle() {
        return sidebarItemHoverStyle();
    }

    public String applyBtnStyle() {
        return "-fx-background-color: " + accent + "; -fx-text-fill: " + onAccent
                + "; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 22; "
                + "-fx-background-radius: 8; -fx-cursor: hand;";
    }

    public String secondaryBtnStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + text
                + "; -fx-font-size: 13px; -fx-padding: 10 18; -fx-background-radius: 8; "
                + "-fx-border-color: " + border + "; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;";
    }

    public String resetBtnStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + danger
                + "; -fx-font-size: 13px; -fx-padding: 10 18; -fx-background-radius: 8; "
                + "-fx-border-color: " + danger + "55; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;";
    }

    public String segmentIdleStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: " + muted
                + "; -fx-font-size: 12px; -fx-padding: 8 14; -fx-background-radius: 8; -fx-cursor: hand;";
    }

    public String segmentActiveStyle() {
        return "-fx-background-color: " + accent + "; -fx-text-fill: " + onAccent
                + "; -fx-font-size: 12px; -fx-font-weight: bold; -fx-padding: 8 14; "
                + "-fx-background-radius: 8; -fx-cursor: hand;";
    }

    public String segmentBarStyle() {
        return "-fx-background-color: " + panel + "; -fx-background-radius: 10; "
                + "-fx-border-color: " + border + "; -fx-border-radius: 10; -fx-border-width: 1; -fx-padding: 3;";
    }

    private static String pickOnAccent(String bg, String accent) {
        // Dark backgrounds → dark text on bright accent; light → white on accent
        if (bg != null && (bg.startsWith("#F") || bg.startsWith("#f") || bg.startsWith("#E") || bg.startsWith("#e"))) {
            return "#FFFFFF";
        }
        return "#0B0D10";
    }

    private static String lightenHex(String hex) {
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            if (h.length() != 6) return hex;
            int r = Math.min(255, Integer.parseInt(h.substring(0, 2), 16) + 12);
            int g = Math.min(255, Integer.parseInt(h.substring(2, 4), 16) + 12);
            int b = Math.min(255, Integer.parseInt(h.substring(4, 6), 16) + 12);
            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            return hex;
        }
    }
}
