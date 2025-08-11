package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
  Store findByStoreId(Long storeId);

  Optional<Store> findByManager_UserId(Long managerId);

  @Query(value = """
          SELECT p.name AS productName,
                 SUM(oi.quantity) AS totalSold
          FROM order_items oi
          JOIN products p ON p.product_id = oi.product_id
          JOIN orders o ON o.order_id = oi.order_id
          WHERE o.store_id = :storeId
            AND EXTRACT(YEAR FROM o.order_date) BETWEEN (EXTRACT(YEAR FROM CURRENT_DATE) - :yearsAgo + 1)
                                                    AND EXTRACT(YEAR FROM CURRENT_DATE)
            AND CASE
                  WHEN EXTRACT(MONTH FROM o.order_date) IN (10, 11, 12) THEN 'Winter'
                  WHEN EXTRACT(MONTH FROM o.order_date) IN (1, 2, 3) THEN 'Spring'
                  WHEN EXTRACT(MONTH FROM o.order_date) IN (4, 5, 6) THEN 'Summer'
                  WHEN EXTRACT(MONTH FROM o.order_date) IN (7, 8, 9) THEN 'Autumn'
                END = :season
          GROUP BY p.name
          ORDER BY totalSold DESC
          LIMIT 5
      """, nativeQuery = true)
  List<Map<String, Object>> findTopSellingProductsInSeason(
      @Param("storeId") Long storeId,
      @Param("yearsAgo") int yearsAgo,
      @Param("season") String season);

}
