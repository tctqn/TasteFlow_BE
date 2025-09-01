package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.ItemCondition;
import com.startup.tasteflowbe.enums.ReturnResolution;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnItemResponseDTO {
    private Long id;
    private Long orderItemId;
    private Long productId;
    private String productName;
    private BigDecimal qty;
    private ItemCondition condition;
    private ReturnResolution resolution;
    private BigDecimal refundAmount;
}