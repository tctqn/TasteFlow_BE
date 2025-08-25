package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.enums.DistributionType;
import com.startup.tasteflowbe.model.Voucher;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    List<Voucher> findByVoucherIdIn(List<Long> voucherIds);
    List<Voucher> findAllByDistributionTypeAndStartDateBeforeAndEndDateAfterAndQuantityGreaterThan(
            DistributionType distributionType,
            LocalDateTime start,
            LocalDateTime end,
            int minQuantity);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT v FROM Voucher v WHERE v.voucherId = :id")
    Optional<Voucher> findByIdForUpdate(@Param("id") Long id);
}
