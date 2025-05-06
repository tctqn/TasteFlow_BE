package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.ProductUnit;

import java.util.List;
import java.util.Optional;

public interface ProductUnitService {
    List<ProductUnit> getAllProductUnits();
    Optional<ProductUnit> getProductUnitById(Long id);
    ProductUnit createProductUnit(ProductUnit productUnit);
    ProductUnit updateProductUnit(Long id, ProductUnit productUnit);
    void deleteProductUnit(Long id);
}
