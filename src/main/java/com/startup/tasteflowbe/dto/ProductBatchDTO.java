package com.startup.tasteflowbe.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProductBatchDTO {
    private Long productId;
    private Long supplierId;
    private Long warehouseId;
    private Long unitId;
    private Integer quantity;
    private LocalDate manufactureDate;
    private String note;
}