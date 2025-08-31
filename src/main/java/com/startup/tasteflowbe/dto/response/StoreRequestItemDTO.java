package com.startup.tasteflowbe.dto.response;

import lombok.Data;

@Data
public class StoreRequestItemDTO {
    private Long productId;
    private String productName;
    private Long quantity;
    private Long unitId;
    private boolean isDirect;
}