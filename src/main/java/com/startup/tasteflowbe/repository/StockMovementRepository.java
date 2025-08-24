package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.StockMovement;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByStore_StoreId(Long storeId);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementType IN :types")
    List<StockMovement> findMovementsByTypes(@Param("types") Collection<String> types);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementType IN :types AND store.storeId = :storeId")
    List<StockMovement> findMovementsByTypesInStore(@Param("types") Collection<String> types, Long storeId);
}
