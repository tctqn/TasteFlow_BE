package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.BulkApprovalRequestDTO;
import com.startup.tasteflowbe.dto.request.CreateWarehouseRequestDTO;
import com.startup.tasteflowbe.dto.request.ItemApprovalDTO;
import com.startup.tasteflowbe.model.WarehouseRequest;
import com.startup.tasteflowbe.model.WarehouseRequestItem;
import com.startup.tasteflowbe.service.WarehouseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouse-requests")
@RequiredArgsConstructor
public class WarehouseRequestController {

    private final WarehouseRequestService requestService; // Inject interface

    @PostMapping
    public ResponseEntity<WarehouseRequest> createRequest(@RequestBody CreateWarehouseRequestDTO requestDTO) {
        return ResponseEntity.ok(requestService.createWarehouseRequest(requestDTO));
    }

    @GetMapping
    public ResponseEntity<List<WarehouseRequest>> getAllRequests() {
        List<WarehouseRequest> requests = requestService.findAllRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WarehouseRequest> getRequestById(@PathVariable Integer id) {
        return requestService.findRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Integer id) {
        if (requestService.deleteRequest(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<WarehouseRequest> processBulkRequest(
            @PathVariable Integer requestId,
            @RequestBody BulkApprovalRequestDTO dto) {
        WarehouseRequest updatedRequest = requestService.updateRequestStatus(requestId, dto);

        return ResponseEntity.ok(updatedRequest);
    }

    @PutMapping("/item/{itemId}/approval")
    public ResponseEntity<WarehouseRequestItem> processRequestItem(
            @PathVariable Integer itemId,
            @RequestBody ItemApprovalDTO approvalDTO) {
        WarehouseRequestItem processedItem = requestService.processRequestItem(itemId, approvalDTO);
        return ResponseEntity.ok(processedItem);
    }
}
