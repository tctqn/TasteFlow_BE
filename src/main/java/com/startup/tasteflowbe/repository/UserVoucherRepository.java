package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.UserVoucher;
import com.startup.tasteflowbe.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUser_UserId(Long userId);
    boolean existsByUserAndVoucher(User user, Voucher voucher);
    // Tổng lượt đã claim (dùng để so với voucher.quantity)
    @Query("SELECT COUNT(uv) FROM UserVoucher uv WHERE uv.voucher = :voucher")
    int countClaimed(@Param("voucher") Voucher voucher);

    // Số lần user đã dùng voucher (dùng để so với voucher.maxPerUser)
    @Query("SELECT COUNT(uv) FROM UserVoucher uv " +
            "WHERE uv.user = :user AND uv.voucher = :voucher AND uv.used = true")
    int countUsedByUserAndVoucher(@Param("user") User user,
                                  @Param("voucher") Voucher voucher);

    // Lấy 1 claim chưa dùng (nếu user đã claim trước đó)
    Optional<UserVoucher> findTopByUserAndVoucherAndUsedFalseOrderByClaimedAtAsc(User user, Voucher voucher);

    @Query("SELECT COUNT(uv) FROM UserVoucher uv " +
            "WHERE uv.user = :user AND uv.voucher = :voucher")
    int countClaimedByUser(@Param("user") User user,
                           @Param("voucher") Voucher voucher);
}
