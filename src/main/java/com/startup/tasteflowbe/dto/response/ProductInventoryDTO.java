package com.startup.tasteflowbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductInventoryDTO {
    private Long productId;
    private String productName;
    private List<ProductUnitStockDTO> units;
}