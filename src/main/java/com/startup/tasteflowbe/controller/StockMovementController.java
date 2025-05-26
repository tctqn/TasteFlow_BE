package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.model.StockMovement;
import com.startup.tasteflowbe.dto.StoreTransferParam;
import com.startup.tasteflowbe.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stock-movements")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @PostMapping
    public StockMovement createStockMovement(@RequestBody StockMovement stockMovement) {
        return stockMovementService.createStockMovement(stockMovement);
    }

    @GetMapping("/{id}")
    public StockMovement getStockMovementById(@PathVariable("id") Long id) {
        return stockMovementService.getStockMovementById(id);
    }

    @GetMapping
    public List<StockMovement> getAllStockMovements() {
        return stockMovementService.getAllStockMovements();
    }

    @PostMapping("/transfer-to-stores")
    public void transferToStores(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestParam Long batchId,
            @RequestBody List<StoreTransferParam> transferList) {
        stockMovementService.transferToStores(warehouseId, productId, batchId, transferList);
    }

}
