package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.StoreRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRequestItemRepository extends JpaRepository<StoreRequestItem, Integer> {

    List<StoreRequestItem> findByStoreRequest_RequestId(Long requestId);

    StoreRequestItem findByStoreRequest_RequestIdAndProductId(Long requestId, Long productId);
}
