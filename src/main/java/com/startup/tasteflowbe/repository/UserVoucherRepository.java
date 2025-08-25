package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.UserVoucher;
import com.startup.tasteflowbe.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUser_UserId(Long userId);
    Optional<UserVoucher> findByUser_UserIdAndVoucher_VoucherId(Long userId, Long voucherId);
    boolean existsByUserAndVoucher(User user, Voucher voucher);
    long countByUserAndVoucher(User user, Voucher voucher); // tổng claim (dù used true/false)
    long countByUserAndVoucherAndUsedTrue(User user, Voucher voucher);
}
