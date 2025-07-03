package com.startup.tasteflowbe.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.startup.tasteflowbe.model.StoreRequestItem;
import com.startup.tasteflowbe.service.StoreRequestItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/store-request-items")
@RequiredArgsConstructor
public class StoreRequestItemController {
    private final StoreRequestItemService storeRequestItemService;

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<StoreRequestItem>> getRequestItems(@PathVariable Long requestId) {
        List<StoreRequestItem> requests = storeRequestItemService.getStoreRequestItemsByRequest(requestId);
        return ResponseEntity.ok(requests);
    }
}
