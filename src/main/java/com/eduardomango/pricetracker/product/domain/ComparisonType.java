package com.eduardomango.pricetracker.product.domain;

public enum ComparisonType {
    LOWER_THAN("menor que"),
    HIGHER_THAN("mayor que"),
    EQUALS("igual a");

    private final String label;

    ComparisonType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
