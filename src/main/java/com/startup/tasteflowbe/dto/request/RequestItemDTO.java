package com.startup.tasteflowbe.dto.request;

import lombok.Data;
import java.util.List;

// DTO cho các mặt hàng trong phiếu yêu cầu
@Data
public class RequestItemDTO {
    private Integer productUnitId;
    private Integer quantity;
    private String note;
}
