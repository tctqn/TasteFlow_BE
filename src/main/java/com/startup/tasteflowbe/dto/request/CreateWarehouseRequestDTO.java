package com.startup.tasteflowbe.dto.request;

import lombok.Data;
import java.util.List;

// DTO chính để tạo phiếu yêu cầu
@Data
public class CreateWarehouseRequestDTO {
    private Integer warehouseId;
    private Integer createdBy;
    private String notes;
    private List<RequestItemDTO> items;
}
