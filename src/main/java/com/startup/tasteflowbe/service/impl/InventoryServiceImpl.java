package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.ProductRequestDTO;
import com.startup.tasteflowbe.dto.request.StoreInventoryRequestDTO;
import com.startup.tasteflowbe.dto.response.InventoriesResponseDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.model.Inventory;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.model.Store;
import com.startup.tasteflowbe.model.StoreRequest;
import com.startup.tasteflowbe.model.Warehouse;
import com.startup.tasteflowbe.repository.InventoryRepository;
import com.startup.tasteflowbe.repository.ProductBatchRepository;
import com.startup.tasteflowbe.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductBatchRepository productBatchRepository;

    private final InventoryRepository inventoryRepository;

    private final ProductService productService;

    private final WarehouseService warehouseService;

    private final ProductBatchService productBatchService;

    private final StoreRequestService storeRequestService;

    private final StoreService storeService;

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
    public void createStoreInventory(StoreInventoryRequestDTO storeInventoryRequestDTO) {
        System.out.print("Data here tuyennq223:" + storeInventoryRequestDTO);
        Inventory inventory = new Inventory();
        ProductBatch productBatch = productBatchService.getProductBatchById(storeInventoryRequestDTO.getBatchId())
                .orElseThrow(() -> new RuntimeException(
                        "ProductBatch not found with id " + storeInventoryRequestDTO.getBatchId()));
        Warehouse warehouse = warehouseService.getWarehouseById(storeInventoryRequestDTO.getWarehouseId())
                .orElseThrow(() -> new RuntimeException(
                        "Warehouse not found with id " + storeInventoryRequestDTO.getWarehouseId()));
        StoreRequest storeRequest = storeRequestService.getStoreRequestById(storeInventoryRequestDTO.getRequestId())
                .orElseThrow(() -> new RuntimeException(
                        "Store request not found with id " + storeInventoryRequestDTO.getRequestId()));
        Store store = storeService.getStoreById(storeInventoryRequestDTO.getStoreId())
                .orElseThrow(() -> new RuntimeException(
                        "Store not found with id " + storeInventoryRequestDTO.getStoreId()));

        inventory.setBatch(productBatch);
        inventory.setProduct(productBatch.getProduct());
        inventory.setQuantity(storeInventoryRequestDTO.getQuantity());
        inventory.setStore(store);
        inventory.setReorderLevel(storeInventoryRequestDTO.getReorder_level());
        inventory.setWarehouse(warehouse);

        inventoryRepository.save(inventory);

        storeRequestService.updateStoreRequestStatus(storeRequest.getRequestId(), storeInventoryRequestDTO.getStatus());
    }

    @Override
    public Inventory updateInventory(Long id, Inventory updatedInventory) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    inventory.setWarehouse(updatedInventory.getWarehouse());
                    inventory.setStore(updatedInventory.getStore());
                    inventory.setProduct(updatedInventory.getProduct());
                    inventory.setBatch(updatedInventory.getBatch());
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

    @Override
    public List<Inventory> findInventoriesByStoreId(Long storeId) {
        return inventoryRepository.findByStore_StoreId(storeId);
    }

    @Override
    public List<Inventory> findInventoriesByWarehouseId(Long warehouseId) {
        return inventoryRepository.findByWarehouse_WarehouseId(warehouseId);
    }
}
