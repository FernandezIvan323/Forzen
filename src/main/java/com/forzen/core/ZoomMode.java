package com.forzen.core;

/**
 * Magnifier presentation modes.
 * FULL was removed (clip/blur issues) — only LENS and DOCKED remain.
 */
public enum ZoomMode {
    LENS,
    DOCKED;

    /**
     * Parse prefs / legacy values. Unknown or retired FULL → LENS.
     */
    public static ZoomMode fromStored(String raw) {
        if (raw == null || raw.isBlank()) return LENS;
        try {
            ZoomMode m = ZoomMode.valueOf(raw.trim());
            return m;
        } catch (Exception e) {
            // Legacy: FULL or typos
            if ("FULL".equalsIgnoreCase(raw.trim())) {
                return LENS;
            }
            return LENS;
        }
    }
}
