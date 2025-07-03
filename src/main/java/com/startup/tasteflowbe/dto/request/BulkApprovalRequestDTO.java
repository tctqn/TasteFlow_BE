package com.startup.tasteflowbe.dto.request;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class BulkApprovalRequestDTO {
    private String status;
    private List<ApprovalItemInfo> items;

    @Data
    public static class ApprovalItemInfo {
        private Integer itemId;
        private Integer approvedQuantity;
        private Integer supplierId;
        private LocalDate expiresAt;
    }
}