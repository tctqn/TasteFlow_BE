package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ProductBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {
    List<ProductBatch> findByWarehouseWarehouseId(Long idCuaWarehouse);
}
