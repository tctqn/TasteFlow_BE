package com.startup.tasteflowbe.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
public class PromotionRequestDTO {
    private Long promotionId;

    private String name;
    private String description;

    private String discountType; // "PERCENT" | "AMOUNT"
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;

    private LocalDate startDate;
    private LocalTime startTime;
    private LocalDate endDate;
    private LocalTime endTime;

    private Set<Long> applicableProducts;
    private Set<Long> applicableCategories;
    private Set<Long> applicableStores;
}

