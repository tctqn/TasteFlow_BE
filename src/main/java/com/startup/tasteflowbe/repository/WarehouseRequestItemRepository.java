package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.WarehouseRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRequestItemRepository extends JpaRepository<WarehouseRequestItem, Integer> {
}