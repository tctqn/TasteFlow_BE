package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    @Query("SELECT p FROM Promotion p JOIN p.applicableProducts ap JOIN p.applicableStores s " +
            "WHERE ap.productId = :productId AND s.storeId = :storeId " +
            "AND p.startDate <= CURRENT_TIMESTAMP AND p.endDate >= CURRENT_TIMESTAMP")
    List<Promotion> findValidPromotionsForProductAtStore(@Param("productId") Long productId,
                                                         @Param("storeId") Long storeId);
}
