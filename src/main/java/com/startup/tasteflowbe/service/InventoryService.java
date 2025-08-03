package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.InventoryRequestDTO;
import com.startup.tasteflowbe.dto.request.StoreInventoryRequestDTO;
import com.startup.tasteflowbe.dto.response.BatchDetailDTO;
import com.startup.tasteflowbe.dto.response.ProductInventoryDTO;
import com.startup.tasteflowbe.dto.response.StoreProductDTO;
import com.startup.tasteflowbe.dto.response.WarehouseProductDTO;
import com.startup.tasteflowbe.model.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    List<Inventory> getAllInventories();

    Optional<Inventory> getInventoryById(Long id);

    List<ProductInventoryDTO> getInventoryAllUnitByStore(Long storeId);

    Inventory createInventory(InventoryRequestDTO inventoryRequestDTO);

    Inventory updateInventory(Long id, Inventory inventory);

    void deleteInventory(Long id);

    List<Inventory> findInventoriesByStoreId(Long storeId);

    List<Inventory> findInventoriesByWarehouseId(Long warehouseId);

    void createStoreInventory(StoreInventoryRequestDTO storeInventoryRequestDTO);

    int getAvailableStock(Long storeId, Long productUnitId, Long batchId);

    // Lấy danh sách sản phẩm duy nhất trong kho hàng theo warehouseId (WarehouseProductDTO)
    List<WarehouseProductDTO> getWarehouseProductsByWarehouseId(Long warehouseId);

    // Lấy chi tiết các batch của 1 product theo warehouseId hoặc storeId
    List<BatchDetailDTO> getBatchDetailsByProductAndWarehouseOrStore(Long productId, Long warehouseId, Long storeId);

    // Lấy danh sách sản phẩm duy nhất trong cửa hàng theo storeId (StoreProductDTO)
    List<StoreProductDTO> getStoreProductsByStoreId(Long storeId);

    int updateReorderLevel(Long productId, Long warehouseId, Long storeId, Integer reorderLevel);
}
