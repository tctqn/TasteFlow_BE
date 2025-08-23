package com.startup.tasteflowbe.dto.request;

import lombok.Data;

// DTO cho các mặt hàng trong phiếu yêu cầu
@Data
public class RequestItemDTO {
    private Long productUnitId;
    private Integer quantity;
    private String note;
    private boolean directInput;
    private Long supplierId;
}
