package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.User;
import com.startup.tasteflowbe.model.UserVoucher;
import com.startup.tasteflowbe.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    boolean existsByUserAndVoucher(User user, Voucher voucher);

}
