package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.dto.request.StoreInventoryRequestDTO;
import com.startup.tasteflowbe.model.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    List<Inventory> getAllInventories();

    Optional<Inventory> getInventoryById(Long id);

    Inventory createInventory(Inventory inventory);

    Inventory updateInventory(Long id, Inventory inventory);

    void deleteInventory(Long id);

    List<Inventory> findByStore_StoreId(Long storeId);

    void createStoreInventory(StoreInventoryRequestDTO storeInventoryRequestDTO);
}
