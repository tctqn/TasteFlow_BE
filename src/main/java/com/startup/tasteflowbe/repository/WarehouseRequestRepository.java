package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.WarehouseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRequestRepository extends JpaRepository<WarehouseRequest, Integer> {
}
