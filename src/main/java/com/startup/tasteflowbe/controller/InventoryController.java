package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.StoreInventoryRequestDTO;
import com.startup.tasteflowbe.dto.response.InventoriesResponseDTO;
import com.startup.tasteflowbe.dto.response.ProductBatchResponseDTO;
import com.startup.tasteflowbe.dto.response.ProductResponseDTO;
import com.startup.tasteflowbe.model.Inventory;
import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.model.Store;
import com.startup.tasteflowbe.model.Warehouse;
import com.startup.tasteflowbe.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventories() {
        return ResponseEntity.ok(inventoryService.getAllInventories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Inventory> getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        return ResponseEntity.ok(inventoryService.createInventory(inventory));
    }

    @PostMapping("/store-import")
    public void createStoreInventory(@RequestBody StoreInventoryRequestDTO storeInventoryRequestDTO) {
        inventoryService.createStoreInventory(storeInventoryRequestDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, inventory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/store/{store_id}")
    public ResponseEntity<List<InventoriesResponseDTO>> getInventoryOfStore(@PathVariable Long store_id) {
        List<InventoriesResponseDTO> dtoList = inventoryService.findInventoriesByStoreId(store_id)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    private InventoriesResponseDTO convertToDto(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoriesResponseDTO dto = new InventoriesResponseDTO();
        dto.setInventoryId(inventory.getInventoryId());
        dto.setQuantity(inventory.getQuantity());
        dto.setReorderLevel(inventory.getReorderLevel());

        if (inventory.getWarehouse() != null) {
            dto.setWarehouseId(inventory.getWarehouse());
        }

        if (inventory.getStore() != null) {
            dto.setStoreId(inventory.getStore().getStoreId());
        }

        if (inventory.getBatch() != null) {
            ProductBatchResponseDTO batchDTO = new ProductBatchResponseDTO();
            ProductBatch productBatch = inventory.getBatch();
            batchDTO.setBatchId(productBatch.getBatchId());
            batchDTO.setManufactureDate(productBatch.getManufactureDate());
            batchDTO.setExpirationDate(productBatch.getExpirationDate());
            batchDTO.setImportPrice(productBatch.getImportPrice());
            batchDTO.setQuantity(productBatch.getQuantity());
            batchDTO.setSupplierId(productBatch.getSupplier());
            batchDTO.setReceivedDate(productBatch.getReceivedDate());
            dto.setBatchId(batchDTO);
        }
        if (inventory.getProduct() != null) {
            ProductResponseDTO productDto = new ProductResponseDTO();
            Product product = inventory.getProduct();
            productDto.setProductId(product.getProductId());
            productDto.setName(product.getName());
            productDto.setPrice(product.getPrice());
            productDto.setSku(product.getSku());
            productDto.setImageUrl(product.getImageUrl());
            productDto.setCategoryName(product.getCategory().getName());
            dto.setProduct(productDto);
        }

        return dto;
    }

}
