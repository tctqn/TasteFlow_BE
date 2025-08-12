package com.startup.tasteflowbe.dto.request;

import lombok.Data;
import java.util.List;

// DTO chính để tạo phiếu yêu cầu
@Data
public class CreateWarehouseRequestDTO {
    private Long warehouseId;
    private Long createdBy;
    private String notes;
    private List<RequestItemDTO> items;
}
