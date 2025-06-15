package com.startup.tasteflowbe.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDTO {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
}
