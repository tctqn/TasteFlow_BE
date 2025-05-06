package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.repository.ProductBatchRepository;
import com.startup.tasteflowbe.service.ProductBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductBatchServiceImpl implements ProductBatchService {

    private final ProductBatchRepository productBatchRepository;

    @Override
    public List<ProductBatch> getAllProductBatches() {
        return productBatchRepository.findAll();
    }

    @Override
    public Optional<ProductBatch> getProductBatchById(Long id) {
        return productBatchRepository.findById(id);
    }

    @Override
    public ProductBatch createProductBatch(ProductBatch productBatch) {
        return productBatchRepository.save(productBatch);
    }

    @Override
    public ProductBatch updateProductBatch(Long id, ProductBatch updatedProductBatch) {
        return productBatchRepository.findById(id)
                .map(batch -> {
                    batch.setProduct(updatedProductBatch.getProduct());
                    batch.setWarehouse(updatedProductBatch.getWarehouse());
                    batch.setSupplier(updatedProductBatch.getSupplier());
                    batch.setUnit(updatedProductBatch.getUnit());
                    batch.setQuantity(updatedProductBatch.getQuantity());
                    batch.setManufactureDate(updatedProductBatch.getManufactureDate());
                    batch.setExpirationDate(updatedProductBatch.getExpirationDate());
                    batch.setReceivedDate(updatedProductBatch.getReceivedDate());
                    batch.setNote(updatedProductBatch.getNote());
                    batch.setImportPrice(updatedProductBatch.getImportPrice());
                    return productBatchRepository.save(batch);
                })
                .orElseThrow(() -> new RuntimeException("ProductBatch not found with id " + id));
    }

    @Override
    public void deleteProductBatch(Long id) {
        productBatchRepository.deleteById(id);
    }
}
