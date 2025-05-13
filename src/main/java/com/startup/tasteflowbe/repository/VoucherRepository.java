package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByVoucherIdIn(List<Long> voucherIds);
}
