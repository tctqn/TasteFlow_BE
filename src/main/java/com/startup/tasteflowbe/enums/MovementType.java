package com.startup.tasteflowbe.enums;

public enum MovementType {
    IMPORT_BATCH("Nhập kho từ lô sản phẩm"),
    TRANSFER_TO_STORE("Xuất kho về cửa hàng"),
    SALE("Bán sản phẩm"),
    DAMAGE("Hàng bị hỏng, loại khỏi kho"),
    EXPIRED("Hàng hết hạn, loại khỏi kho");

    private final String description;

    MovementType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
