package com.startup.tasteflowbe.dto.request;

import com.startup.tasteflowbe.enums.MovementType;
import lombok.Data;

@Data
public class StockMovementRequestDTO {

    private Long warehouseId;

    private Long productId;

    private Long batchId;

    private MovementType movementType;

    private Integer quantity;

    private String note;
}