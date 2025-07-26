package com.startup.tasteflowbe.dto.response;

import lombok.Data;

import java.util.List;

import com.startup.tasteflowbe.model.Category;

@Data
public class ProductDetailDTO {
    private Long productId;
    private String name;
    private Category category;
    private String description;
    private String sku;
    private String imageUrl;
    private List<ProductUnitDTO> units;
}
