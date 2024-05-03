package com.jelled.controller.Control;

public enum PatternType {
    COLORED_AMPLITUDE(1),
    ITERATING_COLORS(2);

    private final int id;

    private PatternType(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
