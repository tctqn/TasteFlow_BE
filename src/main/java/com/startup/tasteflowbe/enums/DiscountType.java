package com.startup.tasteflowbe.enums;

public enum DiscountType {
    AMOUNT("AMOUNT"),
    PERCENT("PERCENT");

    private final String value;

    DiscountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static DiscountType fromValue(String value) {
        for (DiscountType type : DiscountType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid DiscountType: " + value);
    }
}
