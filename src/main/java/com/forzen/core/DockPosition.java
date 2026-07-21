package com.forzen.core;

public enum DockPosition {
    TOP_LEFT("Arriba izquierda"),
    TOP_RIGHT("Arriba derecha"),
    BOTTOM_LEFT("Abajo izquierda"),
    BOTTOM_RIGHT("Abajo derecha"),
    CENTER("Centro");

    private final String label;

    DockPosition(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
