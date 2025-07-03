package com.startup.tasteflowbe.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class ProductDetailDTO {
    private Long productId;
    private String name;
    private String description;
    private String sku;
    private String imageUrl;
    private List<ProductUnitDTO> units;
}
