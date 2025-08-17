package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.StockMovement;
import com.startup.tasteflowbe.dto.response.StockMovementDTO;
import com.startup.tasteflowbe.dto.StoreTransferParam;
import com.startup.tasteflowbe.dto.request.StockMovementRequestDTO;

import java.util.List;

public interface StockMovementService {
    StockMovement createStockMovement(StockMovementRequestDTO stockMovementRequestDTO);

    StockMovement getStockMovementById(Long movementId);

    List<StockMovementDTO> getAllStockMovements();

    void transferToStores(Long requestId, Long warehouseId, Long productId, Long batchId,
            List<StoreTransferParam> transferList);

    List<StockMovement> getStockMovementsToStore(Long storeId);
}
