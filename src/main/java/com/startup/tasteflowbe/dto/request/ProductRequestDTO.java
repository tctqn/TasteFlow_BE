package com.startup.tasteflowbe.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.startup.tasteflowbe.model.Category;
import com.startup.tasteflowbe.model.Unit;

@Data
public class ProductRequestDTO {
    private String name;
    private String description;
    private String sku;
    private Category category;
    private Unit unit;
    private BigDecimal price;
    private String imageUrl;
    private List<Long> promotionIds;
}
