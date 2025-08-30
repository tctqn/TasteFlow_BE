package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    @Query("""
        SELECT DISTINCT p
        FROM Promotion p
        LEFT JOIN p.applicableStores s
        WHERE p.isActive = true
          AND p.startDate <= :now
          AND p.endDate   >= :now
          AND (s.storeId = :storeId OR s IS NULL)
    """)
    List<Promotion> findActiveForStore(@Param("storeId") Long storeId,
                                       @Param("now") LocalDateTime now);
}
