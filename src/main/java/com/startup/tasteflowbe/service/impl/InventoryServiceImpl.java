package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Inventory;
import com.startup.tasteflowbe.repository.InventoryRepository;
import com.startup.tasteflowbe.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public List<Inventory> getAllInventories() {
        return inventoryRepository.findAll();
    }

    @Override
    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id);
    }

    @Override
    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public Inventory updateInventory(Long id, Inventory updatedInventory) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    inventory.setWarehouse(updatedInventory.getWarehouse());
                    inventory.setStore(updatedInventory.getStore());
                    inventory.setProduct(updatedInventory.getProduct());
                    inventory.setQuantity(updatedInventory.getQuantity());
                    inventory.setReorderLevel(updatedInventory.getReorderLevel());
                    return inventoryRepository.save(inventory);
                })
                .orElseThrow(() -> new RuntimeException("Inventory not found with id " + id));
    }

    @Override
    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }
}
