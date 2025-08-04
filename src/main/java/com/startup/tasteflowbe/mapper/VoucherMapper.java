// VoucherMapper.java
package com.startup.tasteflowbe.mapper;

import com.startup.tasteflowbe.dto.request.CreateVoucherRequest;
import com.startup.tasteflowbe.dto.response.VoucherResponseDTO;
import com.startup.tasteflowbe.model.Category;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.Voucher;

import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

public class VoucherMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    public static Voucher toEntity(CreateVoucherRequest dto,
                                   Set<Product> products,
                                   Set<Category> categories) {
        return Voucher.builder()
                .code(dto.getCode())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .distributionType(dto.getDistributionType())
                .freeShipping(dto.getFreeShipping())
                .isStackable(dto.getStackable() != null ? dto.getStackable() : false)
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

    public static VoucherResponseDTO toResponseDTO(Voucher voucher) {
        return VoucherResponseDTO.builder()
                .voucherId(voucher.getVoucherId())
                .code(voucher.getCode())
                .title(voucher.getTitle())
                .discountAmount(voucher.getDiscountAmount())
                .discountPercent(voucher.getDiscountPercent())
                .minOrderAmount(voucher.getMinOrderAmount())
                .isStackable(voucher.isStackable())
                .freeShipping(voucher.getFreeShipping())
                .description(voucher.getDescription())
                .distributionType(voucher.getDistributionType().name())
                .startDate(voucher.getStartDate().format(formatter))
                .endDate(voucher.getEndDate().format(formatter))
                .maxPerUser(voucher.getMaxPerUser())
                .quantity(voucher.getQuantity())
                .claimedCount(voucher.getClaimedCount())
                .claimed(false) // Hoặc xử lý thực tế nếu có logic check đã claim
                .used(false)    // Hoặc xử lý thực tế nếu có logic check đã dùng
                .valid(true)    // Tùy vào logic, có thể kiểm tra startDate, endDate, quantity > claimedCount
                .discountType(voucher.getDiscountType().name())
                .build();
    }
}
