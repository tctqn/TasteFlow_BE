package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByWarehouse_WarehouseIdAndProduct_ProductIdAndBatch_BatchId(Long warehouseWarehouseId, Long productProductId, Long batchBatchId);
    Optional<Inventory> findByStore_StoreIdAndProduct_ProductIdAndBatch_BatchId(Long storeId, Long productId, Long batchBatchId);
}

