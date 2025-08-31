package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.MovementType;
import com.startup.tasteflowbe.model.Warehouse;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockMovementResponseDTO {

    private Long movementId;
    private MovementType movementType;
    private Integer quantity;
    private LocalDateTime movementDate;
    private String note;
    private Warehouse warehouse;
    private Long storeId;
    private Long productId;
    private BigDecimal price;
    private String productName;
    private ProductBatchResponseDTO productBatchResponseDTO;
    private Long storeRequestId;
    private String storeRequestStatus;
}