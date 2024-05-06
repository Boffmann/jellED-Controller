package com.jelled.controller.Control;

public enum PatternType {
    COLORED_AMPLITUDE(1),
    ITERATING_COLORS(2);

    private final int id;

    PatternType(final int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PatternType fromString(final String typeString) {
        for (final PatternType type : PatternType.values()) {
            if (type.name().equalsIgnoreCase(typeString)) {
                return type;
            }
        }

        return null;
    }
}
