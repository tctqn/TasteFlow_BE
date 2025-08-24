package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.model.StockMovement;
import com.startup.tasteflowbe.dto.response.StockMovementDTO;
import com.startup.tasteflowbe.dto.StoreTransferParam;
import com.startup.tasteflowbe.dto.request.StockMovementRequestDTO;
import com.startup.tasteflowbe.dto.request.DamageStockRequestDTO;
import com.startup.tasteflowbe.dto.response.ProductBatchResponseDTO;
import com.startup.tasteflowbe.dto.response.StockMovementResponseDTO;
import com.startup.tasteflowbe.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    public ResponseEntity<StockMovement> createStockMovement(
            @RequestBody StockMovementRequestDTO stockMovementRequestDTO) {
        StockMovement createdMovement = stockMovementService.createStockMovement(stockMovementRequestDTO);
        return ResponseEntity.ok(createdMovement);
    }

    @GetMapping("/{id}")
    public StockMovement getStockMovementById(@PathVariable("id") Long id) {
        return stockMovementService.getStockMovementById(id);
    }

    @GetMapping
    public List<StockMovementDTO> getAllStockMovements() {
        return stockMovementService.getAllStockMovements();
    }

    @PostMapping("/transfer-to-stores")
    public void transferToStores(
            @RequestParam Long requestId,
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestBody List<StoreTransferParam> transferList) {
        stockMovementService.transferToStores(requestId, warehouseId, productId, transferList);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<StockMovementResponseDTO>> getStockMovementsToStore(@PathVariable Long storeId) {
        List<StockMovementResponseDTO> dtoList = stockMovementService.getStockMovementsToStore(storeId)
                .stream()
                .map(this::convertToDto) // Sử dụng hàm chuyển đổi
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // Phương thức helper để chuyển đổi Entity sang DTO
    private StockMovementResponseDTO convertToDto(StockMovement movement) {
        if (movement == null) {
            return null;
        }

        StockMovementResponseDTO dto = new StockMovementResponseDTO();
        dto.setMovementId(movement.getMovementId());
        dto.setMovementType(movement.getMovementType());
        dto.setQuantity(movement.getQuantity());
        dto.setMovementDate(movement.getMovementDate());
        dto.setNote(movement.getNote());

        if (movement.getWarehouse() != null) {
            dto.setWarehouse(movement.getWarehouse());
        }

        if (movement.getStore() != null) {
            dto.setStoreId(movement.getStore().getStoreId());
        }

        if (movement.getBatch() != null) {
            ProductBatchResponseDTO batchDTO = new ProductBatchResponseDTO();
            ProductBatch productBatch = movement.getBatch();
            batchDTO.setBatchId(productBatch.getBatchId());
            batchDTO.setManufactureDate(productBatch.getManufactureDate());
            batchDTO.setExpirationDate(productBatch.getExpirationDate());
            batchDTO.setImportPrice(productBatch.getImportPrice());
            batchDTO.setQuantity(productBatch.getQuantity());
            dto.setProductBatchResponseDTO(batchDTO);
        }

        if (movement.getProduct() != null) {
            dto.setProductId(movement.getProduct().getProductId());
            dto.setProductName(movement.getProduct().getName());
        }

        if (movement.getStoreRequest() != null) {
            dto.setStoreRequestId(movement.getStoreRequest().getRequestId());
            dto.setStoreRequestStatus(movement.getStoreRequest().getStatus());
        }

        return dto;
    }

    @PostMapping("/damage")
    public ResponseEntity<String> damageStock(@RequestBody DamageStockRequestDTO dto) {
        String result = stockMovementService.damageStock(dto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/damaged-expired")
    public List<StockMovement> getDamagedAndExpired() {
        return stockMovementService.getDamagedAndExpired();
    }
}
