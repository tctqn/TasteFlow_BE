package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.StockMovement;
import com.startup.tasteflowbe.repository.StockMovementRepository;
import com.startup.tasteflowbe.service.StockMovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StockMovementServiceImpl implements StockMovementService {

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Override
    public StockMovement createStockMovement(StockMovement stockMovement) {
        return stockMovementRepository.save(stockMovement);
    }

    @Override
    public StockMovement getStockMovementById(Long movementId) {
        Optional<StockMovement> stockMovement = stockMovementRepository.findById(movementId);
        return stockMovement.orElse(null);
    }

    @Override
    public List<StockMovement> getAllStockMovements() {
        return stockMovementRepository.findAll();
    }
}
