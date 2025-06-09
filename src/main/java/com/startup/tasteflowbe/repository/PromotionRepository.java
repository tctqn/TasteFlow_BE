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
    @Query("SELECT p FROM Promotion p JOIN p.products prod " +
            "WHERE prod.productId = :productId " +
            "AND :now BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotionsByProductId(@Param("productId") Long productId,
                                                    @Param("now") LocalDateTime now);
}
