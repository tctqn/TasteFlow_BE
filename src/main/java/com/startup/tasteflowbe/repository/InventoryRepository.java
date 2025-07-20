package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Inventory;
import com.startup.tasteflowbe.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByWarehouse_WarehouseIdAndProduct_ProductIdAndBatch_BatchId(Long warehouseWarehouseId,
                                                                                    Long productProductId, Long batchBatchId);

    Optional<Inventory> findByStore_StoreIdAndProduct_ProductIdAndBatch_BatchId(Long storeId, Long productId,
                                                                                Long batchBatchId);

    List<Inventory> findByStore_StoreIdAndProduct_ProductIdAndQuantityGreaterThanAndBatch_ExpirationDateAfterOrderByBatch_ExpirationDateAsc(
            Long storeId, Long productId, int quantity, LocalDate now);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Inventory i WHERE i.warehouse.warehouseId = :warehouseId")
    Optional<Integer> getTotalProductByWarehouseId(@Param("warehouseId") Long warehouseId);

    List<Inventory> findByStore_StoreId(Long storeId);

    @Query("""
                SELECT COALESCE(SUM(i.quantity), 0)
                FROM Inventory i
                WHERE i.store.storeId = :storeId
                  AND i.product.productId = :productId
                  AND i.batch.unit.unitId = :unitId
            """)
    int findAvailableQuantity(@Param("storeId") Long storeId,
                              @Param("productId") Long productId,
                              @Param("unitId") Long unitId);

    @Query("SELECT i FROM Inventory i WHERE i.warehouse.warehouseId = :warehouseId")
    List<Inventory> findByWarehouse_WarehouseId(Long warehouseId);

    // Lấy danh sách sản phẩm duy nhất trong kho hàng theo warehouseId
    @Query("SELECT DISTINCT i.product FROM Inventory i WHERE i.warehouse.warehouseId = :warehouseId")
    List<Product> findDistinctProductsByWarehouseId(@Param("warehouseId") Long warehouseId);

    // Tìm inventory theo productId và warehouseId hoặc storeId
    @Query("""
        SELECT i FROM Inventory i
        WHERE i.product.productId = :productId
        AND ((:warehouseId IS NOT NULL AND i.warehouse.warehouseId = :warehouseId) 
             OR (:storeId IS NOT NULL AND i.store.storeId = :storeId))
    """)
    List<Inventory> findByProductAndWarehouseOrStore(@Param("productId") Long productId,
                                                     @Param("warehouseId") Long warehouseId,
                                                     @Param("storeId") Long storeId);
}
