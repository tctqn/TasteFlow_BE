package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Inventory;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.model.ProductUnit;
import com.startup.tasteflowbe.model.StockMovement;
import com.startup.tasteflowbe.model.Warehouse;
import com.startup.tasteflowbe.enums.MovementType;
import com.startup.tasteflowbe.repository.InventoryRepository;
import com.startup.tasteflowbe.repository.ProductBatchRepository;
import com.startup.tasteflowbe.repository.ProductUnitRepository;
import com.startup.tasteflowbe.repository.StockMovementRepository;
import com.startup.tasteflowbe.repository.WarehouseRepository;
import com.startup.tasteflowbe.service.ProductBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductBatchServiceImpl implements ProductBatchService {

    private final ProductBatchRepository productBatchRepository;

    private final InventoryRepository inventoryRepository;

    private final StockMovementRepository stockMovementRepository;

    private final ProductUnitRepository productUnitRepository;

    private final WarehouseRepository warehouseRepository;

    @Override
    public List<ProductBatch> getAllProductBatches() {
        return productBatchRepository.findAll();
    }

    @Override
    public Optional<ProductBatch> getProductBatchById(Long id) {
        return productBatchRepository.findById(id);
    }

    @Override
    public List<ProductBatch> getProductBatchByWarehouseId(Long id) {
        return productBatchRepository.findByWarehouseWarehouseId(id);
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
                    batch.setStatus(updatedProductBatch.getStatus());
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

    @Override
    @Transactional
    public void addNewBatch(ProductBatch productBatch) {
        // Lưu lô hàng mới vào bảng product_batches
        productBatchRepository.save(productBatch);

        // Quy đổi sang base unit
        ProductUnit productUnit = (ProductUnit) productUnitRepository
                .findByProduct_ProductIdAndUnit_UnitId(
                        productBatch.getProduct().getProductId(),
                        productBatch.getUnit().getUnitId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị quy đổi cho sản phẩm"));
        int quantityInBaseUnit = productBatch.getQuantity() * productUnit.getConversionRate();

        // Cập nhật bảng inventories
        Optional<Inventory> inventory = inventoryRepository
                .findByWarehouse_WarehouseIdAndProduct_ProductIdAndBatch_BatchId(
                        productBatch.getWarehouse().getWarehouseId(),
                        productBatch.getProduct().getProductId(),
                        productBatch.getBatchId());
        if (inventory.isPresent()) {
            // Cập nhật số lượng tồn kho
            Inventory existingInventory = inventory.get();
            existingInventory.setQuantity(existingInventory.getQuantity() + quantityInBaseUnit);
            inventoryRepository.save(existingInventory);
        } else {
            // Tạo mới bản ghi tồn kho nếu chưa có
            Inventory newInventory = new Inventory();
            newInventory.setProduct(productBatch.getProduct());
            newInventory.setWarehouse(productBatch.getWarehouse());
            newInventory.setBatch(productBatch);
            newInventory.setQuantity(quantityInBaseUnit);
            newInventory.setReorderLevel(10); // Mức cảnh báo tái nhập kho mặc định
            inventoryRepository.save(newInventory);
        }

        // Ghi nhận chuyển động hàng hóa vào bảng stock_movements
        StockMovement stockMovement = new StockMovement();
        stockMovement.setWarehouse(productBatch.getWarehouse());
        stockMovement.setProduct(productBatch.getProduct());
        stockMovement.setQuantity(quantityInBaseUnit);
        stockMovement.setBatch(productBatch);
        stockMovement.setMovementDate(LocalDateTime.now());
        stockMovement.setNote(MovementType.IMPORT_BATCH.getDescription());
        stockMovement.setMovementType(MovementType.IMPORT_BATCH);
        stockMovementRepository.save(stockMovement);
    }
}
