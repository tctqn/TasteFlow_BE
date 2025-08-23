package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Supplier findBySupplierId(Long supplierId);
}
