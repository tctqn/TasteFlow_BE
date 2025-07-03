package com.startup.tasteflowbe.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponseDTO {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private String imageUrl;
    private LocalDateTime createdAt;
    private String categoryName;
    private List<PromotionResponseDTO> promotions;
}
