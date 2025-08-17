package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.WarehouseRequest;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRequestRepository extends JpaRepository<WarehouseRequest, Integer> {
    @EntityGraph(attributePaths = { "items", "warehouse", "warehouse.manager" })
    @Query("SELECT wr FROM WarehouseRequest wr WHERE wr.requestId = :requestId")
    Optional<WarehouseRequest> findByIdWithItemsAndWarehouse(@Param("requestId") Integer requestId);
}
