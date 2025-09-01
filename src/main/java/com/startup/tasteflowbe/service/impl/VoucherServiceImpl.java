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
import java.time.ZoneId;
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
    public List<VoucherResponseDTO> getAvailableVouchers(User user, BigDecimal cartTotalPrice) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // 1) PUBLIC còn thời gian
        List<Voucher> publicActive = voucherRepository
                .findAllByDistributionTypeAndStartDateBeforeAndEndDateAfterAndQuantityGreaterThan(
                        DistributionType.PUBLIC, now, now, 0);

        // 2) Tất cả claim của user
        List<UserVoucher> userClaims = userVoucherRepository.findByUser_UserId(user.getUserId());

        Map<Long, VoucherResponseDTO> merged = new LinkedHashMap<>();

        // Helper build dto
        java.util.function.Function<Voucher, VoucherResponseDTO> buildDto = v -> {
            boolean minOk = cartTotalPrice.compareTo(
                    Optional.ofNullable(v.getMinOrderAmount()).orElse(BigDecimal.ZERO)) >= 0;

            Integer quantity = Optional.ofNullable(v.getQuantity()).orElse(0);
            int claimedAll = userVoucherRepository.countClaimed(v);
            int remainingGlobal = Math.max(0, quantity - claimedAll);

            Integer maxPerUser = Optional.ofNullable(v.getMaxPerUser()).orElse(0);
            int usedByUser = userVoucherRepository.countUsedByUserAndVoucher(user, v);
            int remainingForUser = (maxPerUser > 0) ? Math.max(0, maxPerUser - usedByUser) : Integer.MAX_VALUE;

            boolean userClaimed = userVoucherRepository.countClaimedByUser(user, v) > 0;
            boolean anyUnusedClaim = userClaims.stream()
                    .anyMatch(uv -> uv.getVoucher().getVoucherId().equals(v.getVoucherId()) && !uv.isUsed());

            boolean validTime = !(v.getStartDate().isAfter(now) || v.getEndDate().isBefore(now));
            boolean valid =
                    validTime
                            && minOk
                            && remainingGlobal > 0
                            && remainingForUser > 0
                            && (!userClaimed || anyUnusedClaim);

            return VoucherResponseDTO.builder()
                    .voucherId(v.getVoucherId())
                    .code(v.getCode())
                    .title(v.getTitle())
                    .description(v.getDescription())
                    .discountAmount(v.getDiscountAmount())
                    .discountPercent(v.getDiscountPercent())
                    .minOrderAmount(v.getMinOrderAmount())
                    .freeShipping(Boolean.TRUE.equals(v.getFreeShipping()))
                    .discountType(v.getDiscountType().name())
                    .isStackable(v.isStackable())
                    .claimed(userClaimed)
                    .used(userClaimed && !anyUnusedClaim) // true nếu user đã claim và không còn claim nào unused
                    .valid(valid)
                    .build();
        };

        // 3) Ưu tiên merge voucher user đã claim
        for (UserVoucher uv : userClaims) {
            Voucher v = uv.getVoucher();
            merged.put(v.getVoucherId(), buildDto.apply(v));
        }

        // 4) Thêm PUBLIC chưa claim
        for (Voucher v : publicActive) {
            merged.putIfAbsent(v.getVoucherId(), buildDto.apply(v));
        }

        // 5) Kết quả
        List<VoucherResponseDTO> result = new ArrayList<>(merged.values());

        result.removeIf(dto -> dto.isClaimed() && dto.isUsed());

        result.removeIf(dto -> !dto.isValid());

        // Sắp xếp: ưu tiên valid trước, rồi claimed
        result.sort((a, b) -> {
            int byValid = Boolean.compare(b.isValid(), a.isValid());
            if (byValid != 0) return byValid;
            int byClaimed = Boolean.compare(b.isClaimed(), a.isClaimed());
            if (byClaimed != 0) return byClaimed;
            return 0;
        });

        return result;
    }


    @Override
    public List<VoucherResponseDTO> getPublicVouchers(BigDecimal totalPrice) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<Voucher> publicVouchers = voucherRepository
                .findAllByDistributionTypeAndStartDateBeforeAndEndDateAfterAndQuantityGreaterThan(
                        DistributionType.PUBLIC, now, now, 0);


        return publicVouchers.stream()
                .map(VoucherMapper::toResponseDTO)
                .collect(Collectors.toList());
    }


    @Override
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }
}