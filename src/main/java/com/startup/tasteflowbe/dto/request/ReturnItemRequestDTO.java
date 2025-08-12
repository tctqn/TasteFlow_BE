package com.startup.tasteflowbe.dto.request;

import com.startup.tasteflowbe.enums.ItemCondition;
import com.startup.tasteflowbe.enums.ReturnResolution;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReturnItemRequestDTO {
    @NotNull
    private Long orderItemId;
    @NotNull private Long productId;
    @NotNull @DecimalMin("0.001") private BigDecimal qty;
    @NotNull private ItemCondition condition;
    @NotNull private ReturnResolution resolution;
    @DecimalMin("0.00") private BigDecimal refundAmount;
}
