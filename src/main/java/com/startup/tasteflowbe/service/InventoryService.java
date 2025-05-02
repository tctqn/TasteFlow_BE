package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    List<Inventory> getAllInventories();
    Optional<Inventory> getInventoryById(Long id);
    Inventory createInventory(Inventory inventory);
    Inventory updateInventory(Long id, Inventory inventory);
    void deleteInventory(Long id);

    // Add new product batch
    void updateInventoryForNewBatch(Long productId, Long warehouseId,Long batchId, int quantity);
}
