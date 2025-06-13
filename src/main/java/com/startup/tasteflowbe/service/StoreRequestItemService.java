package com.startup.tasteflowbe.service;

import java.util.List;

import com.startup.tasteflowbe.model.StoreRequestItem;

public interface StoreRequestItemService {
    List<StoreRequestItem> getStoreRequestItemsByRequest(Long requestId);
}
