package com.startup.tasteflowbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemAvailabilityDTO {
    private Integer requestedQtyInBase;   // số cần (base unit), dùng quantityInBase
    private Integer totalAvailableInBase; // tổng tồn (base unit)
    private boolean canFulfill;           // đủ hay không
    private List<BatchAllocationDTO> allocations;
}