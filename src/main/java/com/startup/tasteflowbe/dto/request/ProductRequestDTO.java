package com.startup.tasteflowbe.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequestDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private String imageUrl;
    private Long categoryId;
    private List<Long> promotionIds;
}
