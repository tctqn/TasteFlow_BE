package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.UserVoucher;
import com.startup.tasteflowbe.model.Voucher;
import com.startup.tasteflowbe.repository.UserRepository;
import com.startup.tasteflowbe.repository.UserVoucherRepository;
import com.startup.tasteflowbe.repository.VoucherRepository;
import com.startup.tasteflowbe.service.UserVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserVoucherServiceImpl implements UserVoucherService {

    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Override
    @Transactional
    public UserVoucher claimVoucher(Long userId, Long voucherId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
        Voucher voucher = voucherRepository.findById(voucherId).orElseThrow(() -> new IllegalStateException("Voucher not found"));

        if (voucher.getClaimedCount() >= voucher.getQuantity()) {
            throw new IllegalStateException("Voucher has been fully claimed");
        }

        boolean alreadyClaimed = userVoucherRepository.existsByUserAndVoucher(user, voucher);
        if (alreadyClaimed) {
            throw new IllegalStateException("User has already claimed this voucher");
        }

        voucher.setClaimedCount(voucher.getClaimedCount() + 1);
        voucherRepository.save(voucher);

        UserVoucher userVoucher = new UserVoucher();
        userVoucher.setUser(user);
        userVoucher.setVoucher(voucher);
        userVoucher.setClaimedAt(LocalDateTime.now());
        userVoucher.setUsed(false);
        return userVoucherRepository.save(userVoucher);
    }

    @Override
    @Transactional
    public UserVoucher useVoucher(Long userVoucherId) {
        UserVoucher uv = userVoucherRepository.findById(userVoucherId)
                .orElseThrow(() -> new IllegalStateException("User voucher not found"));

        if (uv.isUsed()) {
            throw new IllegalStateException("Voucher already used");
        }

        uv.setUsed(true);
        return userVoucherRepository.save(uv);
    }
}
