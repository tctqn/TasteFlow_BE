package com.startup.tasteflowbe.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class ItemApprovalDTO {
    private String status;
    private Integer approvedQuantity;
    private Integer supplierId;
    private LocalDate expiresAt;
}
