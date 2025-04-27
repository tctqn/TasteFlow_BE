package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Voucher;
import com.startup.tasteflowbe.repository.VoucherRepository;
import com.startup.tasteflowbe.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> getAllVouchers() {
        return voucherRepository.findAll();
    }

    @Override
    public Optional<Voucher> getVoucherById(Long id) {
        return voucherRepository.findById(id);
    }

    @Override
    public Voucher createVoucher(Voucher voucher) {
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher updateVoucher(Long id, Voucher voucher) {
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
    public void deleteVoucher(Long id) {
        voucherRepository.deleteById(id);
    }
}
