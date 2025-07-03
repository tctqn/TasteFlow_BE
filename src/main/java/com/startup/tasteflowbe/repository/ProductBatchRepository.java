package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Product;
import com.startup.tasteflowbe.model.ProductBatch;
import com.startup.tasteflowbe.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {
    List<ProductBatch> findByWarehouseWarehouseId(Long idCuaWarehouse);

    Optional<ProductBatch> findTopByProductOrderByReceivedDateDesc(Product product);

    Optional<ProductBatch> findByRequestItem_RequestItemId(Integer requestItemId);
}
