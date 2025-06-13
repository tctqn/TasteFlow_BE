package com.startup.tasteflowbe.service.impl;

import com.startup.tasteflowbe.dto.request.StoreRequestDTO;
import com.startup.tasteflowbe.model.StoreRequest;
import com.startup.tasteflowbe.model.StoreRequestItem;
import com.startup.tasteflowbe.repository.StoreRequestRepository;
import com.startup.tasteflowbe.service.StoreRequestService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreRequestServiceImpl implements StoreRequestService {

    private final StoreRequestRepository storeRequestRepository;

    @Override
    public List<StoreRequest> getAllStoreRequests() {
        return storeRequestRepository.findAll();
    }

    @Override
    public Optional<StoreRequest> getStoreRequestById(Long requestId) {
        return storeRequestRepository.findById(requestId);
    }

    @Override
    @Transactional
    public StoreRequest createStoreRequest(StoreRequestDTO requestDTO) {
        // 1. Tạo đối tượng StoreRequest chính
        StoreRequest storeRequest = new StoreRequest();
        storeRequest.setStoreId(requestDTO.getStoreId());
        storeRequest.setWarehouseId(requestDTO.getWarehouseId());
        storeRequest.setNotes(requestDTO.getNotes());
        storeRequest.setStatus("Pending");

        // 2. Tạo danh sách các sản phẩm (items) từ DTO
        List<StoreRequestItem> requestItems = new ArrayList<>();
        if (requestDTO.getItems() != null) {
            requestDTO.getItems().forEach(itemDTO -> {
                StoreRequestItem item = new StoreRequestItem();
                item.setProductId(itemDTO.getProductId());
                item.setQuantity(itemDTO.getQuantity());
                item.setUnitId(itemDTO.getUnitId());
                item.setStoreRequest(storeRequest);
                requestItems.add(item);
            });
        }
        storeRequest.setItems(requestItems);

        return storeRequestRepository.save(storeRequest);
    }

    @Override
    public List<StoreRequest> getStoreRequestByStore(Long storeId) {
        return storeRequestRepository.findByStoreId(storeId);
    }

    @Override
    public StoreRequest updateStoreRequest(Long requestId, StoreRequest requestDetails) {
        StoreRequest existingRequest = storeRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("StoreRequest not found with id: " + requestId));

        existingRequest.setStoreId(requestDetails.getStoreId());
        existingRequest.setWarehouseId(requestDetails.getWarehouseId());
        existingRequest.setNotes(requestDetails.getNotes());

        return storeRequestRepository.save(existingRequest);
    }

    @Override
    public StoreRequest updateStoreRequestStatus(Long requestId, String status) {
        StoreRequest existingRequest = storeRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("StoreRequest not found with id: " + requestId));

        existingRequest.setStatus(status);

        if ("Completed".equalsIgnoreCase(status)) {
            existingRequest.setCompletedDate(LocalDateTime.now());
        }

        return storeRequestRepository.save(existingRequest);
    }

    @Override
    public void deleteStoreRequest(Long requestId) {
        if (!storeRequestRepository.existsById(requestId)) {
            throw new EntityNotFoundException("StoreRequest not found with id: " + requestId);
        }
        storeRequestRepository.deleteById(requestId);
    }
}
