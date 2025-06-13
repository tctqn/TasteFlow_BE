package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.StoreRequestDTO;
import com.startup.tasteflowbe.model.StoreRequest;
import com.startup.tasteflowbe.service.StoreRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/store-requests")
@RequiredArgsConstructor
public class StoreRequestController {

    private final StoreRequestService storeRequestService;

    @GetMapping
    public ResponseEntity<List<StoreRequest>> getAllStoreRequests() {
        List<StoreRequest> requests = storeRequestService.getAllStoreRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoreRequest> getStoreRequestById(@PathVariable Long id) {
        return storeRequestService.getStoreRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createStoreRequest(@RequestBody StoreRequestDTO requestDTO) {
        try {
            // Thêm validation cơ bản
            if (requestDTO.getStoreId() == null || requestDTO.getWarehouseId() == null ||
                    requestDTO.getItems() == null || requestDTO.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body("StoreId, WarehouseId, and items list cannot be empty.");
            }
            StoreRequest createdRequest = storeRequestService.createStoreRequest(requestDTO);
            return new ResponseEntity<>(createdRequest, HttpStatus.CREATED);
        } catch (Exception e) {
            // Log the exception e
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating store request: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoreRequest> updateStoreRequest(@PathVariable Long id,
            @RequestBody StoreRequest requestDetails) {
        try {
            StoreRequest updatedRequest = storeRequestService.updateStoreRequest(id, requestDetails);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<StoreRequest> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            StoreRequest updatedRequest = storeRequestService.updateStoreRequestStatus(id, status);
            return ResponseEntity.ok(updatedRequest);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStoreRequest(@PathVariable Long id) {
        try {
            storeRequestService.deleteStoreRequest(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<StoreRequest>> getStoreRequestByStore(@PathVariable Long storeId) {
        List<StoreRequest> requests = storeRequestService.getStoreRequestByStore(storeId);
        return new ResponseEntity<>(requests, HttpStatus.OK);
    }

}
