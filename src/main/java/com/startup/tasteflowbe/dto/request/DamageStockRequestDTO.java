package com.startup.tasteflowbe.dto.request;

import com.startup.tasteflowbe.enums.MovementType;
import lombok.Data;

@Data
public class DamageStockRequestDTO {
    private Long batchId;
    private Long warehouseId;
    private Long storeId;     
    private Long productId;
    private Integer damageQuantity;
    private String note;
    private MovementType movementType; // Cho phép phân biệt DAMAGE/EXPIRED
}
