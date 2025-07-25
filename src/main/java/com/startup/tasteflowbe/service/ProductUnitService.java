package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.response.ProductUnitDTO;
import com.startup.tasteflowbe.model.ProductUnit;

import java.util.List;
import java.util.Optional;

public interface ProductUnitService {
    List<ProductUnit> getAllProductUnits();

    Optional<ProductUnit> getProductUnitById(Long id);

    ProductUnit createProductUnit(ProductUnitDTO productUnitDTO);

    ProductUnit updateProductUnit(Long id, ProductUnit productUnit);

    void deleteProductUnit(Long id);

    Long getUnitIdByProductUnitId(Long productUnitId);

    Optional<Object> findByProduct_ProductIdAndUnit_UnitIdAndIsBaseUnit(Long productProductId, Long unitUnitId,
            Boolean isBaseUnit);
}
