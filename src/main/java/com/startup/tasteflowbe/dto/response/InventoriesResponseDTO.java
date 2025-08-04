package com.startup.tasteflowbe.dto.response;

import lombok.Data;

import com.startup.tasteflowbe.model.Warehouse;

@Data
public class InventoriesResponseDTO {
    // Từ Inventory Entity
    private Long inventoryId;
    private Integer quantity;
    private Integer reorderLevel;
    // Từ Warehouse Entity
    private Warehouse warehouseId;
    // Từ Store Entity
    private Long storeId;
    // Từ Product Entity
    private ProductResponseDTO product;
    // Từ ProductBatch Entity
    private ProductBatchResponseDTO batchId;
}