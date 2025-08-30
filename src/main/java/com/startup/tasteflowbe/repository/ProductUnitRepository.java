package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
    Optional<Object> findByProduct_ProductIdAndUnit_UnitIdAndIsBaseUnit(Long productProductId, Long unitUnitId,
            Boolean isBaseUnit);

    Optional<List<ProductUnit>> findByProduct_ProductId(Long productId);

    Optional<Object> findByProduct_ProductIdAndUnit_UnitId(Long productProductId, Long unitUnitId);

    Optional<ProductUnit> findBySku(String sku);

    ProductUnit findByProductUnitId(Long productUnitId);

    @Query("""
        SELECT DISTINCT pu
        FROM ProductUnit pu
        JOIN pu.product p
        WHERE pu.isBaseUnit = true
          AND EXISTS (
                SELECT 1
                FROM Inventory i
                WHERE i.product = p
                  AND i.store.storeId = :storeId
          )
    """)
    List<ProductUnit> findBaseUnitsByStoreIdViaInventory(@Param("storeId") Long storeId);


    @Query("""
        select pu
        from ProductUnit pu
        join fetch pu.product p
        left join fetch p.category c
        where pu.isBaseUnit = true
    """)
    List<ProductUnit> findAllBaseUnitsWithProduct();


}
