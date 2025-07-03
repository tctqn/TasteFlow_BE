package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.ProductUnit;
import com.startup.tasteflowbe.repository.ProductUnitRepository;
import com.startup.tasteflowbe.service.ProductUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class
ProductUnitServiceImpl implements ProductUnitService {

    private final ProductUnitRepository productUnitRepository;

    @Override
    public List<ProductUnit> getAllProductUnits() {
        return productUnitRepository.findAll();
    }

    @Override
    public Optional<ProductUnit> getProductUnitById(Long id) {
        return productUnitRepository.findById(id);
    }

    @Override
    public ProductUnit createProductUnit(ProductUnit productUnit) {
        return productUnitRepository.save(productUnit);
    }

    @Override
    public ProductUnit updateProductUnit(Long id, ProductUnit updatedProductUnit) {
        return productUnitRepository.findById(id)
                .map(productUnit -> {
                    productUnit.setConversionRate(updatedProductUnit.getConversionRate());
                    productUnit.setIsBaseUnit(updatedProductUnit.getIsBaseUnit());
                    return productUnitRepository.save(productUnit);
                })
                .orElseThrow(() -> new RuntimeException("ProductUnit not found with id " + id));
    }

    @Override
    public void deleteProductUnit(Long id) {
        productUnitRepository.deleteById(id);
    }

    public Long getUnitIdByProductUnitId(Long productUnitId) {
        ProductUnit pu = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found with id " + productUnitId));;
        return pu.getUnit().getUnitId();
    }

    @Override
    public Optional<Object> findByProduct_ProductIdAndUnit_UnitIdAndIsBaseUnit(Long productProductId, Long unitUnitId, Boolean isBaseUnit) {
        return productUnitRepository.findByProduct_ProductIdAndUnit_UnitIdAndIsBaseUnit(productProductId, unitUnitId, isBaseUnit);
    }
}
