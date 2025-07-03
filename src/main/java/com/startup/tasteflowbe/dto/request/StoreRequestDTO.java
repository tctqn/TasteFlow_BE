package com.startup.tasteflowbe.dto.request;

import lombok.Data;
import com.startup.tasteflowbe.dto.response.StoreRequestItemDTO;
import java.util.List;

@Data
public class StoreRequestDTO {
    private Long storeId;
    private Long warehouseId;
    private String notes;
    private List<StoreRequestItemDTO> items;
}
