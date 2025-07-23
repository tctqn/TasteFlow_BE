package com.startup.tasteflowbe.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductListItemDTO {
    private Long productId;
    private Long productUnitId;
    private String productName;
    private String unitName;
    private BigDecimal price;
    private String imageUrl;
    private String supplierName;
    private String sku;
    private String categoryName;
    private Boolean isDraft;
}
