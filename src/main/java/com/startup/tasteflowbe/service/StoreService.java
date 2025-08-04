package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.Store;

import java.util.List;
import java.util.Optional;

public interface StoreService {
    List<Store> getAllStores();

    Optional<Store> getStoreById(Long id);

    Store createStore(Store store);

    Store updateStore(Long id, Store store);

    void deleteStore(Long id);

    Optional<Store> getStoreByManager(Long managerId);

    Optional<Store> getStoreByStaff(Long staffId);
}
