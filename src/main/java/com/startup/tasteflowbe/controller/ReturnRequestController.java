package com.startup.tasteflowbe.controller;

import com.startup.tasteflowbe.dto.request.ReturnRequestRequestDTO;
import com.startup.tasteflowbe.dto.response.ReturnRequestResponseDTO;
import com.startup.tasteflowbe.service.ReturnRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/return-requests")
@RequiredArgsConstructor
public class ReturnRequestController {

    private final ReturnRequestService service;

    @GetMapping
    public ResponseEntity<List<ReturnRequestResponseDTO>> getAll() {
        return ResponseEntity.ok(service.getAllReturnRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReturnRequestResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReturnRequestById(id));
    }

    @GetMapping("/by-order/{orderCode}")
    public ResponseEntity<List<ReturnRequestResponseDTO>> getByOrder(@PathVariable String orderCode) {
        return ResponseEntity.ok(service.getReturnRequestsByOriginalOrderCode(orderCode));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ReturnRequestResponseDTO> create(
            @RequestPart("payload") @Valid ReturnRequestRequestDTO payload,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createReturnRequest(payload, image));
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<ReturnRequestResponseDTO> update(
            @PathVariable Long id,
            @RequestPart("payload") @Valid ReturnRequestRequestDTO payload,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(service.updateReturnRequest(id, payload, image));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteReturnRequest(id);
        return ResponseEntity.noContent().build();
    }
}
