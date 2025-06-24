// VoucherMapper.java
package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.request.CreateVoucherRequest;
import com.startup.tasteflowbe.model.Category;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.Voucher;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VoucherMapper {

    public static Voucher toEntity(CreateVoucherRequest dto,
                                   Set<Product> products,
                                   Set<Category> categories) {
        return Voucher.builder()
                .code(dto.getCode())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .distributionType(dto.getDistributionType())
                .freeShipping(dto.getFreeShipping())
                .discountType(dto.getDiscountType())
                .discountAmount(dto.getDiscountAmount())
                .discountPercent(dto.getDiscountPercent())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .quantity(dto.getQuantity())
                .claimedCount(dto.getClaimedCount() != null ? dto.getClaimedCount() : 0)
                .minOrderAmount(dto.getMinOrderAmount())
                .maxPerUser(dto.getMaxPerUser())
                .applicableProducts(products != null ? products : new HashSet<>())
                .applicableCategories(categories != null ? categories : new HashSet<>())
                .build();
    }
}
