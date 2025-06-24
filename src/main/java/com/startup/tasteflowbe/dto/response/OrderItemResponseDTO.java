package com.startup.tasteflowbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDTO {
    private Long orderItemId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private Long productUnitId;
    private String productUnitName;
    private Integer quantity;
    private Integer quantityInBase;
    private BigDecimal price;
    private BigDecimal discount;
}
