package com.startup.tasteflowbe.validator;

import com.startup.tasteflowbe.dto.request.CreateVoucherRequest;
import com.startup.tasteflowbe.enums.DiscountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class VoucherValidator {

    public void validate(CreateVoucherRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc.");
        }

        if (request.getDiscountType() == DiscountType.PERCENT) {
            if (request.getDiscountPercent() == null)
                throw new IllegalArgumentException("Voucher dạng phần trăm phải có discountPercent.");
            if (request.getDiscountPercent().compareTo(BigDecimal.ZERO) <= 0 ||
                request.getDiscountPercent().compareTo(BigDecimal.valueOf(100)) > 0)
                throw new IllegalArgumentException("discountPercent phải trong khoảng 0 - 100.");
        }

        if (request.getDiscountType() == DiscountType.AMOUNT) {
            if (request.getDiscountAmount() == null)
                throw new IllegalArgumentException("Voucher dạng số tiền phải có discountAmount.");
            if (request.getDiscountAmount().compareTo(BigDecimal.ZERO) <= 0)
                throw new IllegalArgumentException("discountAmount phải lớn hơn 0.");
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0)
            throw new IllegalArgumentException("Số lượng voucher phải > 0.");

        if (request.getMaxPerUser() == null || request.getMaxPerUser() <= 0)
            throw new IllegalArgumentException("Số lượt sử dụng mỗi user phải ≥ 1.");
    }
}
