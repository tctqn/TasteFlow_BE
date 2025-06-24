package com.startup.tasteflowbe.dto.request;

import com.startup.tasteflowbe.enums.DiscountType;
import com.startup.tasteflowbe.enums.DistributionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateVoucherRequest {

    @NotBlank
    private String code;

    private String title;

    private String description;

    @NotNull
    private DistributionType distributionType;

    private Boolean freeShipping;

    @NotNull
    private DiscountType discountType;

    private BigDecimal discountAmount;

    private BigDecimal discountPercent;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime endDate;

    private boolean stackable;

    @NotNull
    private Integer quantity;

    private Integer claimedCount;

    private BigDecimal minOrderAmount;

    @NotNull
    private Integer maxPerUser;

    private List<Long> productIds;

    private List<Long> categoryIds;
}