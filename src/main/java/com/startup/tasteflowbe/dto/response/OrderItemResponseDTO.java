package com.startup.tasteflowbe.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemResponseDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
}
