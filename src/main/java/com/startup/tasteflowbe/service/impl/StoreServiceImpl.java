package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.model.Store;
import com.startup.tasteflowbe.repository.StoreRepository;
import com.startup.tasteflowbe.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;

    @Override
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    @Override
    public Optional<Store> getStoreById(Long id) {
        return storeRepository.findById(id);
    }

    @Override
    public Optional<Store> getStoreByManager(Long managerId) {
        return storeRepository.findByManagerId(managerId);
    }

    @Override
    public Store createStore(Store store) {
        return storeRepository.save(store);
    }

    @Override
    public Store updateStore(Long id, Store updatedStore) {
        return storeRepository.findById(id)
                .map(store -> {
                    store.setName(updatedStore.getName());
                    store.setAddress(updatedStore.getAddress());
                    store.setContactInfo(updatedStore.getContactInfo());
                    store.setBusinessHours(updatedStore.getBusinessHours());
                    return storeRepository.save(store);
                })
                .orElseThrow(() -> new RuntimeException("Store not found with id " + id));
    }

    @Override
    public void deleteStore(Long id) {
        storeRepository.deleteById(id);
    }
}
