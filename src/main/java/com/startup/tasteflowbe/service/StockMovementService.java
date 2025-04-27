package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.StockMovement;

import java.util.List;

public interface StockMovementService {
    StockMovement createStockMovement(StockMovement stockMovement);
    StockMovement getStockMovementById(Long movementId);
    List<StockMovement> getAllStockMovements();
}
