package com.startup.tasteflowbe.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherResponseDTO {
    private Long voucherId;
    private String code;
    private String title;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private BigDecimal minOrderAmount;
    private boolean isStackable;
    private boolean claimed;
    private boolean used;
    private boolean valid;
    private String discountType;
}