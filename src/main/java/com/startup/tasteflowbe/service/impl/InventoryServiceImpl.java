package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.StoreInventoryRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductInventoryDTO;
import com.startup.tasteflowbe.dto.response.ProductUnitStockDTO;
import com.startup.tasteflowbe.model.*;
import com.startup.tasteflowbe.repository.InventoryRepository;
import com.startup.tasteflowbe.repository.ProductRepository;
import com.startup.tasteflowbe.repository.ProductUnitRepository;
import com.startup.tasteflowbe.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    private final WarehouseService warehouseService;

    private final ProductBatchService productBatchService;

    private final StoreRequestService storeRequestService;

    private final StoreService storeService;
    private final ProductRepository productRepository;
    private final ProductUnitRepository productUnitRepository;

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
    public List<ProductInventoryDTO> getInventoryAllUnitByStore(Long storeId) {
        List<Inventory> inventories = inventoryRepository.findByStore_StoreId(storeId);

        Map<Long, Integer> productBaseQuantityMap = inventories.stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getProduct().getProductId(),
                        Collectors.summingInt(Inventory::getQuantity)
                ));

        List<ProductInventoryDTO> result = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : productBaseQuantityMap.entrySet()) {
            Long productId = entry.getKey();
            int baseQty = entry.getValue();

            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) continue;

            List<ProductUnit> units = productUnitRepository.findByProduct_ProductId(productId).get();

            List<ProductUnitStockDTO> unitStocks = units.stream().map(unit -> {
                int available = baseQty / unit.getConversionRate();
                return new ProductUnitStockDTO(
                        unit.getUnit().getName(),
                        unit.getConversionRate(),
                        available
                );
            }).toList();

            result.add(new ProductInventoryDTO(productId, product.getName(), unitStocks));
        }

        return result;
    }


    @Override
    public int getAvailableStock(Long storeId, Long productId, Long unitId) {
        return inventoryRepository.findAvailableQuantity(storeId, productId, unitId);
    }

    @Override
    public List<Inventory> findInventoriesByWarehouseId(Long warehouseId) {
        return inventoryRepository.findByWarehouse_WarehouseId(warehouseId);
    }
}
