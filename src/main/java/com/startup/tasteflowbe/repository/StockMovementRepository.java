package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.StockMovement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByStore_StoreId(Long storeId);
}
