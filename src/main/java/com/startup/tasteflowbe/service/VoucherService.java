package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.Voucher;

import java.util.List;
import java.util.Optional;

public interface VoucherService {
    List<Voucher> getAllVouchers();
    Optional<Voucher> getVoucherById(Long id);
    Voucher createVoucher(Voucher voucher);
    Voucher updateVoucher(Long id, Voucher voucher);
    void deleteVoucher(Long id);
}
