package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {
}
