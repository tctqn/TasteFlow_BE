package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.StoreRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRequestRepository extends JpaRepository<StoreRequest, Long> {

    List<StoreRequest> findByStatus(String status);

    @Query("SELECT sr FROM StoreRequest sr WHERE sr.storeId = :storeId")
    List<StoreRequest> findByStoreId(Long storeId);

    @Query("SELECT sr FROM StoreRequest sr WHERE sr.warehouseId = :warehouseId")
    List<StoreRequest> findByWarehouseId(Long warehouseId);

}
