package com.startup.tasteflowbe.dto.request;

import lombok.Data;

@Data
public class InventoryRequestDTO {
    private Long warehouseId;
    private Long productId;
    private Long batchId;
    private int quantity;
}
