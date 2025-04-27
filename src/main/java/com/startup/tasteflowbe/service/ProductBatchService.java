package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.ProductBatch;

import java.util.List;
import java.util.Optional;

public interface ProductBatchService {
    List<ProductBatch> getAllProductBatches();
    Optional<ProductBatch> getProductBatchById(Long id);
    ProductBatch createProductBatch(ProductBatch productBatch);
    ProductBatch updateProductBatch(Long id, ProductBatch productBatch);
    void deleteProductBatch(Long id);
}
