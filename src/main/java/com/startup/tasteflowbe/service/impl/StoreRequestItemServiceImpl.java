package com.startup.tasteflowbe.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.startup.tasteflowbe.model.StoreRequestItem;
import com.startup.tasteflowbe.repository.StoreRequestItemRepository;
import com.startup.tasteflowbe.service.StoreRequestItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreRequestItemServiceImpl implements StoreRequestItemService {

    private final StoreRequestItemRepository storeRequestItemRepository;

    @Override
    public List<StoreRequestItem> getStoreRequestItemsByRequest(Long requestId) {
        return storeRequestItemRepository.findByStoreRequest_RequestId(requestId);
    }
}
