package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.StockMovement;
import com.startup.tasteflowbe.dto.StoreTransferParam;

import java.util.List;

public interface StockMovementService {
    StockMovement createStockMovement(StockMovement stockMovement);

    StockMovement getStockMovementById(Long movementId);

    List<StockMovement> getAllStockMovements();

    void transferToStores(Long requestId, Long warehouseId, Long productId, Long batchId,
            List<StoreTransferParam> transferList);

    List<StockMovement> getStockMovementsToStore(Long storeId);
}
