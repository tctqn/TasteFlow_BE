package com.startup.tasteflowbe.dto.response;

import lombok.Data;

import java.math.BigDecimal;

// ProductListItemDTO.java
@Data
public class ProductListItemDTO {
    private Long productId;
    private Long productUnitId;
    private String productName;
    private String unitName;
    private BigDecimal price;
    private BigDecimal discountedPrice;   // <= giá sau KM (nếu có)
    private String promoBadge;            // <= hiển thị "Giảm 10%" hoặc "-20.000"
    private Long appliedPromotionId;      // <= id KM áp dụng

    private String imageUrl;
    private String supplierName;
    private String sku;
    private String categoryName;
    private Boolean isDraft;

    // NEW: trạng thái tồn kho theo từng store
    private Integer availableQty;         // tổng tồn (theo base unit) ở store
    private Boolean outOfStock;           // true nếu availableQty == 0
}

