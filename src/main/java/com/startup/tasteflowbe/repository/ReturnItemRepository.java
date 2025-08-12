package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ProductUnit;
import com.startup.tasteflowbe.model.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
}
