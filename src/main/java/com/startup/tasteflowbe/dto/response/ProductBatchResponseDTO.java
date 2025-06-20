package com.startup.tasteflowbe.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.startup.tasteflowbe.model.Supplier;
import com.startup.tasteflowbe.model.Unit;
import com.startup.tasteflowbe.model.Warehouse;

import lombok.Data;

@Data
public class ProductBatchResponseDTO {

    private Long batchId;
    private Integer quantity;
    private LocalDate manufactureDate;
    private LocalDate expirationDate;
    private LocalDateTime receivedDate;
    private String status;
    private BigDecimal importPrice;
    private String note;
    private ProductResponseDTO product;
    private Supplier supplierId;
    private Warehouse warehouseId;
    private String unitName;   
    private Unit unit;

}
