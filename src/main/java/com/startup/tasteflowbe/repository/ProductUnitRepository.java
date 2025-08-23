package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
