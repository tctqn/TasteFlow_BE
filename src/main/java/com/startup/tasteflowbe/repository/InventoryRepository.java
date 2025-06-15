package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    Optional<Inventory> findByWarehouse_WarehouseIdAndProduct_ProductIdAndBatch_BatchId(Long warehouseWarehouseId,
            Long productProductId, Long batchBatchId);

    Optional<Inventory> findByStore_StoreIdAndProduct_ProductIdAndBatch_BatchId(Long storeId, Long productId,
            Long batchBatchId);

    List<Inventory> findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
            Long storeId, Long productId, int quantity, LocalDate now);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.warehouse.warehouseId = :warehouseId")
    Optional<Integer> getTotalProductByWarehouseId(@Param("warehouseId") Long warehouseId);

    List<Inventory> findByStore_StoreId(Long storeId);
}
