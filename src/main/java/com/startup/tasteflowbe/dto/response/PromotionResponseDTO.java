package com.startup.tasteflowbe.dto.response;

import com.startup.tasteflowbe.enums.DiscountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PromotionResponseDTO {
    private Long promotionId;
    private String name;
    private String description;
    private String imageUrl;

    private DiscountType discountType;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private Set<Long> applicableProductIds;
    private Set<Long> applicableCategoryIds;
    private Set<Long> applicableStoreIds;
}
