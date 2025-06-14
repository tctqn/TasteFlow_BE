package com.startup.tasteflowbe.dto.request;

import lombok.Data;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Int;

import com.startup.tasteflowbe.dto.response.StoreRequestItemDTO;
import java.util.List;

@Data
public class StoreInventoryRequestDTO {
    private Integer quantity;
    private Long batchId;
    private Long warehouseId;
    private Long productId;
    private String notes;
    private String status;
    private Integer reorder_level;
    private Long requestId;
    private Long storeId;
}
