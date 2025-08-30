package com.startup.tasteflowbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAllocationDTO {
    private Long batchId;
    private Integer available;      // tồn lô hiện có (base unit)
    private Integer allocate;       // lượng đề xuất xuất từ lô này (base unit)
    private String expiryDate;      // nếu có
}