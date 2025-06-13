package com.startup.tasteflowbe.service;

import com.startup.tasteflowbe.model.StoreRequest;
import com.startup.tasteflowbe.dto.request.StoreRequestDTO;
import java.util.List;
import java.util.Optional;

public interface StoreRequestService {

    List<StoreRequest> getAllStoreRequests();

    Optional<StoreRequest> getStoreRequestById(Long requestId);

    StoreRequest createStoreRequest(StoreRequestDTO requestDTO);

    StoreRequest updateStoreRequest(Long requestId, StoreRequest requestDetails);

    StoreRequest updateStoreRequestStatus(Long requestId, String status);

    void deleteStoreRequest(Long requestId);

    List<StoreRequest> getStoreRequestByStore(Long storeId);

}
