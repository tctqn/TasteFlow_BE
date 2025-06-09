package com.startup.tasteflowbe.repository;

import com.startup.tasteflowbe.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Store findByStoreId(Long storeId);
}
