package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.WarehouseRequestItem;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRequestItemRepository extends JpaRepository<WarehouseRequestItem, Integer> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE WarehouseRequestItem wri " +
            "SET wri.status = :status, wri.fulfilledQuantity = :fulfilledQuantity " +
            "WHERE wri.requestItemId = :itemId")
    int updateOneItem(@Param("itemId") Integer itemId,
            @Param("fulfilledQuantity") Integer fulfilledQuantity,
            @Param("status") String status);

}