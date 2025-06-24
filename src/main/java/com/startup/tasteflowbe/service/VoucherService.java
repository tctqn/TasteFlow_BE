package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.CreateVoucherRequest;
import com.startup.tasteflowbe.dto.response.VoucherResponseDTO;
import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.Voucher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VoucherService {
    List<VoucherResponseDTO> getPublicVouchers(BigDecimal totalPrice);
    List<Voucher> getAllVouchers();
    Optional<Voucher> getVoucherById(Long id);
    Voucher createVoucher(CreateVoucherRequest request);
    Voucher updateVoucher(Long id, Voucher voucher);
    void deleteVoucher(Long id);
    List<VoucherResponseDTO> getAvailableVouchers(User user, BigDecimal totalPrice);
}
