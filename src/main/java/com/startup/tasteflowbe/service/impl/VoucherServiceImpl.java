package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.CreateVoucherRequest;
import com.startup.tasteflowbe.dto.response.VoucherResponseDTO;
import com.startup.tasteflowbe.enums.DistributionType;
import com.startup.tasteflowbe.mapper.VoucherMapper;
import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.repository.CategoryRepository;
import com.startup.tasteflowbe.repository.ProductRepository;
import com.startup.tasteflowbe.repository.UserVoucherRepository;
import com.startup.tasteflowbe.repository.VoucherRepository;
import com.startup.tasteflowbe.service.VoucherService;
import com.startup.tasteflowbe.validator.VoucherValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final VoucherValidator voucherValidator;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    @Override
    @Transactional
    public Voucher createVoucher(CreateVoucherRequest request) {
        voucherValidator.validate(request);
        Set<Product> products = request.getProductIds() != null
                ? new HashSet<>(productRepository.findAllById(request.getProductIds()))
                : new HashSet<>();

        Set<Category> categories = request.getCategoryIds() != null
                ? new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()))
                : new HashSet<>();

        Voucher voucher = VoucherMapper.toEntity(request, products, categories);
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher updateVoucher(Long id, CreateVoucherRequest voucher) {
        return voucherRepository.findById(id)
                .map(existingVoucher -> {
                    existingVoucher.setCode(voucher.getCode());
                    existingVoucher.setDiscountAmount(voucher.getDiscountAmount());
                    existingVoucher.setDiscountType(voucher.getDiscountType());
                    existingVoucher.setStartDate(voucher.getStartDate());
                    existingVoucher.setEndDate(voucher.getEndDate());
                    return voucherRepository.save(existingVoucher);
                })
                .orElseThrow(() -> new RuntimeException("Voucher not found with id " + id));
    }

    @Override
    public List<VoucherResponseDTO> getAvailableVouchers(User user, BigDecimal totalPrice) {
        LocalDateTime now = LocalDateTime.now();

        List<Voucher> publicVouchers = voucherRepository
                .findAllByDistributionTypeAndStartDateBeforeAndEndDateAfterAndQuantityGreaterThan(
                        DistributionType.PUBLIC, now, now, 0);

        List<UserVoucher> claimed = userVoucherRepository.findByUser_UserId(user.getUserId());

        Set<Long> claimedIds = claimed.stream()
                .map(uv -> uv.getVoucher().getVoucherId())
                .collect(Collectors.toSet());

        List<VoucherResponseDTO> result = new ArrayList<>();

        for (Voucher v : publicVouchers) {
            boolean isClaimed = claimedIds.contains(v.getVoucherId());
            boolean isValid = totalPrice.compareTo(Optional.ofNullable(v.getMinOrderAmount()).orElse(BigDecimal.ZERO)) >= 0;

            result.add(VoucherResponseDTO.builder()
                    .voucherId(v.getVoucherId())
                    .code(v.getCode())
                    .title(v.getTitle())
                    .discountAmount(v.getDiscountAmount())
                    .discountPercent(v.getDiscountPercent())
                    .minOrderAmount(v.getMinOrderAmount())
                    .isStackable(v.isStackable())
                    .claimed(isClaimed)
                    .used(false)
                    .valid(isValid)
                    .discountType(v.getDiscountType().name())
                    .build());
        }

        for (UserVoucher uv : claimed) {
            Voucher v = uv.getVoucher();
            boolean isValid = totalPrice.compareTo(Optional.ofNullable(v.getMinOrderAmount()).orElse(BigDecimal.ZERO)) >= 0;

            result.add(VoucherResponseDTO.builder()
                    .voucherId(v.getVoucherId())
                    .code(v.getCode())
                    .title(v.getTitle())
                    .discountAmount(v.getDiscountAmount())
                    .discountPercent(v.getDiscountPercent())
                    .minOrderAmount(v.getMinOrderAmount())
                    .isStackable(v.isStackable())
                    .claimed(true)
                    .used(uv.isUsed())
                    .valid(isValid && !uv.isUsed())
                    .discountType(v.getDiscountType().name())
                    .build());
        }

        return result;
    }

    @Override
    public List<VoucherResponseDTO> getPublicVouchers(BigDecimal totalPrice) {
        LocalDateTime now = LocalDateTime.now();

        List<Voucher> publicVouchers = voucherRepository
                .findAllByDistributionTypeAndStartDateBeforeAndEndDateAfterAndQuantityGreaterThan(
                        DistributionType.PUBLIC, now, now, 0);


        return publicVouchers.stream()
                .map(v -> VoucherResponseDTO.builder()
                        .voucherId(v.getVoucherId())
                        .code(v.getCode())
                        .title(v.getTitle())
                        .discountAmount(v.getDiscountAmount())
                        .discountPercent(v.getDiscountPercent())
                        .minOrderAmount(v.getMinOrderAmount())
                        .isStackable(v.isStackable())
                        .claimed(false)
                        .used(false)
                        .valid(totalPrice.compareTo(Optional.ofNullable(v.getMinOrderAmount()).orElse(BigDecimal.ZERO)) >= 0)
                        .discountType(v.getDiscountType().name())
                        .build())
                .toList();
    }


    @Override
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }
}