package com.startup.tasteflowbe.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUnitDTO {
    private Long productUnitId;
    private String unitName;
    private Integer conversionRate;
    private BigDecimal price;
    private String sku;
    private String imageUrl;
    private String description;
}

