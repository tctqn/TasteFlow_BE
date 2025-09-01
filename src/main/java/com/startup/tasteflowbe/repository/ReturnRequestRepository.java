package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.ProductUnit;
import com.startup.tasteflowbe.model.ReturnRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findAll();

    List<ReturnRequest> findByOriginalOrderCodeIgnoreCase(String originalOrderCode);

    List<ReturnRequest> findByOriginalOrderCode(String orderCode);
}
